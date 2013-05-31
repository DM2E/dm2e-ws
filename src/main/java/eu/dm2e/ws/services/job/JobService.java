package eu.dm2e.ws.services.job;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
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

//import java.util.ArrayList;
// TODO @GET /{id}/result with JSON

@Path("/job")
public class JobService extends AbstractRDFService {
	
	private static String ENDPOINT_QUERY = Config.getString("dm2e.ws.sparql_endpoint");
	private static String ENDPOINT_UPDATE = Config.getString("dm2e.ws.sparql_endpoint_statements");

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
		log.info("Reading job from endpoint " + ENDPOINT_QUERY);
		g.readFromEndpoint(ENDPOINT_QUERY, uriStr);
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
			return throwServiceError(ErrorMsg.BAD_RDF);
		}
		GResource blank = inputGrafeo.findTopBlank();
		if (blank == null) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
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
		if (null == newStatusStr || "".equals(newStatusStr))
			return throwServiceError(ErrorMsg.NO_JOB_STATUS);
		try {
			newStatus = Enum.valueOf(JobStatusConstants.class, newStatusStr);
		} catch (IllegalArgumentException e) {
			return throwServiceError(newStatusStr, ErrorMsg.INVALID_JOB_STATUS);
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

		String jobUri = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		
		URI entryUri;
		try {
			entryUri = new URI(jobUri + "/log/" + UUID.randomUUID().toString());
		} catch (URISyntaxException e) {
			return throwServiceError(e);
		}
		log.info(logRdfStr);
		Grafeo gEntry = new GrafeoImpl(IOUtils.toInputStream(logRdfStr));
		GResource blank = gEntry.findTopBlank();
		if (null == blank) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		blank.rename(entryUri.toString());
		gEntry.addTriple(jobUri, "omnom:hasLogEntry", entryUri.toString());
		gEntry.writeToEndpoint(ENDPOINT_UPDATE, jobUri);
		return Response.created(entryUri).build();
	}

	@POST
	@Path("/{id}/log")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response addLogEntryAsText(String logString) {
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		
		JobPojo jobPojo = new JobPojo();
		jobPojo.loadFromURI(resourceUriStr);
		LogLevel logLevel = LogLevel.DEBUG;
		for (LogLevel curLevel : LogLevel.values()) {
			if (logString.startsWith(curLevel.toString() + ": ")) {
				logLevel = curLevel;
				logString = logString.replaceFirst(curLevel.toString() + ": ", "");
				break;
			}
		}
		LogEntryPojo entry = jobPojo.addLogEntry(logString, logLevel.toString());
		jobPojo.publish();
		URI entryURI;
		try {
			entryURI = new URI(entry.getId());
		} catch (URISyntaxException e) {
			return throwServiceError(e);
		}
		return Response.created(entryURI).build();
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
