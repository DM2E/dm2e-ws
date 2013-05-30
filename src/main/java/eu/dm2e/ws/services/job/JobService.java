package eu.dm2e.ws.services.job;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.LogEntryPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.model.JobStatusConstants;
import eu.dm2e.ws.model.LogLevel;
import eu.dm2e.ws.services.AbstractRDFService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

//import java.util.ArrayList;
// TODO @GET /{id}/result with JSON

@Path("/job")
public class JobService extends AbstractRDFService {

	private Logger log = Logger.getLogger(getClass().getName());
	
	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.setLabel("Job Service");
//		ws.setId("http://localhost:9998/job");
		return ws;
	}

	@GET
	@Path("/{resourceID}")
	@Consumes(MediaType.WILDCARD)
	public Response getJob(@PathParam("resourceID") String resourceID) {
		log.info("Access to job: " + resourceID);
		String uriStr = uriInfo.getRequestUri().toString();
		Grafeo g = new GrafeoImpl();
		log.info("Reading job from endpoint " + Config.getString("dm2e.ws.sparql_endpoint"));
		g.readFromEndpoint(Config.getString("dm2e.ws.sparql_endpoint"), uriStr);
		JobPojo job = g.getObjectMapper().getObject(JobPojo.class, g.resource(uriStr));
		String jobStatus = job.getStatus();
        log.info("Job status: " + jobStatus);
		int httpStatus;
		if (jobStatus.equals(JobStatusConstants.NOT_STARTED.toString())) httpStatus = 202;
		else if (jobStatus.equals(JobStatusConstants.STARTED.toString())) httpStatus = 202;
		else if (jobStatus.equals(JobStatusConstants.FAILED.toString())) httpStatus = 409;
		else if (jobStatus.equals(JobStatusConstants.FINISHED.toString())) httpStatus = 200;
		else httpStatus = 400;
        log.info("Returned HTTP Status: " + httpStatus);
        try {
			return Response.status(httpStatus).entity(getResponseEntity(job.getGrafeo())).build();
        } catch (NullPointerException e) {
			return Response.notAcceptable(supportedVariants).build();
        }
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	public Response newJob(File bodyAsFile) {
		log.info("Config posted.");
		// TODO use Exception to return proper HTTP response if input can not be
		// parsed as RDF
		Grafeo inputGrafeo;
		try {
			inputGrafeo = new GrafeoImpl(bodyAsFile);
		} catch (Exception e) {
			return throwServiceError(e);
		}
		GResource blank = inputGrafeo.findTopBlank();
		if (blank == null) {
			return throwServiceError("No top blank node found. Check your job description.");
		}
		String id = "" + new Date().getTime();
		log.warning("Instantiating Job POJO.");
		String uri = getWebServicePojo().getId() + "/" + id;
		JobPojo jobPojo = inputGrafeo.getObjectMapper().getObject(JobPojo.class, blank);
		jobPojo.setId(uri);
		Grafeo outputGrafeo = new GrafeoImpl();
		outputGrafeo.getObjectMapper().addObject(jobPojo);
		outputGrafeo.writeToEndpoint(NS.ENDPOINT_STATEMENTS, uri);
		return Response.created(URI.create(uri)).entity(getResponseEntity(inputGrafeo)).build();
	}

	@GET
	@Path("/{id}/status")
	public Response getJobStatus(@PathParam("id") String id) {
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/status$", "");
		JobPojo jobPojo = new JobPojo();
		jobPojo.loadFromURI(resourceUriStr);
		return Response.ok(jobPojo.getStatus()).build();
	}

	@PUT
	@Path("/{id}/status")
	@Consumes(MediaType.WILDCARD)
	public Response updateJobStatus(@PathParam("id") String id, String newStatusStr) {
		
		JobStatusConstants newStatus;
		// validate if this is a valid status
		try {
			if (null == newStatusStr) return throwServiceError("No status sent.");
			newStatus = Enum.valueOf(JobStatusConstants.class, newStatusStr);
		} catch (Exception e) {
			return throwServiceError("Invalid status type: " + newStatusStr);
		}
		
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/status$", "");
		JobPojo jobPojo = new JobPojo();
		jobPojo.loadFromURI(resourceUriStr);
		jobPojo.setStatus(newStatus);
		jobPojo.publish();
		return Response.created(getRequestUriWithoutQuery()).build();
	}

	//@formatter:off
	@POST
	@Path("/{id}/log")
	@Consumes({ DM2E_MediaType.APPLICATION_RDF_TRIPLES,
				DM2E_MediaType.APPLICATION_RDF_XML,
				DM2E_MediaType.TEXT_RDF_N3, 
				DM2E_MediaType.TEXT_TURTLE})
	public Response addLogEntryAsRDF(String logRdfStr)  {
	//@formatter:on

		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		JobPojo jobPojo = new JobPojo();
		jobPojo.loadFromURI(resourceUriStr);
		LogEntryPojo logEntry = null;
		
		try {
			Grafeo g = new GrafeoImpl(IOUtils.toInputStream(logRdfStr));
			GResource blank = g.findTopBlank();
			String newUri = resourceUriStr + "/log/" + UUID.randomUUID().toString();
			blank.rename(newUri);
			logEntry = g.getObjectMapper().getObject(LogEntryPojo.class, newUri);
			jobPojo.addLogEntry(logEntry);
			jobPojo.publish();
		} catch (Exception e) {
			return throwServiceError(e);
		}
		return Response.created(getRequestUriWithoutQuery()).build();
	}

	@POST
	@Path("/{id}/log")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response addLogEntryAsText(String logString) {
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		
		JobPojo jobPojo = new JobPojo();
		jobPojo.loadFromURI(resourceUriStr);
		jobPojo.addLogEntry(logString, LogLevel.DEBUG.toString());
		jobPojo.publish();
		return Response.created(getRequestUriWithoutQuery()).build();
	}

	@GET
	@Path("/{id}/log")
	@Produces({
		DM2E_MediaType.TEXT_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
	})
	public Response listLogEntries(@QueryParam("minLevel") String minLevelStr, @QueryParam("maxLevel") String maxLevelStr) {

		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		JobPojo jobPojo = new JobPojo();
		jobPojo.loadFromURI(resourceUriStr);
		Set<LogEntryPojo> logEntries = jobPojo.getLogEntries(minLevelStr, maxLevelStr);
		Grafeo logGrafeo = new GrafeoImpl();
		for (LogEntryPojo logEntry : logEntries) {
			logGrafeo.getObjectMapper().addObject(logEntry);
		}
		return getResponse(logGrafeo);
	}

	@GET
	@Path("/{id}/log")
	@Produces({ "text/x-log" })
	public Response listLogEntriesAsLogFile(@QueryParam("minLevel") String minLevelStr, @QueryParam("maxLevel") String maxLevelStr) {
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		JobPojo jobPojo = new JobPojo();
		jobPojo.loadFromURI(resourceUriStr);
		return Response.ok().entity(jobPojo.toLogString(minLevelStr, maxLevelStr)).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces({ "text/x-log" })
	public Response listLogEntriesAsLogFileFromJob(@QueryParam("minLevel") String minLevelStr, @QueryParam("maxLevel") String maxLevelStr) {
		return this.listLogEntriesAsLogFile(minLevelStr, maxLevelStr);
	}
}
