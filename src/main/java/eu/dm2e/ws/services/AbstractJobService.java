package eu.dm2e.ws.services;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

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
import eu.dm2e.ws.api.AbstractJobPojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.LogEntryPojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlUpdate;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.model.LogLevel;

public abstract class AbstractJobService extends AbstractRDFService {

	public AbstractJobService() {
		super();
	}

	public abstract Response putJob(Grafeo inputGrafeo, String uriStr);
	public abstract Response postJob(Grafeo inputGrafeo, String uriStr);
	public abstract Response getJob(Grafeo g, String uri);
	
	@PUT
	@Path("/{resourceID}")
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
	})
	@Produces({
		MediaType.WILDCARD
	})
	public Response putJobHandler(@PathParam("resourceID") String resourceID, File bodyAsFile) {
		log.info("Access to job: " + resourceID);
		String uriStr = uriInfo.getRequestUri().toString();
		Grafeo inputGrafeo;
		try {
			inputGrafeo = new GrafeoImpl(bodyAsFile);
		} catch (Exception e) {
			return throwServiceError(ErrorMsg.BAD_RDF);
		}
		return this.putJob(inputGrafeo, uriStr);
	}

	@POST
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
	})
	@Produces({
		MediaType.WILDCARD
	})
	public Response postJob(File bodyAsFile) {
		log.info("Config posted.");
		Grafeo inputGrafeo;
		try {
			inputGrafeo = new GrafeoImpl(bodyAsFile);
		} catch (Exception e) {
			return throwServiceError(ErrorMsg.BAD_RDF);
		}
		GResource blank = inputGrafeo.findTopBlank("omnom:Job");
		if (blank == null) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		String uriStr = getWebServicePojo().getId() + "/" + UUID.randomUUID().toString();;
		blank.rename(uriStr);
		return this.postJob(inputGrafeo, uriStr);
	}

	
	@GET
	@Path("/{resourceId}")
	@Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
	})
	public Response getJobHandler(@PathParam("resourceID") String resourceID) {
        log.info("Access to job: " + resourceID);
        String uriStr = uriInfo.getRequestUri().toString();
        Grafeo g = new GrafeoImpl();
        log.info("Reading job from endpoint " + Config.ENDPOINT_QUERY);
        try {
            g.readFromEndpoint(Config.ENDPOINT_QUERY, uriStr);
        } catch (Exception e1) {
            // if we couldn't read the job, try again once in a second
            try { Thread.sleep(1000); } catch (InterruptedException e) { }
            try { g.readFromEndpoint(Config.ENDPOINT_QUERY, uriStr);
            } catch (Exception e) {
                return throwServiceError(e);
            }
        }
        return this.getJob(g, uriStr);
    }
	
	
	/**
	 * Get the job status as a string.
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id}/status")
	public Response getJobStatus(@PathParam("id") String id) {
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/status$", "");
		AbstractJobPojo jobPojo = new JobPojo();
		try {
			jobPojo.loadFromURI(resourceUriStr);
			return Response.ok(jobPojo.getStatus()).build();
		} catch (Exception e) {
			return throwServiceError(e);
		}
	}

	/**
	 * Set the job status by string.
	 * @param id
	 * @param newStatusStr
	 * @return
	 */
	@PUT
	@Path("/{id}/status")
	@Consumes(MediaType.WILDCARD)
	public Response updateJobStatus(@PathParam("id") String id, String newStatusStr) {
		
		JobStatus newStatus;
		// validate if this is a valid status
		if (null == newStatusStr || "".equals(newStatusStr))
			return throwServiceError(ErrorMsg.NO_JOB_STATUS);
		try {
			newStatus = Enum.valueOf(JobStatus.class, newStatusStr);
		} catch (IllegalArgumentException e) {
			return throwServiceError(newStatusStr, ErrorMsg.INVALID_JOB_STATUS);
		}
		
		String jobUri = getRequestUriWithoutQuery().toString().replaceAll("/status$", "");
		SparqlUpdate sparul = new SparqlUpdate.Builder()
			.delete("?s " + NS.OMNOM.PROP_JOB_STATUS + " ?p")
			.insert("<" + jobUri + "> " + NS.OMNOM.PROP_JOB_STATUS + " \"" + newStatus.toString() + "\"")
			.endpoint(Config.ENDPOINT_UPDATE)
			.graph(jobUri)
			.build();
		log.info("Updating status with query: " + sparul);
		sparul.execute();
		
		return Response.created(getRequestUriWithoutQuery()).build();
	}

	@POST
	@Path("/{id}/log")
	@Consumes({ DM2E_MediaType.APPLICATION_RDF_TRIPLES, DM2E_MediaType.APPLICATION_RDF_XML,
			DM2E_MediaType.APPLICATION_X_TURTLE, DM2E_MediaType.TEXT_RDF_N3,
			DM2E_MediaType.TEXT_TURTLE })
	public Response postLogAsRDF(String logRdfStr) {
	//@formatter:on
	
		String jobUri = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		
		URI entryUri;
		try {
			entryUri = new URI(jobUri + "/log/" + UUID.randomUUID().toString());
		} catch (URISyntaxException e) {
			return throwServiceError(e);
		}
		Grafeo gEntry = new GrafeoImpl(IOUtils.toInputStream(logRdfStr));
		GResource blank = gEntry.findTopBlank("omnom:LogEntry");
		if (null == blank) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		blank.rename(entryUri.toString());
		gEntry.addTriple(jobUri, NS.OMNOM.PROP_LOG_ENTRY, entryUri.toString());
		log.info(gEntry.getNTriples());
		gEntry.writeToEndpoint(Config.ENDPOINT_UPDATE, jobUri);
		return Response.created(entryUri).build();
	}

	/**
	 * @param logString
	 * @return
	 */
	@POST
	@Path("/{id}/log")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response postLogAsText(String logString) {
		String jobUri = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		
		LogLevel logLevel = LogLevel.DEBUG;
		for (LogLevel curLevel : LogLevel.values()) {
			if (logString.startsWith(curLevel.toString() + ": ")) {
				logLevel = curLevel;
				logString = logString.replaceFirst(curLevel.toString() + ": ", "");
				break;
			}
		}
		LogEntryPojo entry = new LogEntryPojo();
		entry.setId(jobUri + "/log/" + createUniqueStr());
		entry.setMessage(logString);
		entry.setLevel(logLevel);
		entry.setTimestamp(new Date());
		Grafeo outG = entry.getGrafeo();
		outG.addTriple(jobUri, NS.OMNOM.PROP_LOG_ENTRY, entry.getId());
		outG.writeToEndpoint(Config.ENDPOINT_UPDATE, jobUri);
		return Response.created(entry.getIdAsURI()).build();
	}

	@GET
	@Path("/{id}/log")
	@Produces({ DM2E_MediaType.TEXT_TURTLE, DM2E_MediaType.TEXT_RDF_N3,
			DM2E_MediaType.APPLICATION_RDF_TRIPLES, DM2E_MediaType.APPLICATION_RDF_XML })
	public Response listLogEntries(@QueryParam("minLevel") String minLevelStr, @QueryParam("maxLevel") String maxLevelStr) {
	
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		AbstractJobPojo jobPojo = new JobPojo();
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
	@Produces(DM2E_MediaType.TEXT_X_LOG)
	public Response listLogEntriesAsLogFile(@QueryParam("minLevel") String minLevelStr, @QueryParam("maxLevel") String maxLevelStr) {
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		AbstractJobPojo jobPojo = new JobPojo();
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