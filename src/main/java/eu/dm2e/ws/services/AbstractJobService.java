package eu.dm2e.ws.services;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.joda.time.DateTime;

import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.AbstractConfigPojo;
import eu.dm2e.ws.api.AbstractJobPojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.LogEntryPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowJobPojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlUpdate;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.model.LogLevel;

public abstract class AbstractJobService extends AbstractRDFService {

	public AbstractJobService() {
		super();
	}

	public abstract Response putJob(Grafeo inputGrafeo, GResource uriStr);
	public abstract Response postJob(Grafeo inputGrafeo, GResource blank);
	public abstract Response getJob(Grafeo g, GResource uri);
	@GET
	@Path("/list")
	@Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE,
		MediaType.APPLICATION_JSON
	})
	public Response getJobList() {
//		Grafeo g = new GrafeoImpl();
//		g.readTriplesFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), null, "rdf:type", g.resource(NS.OMNOM.CLASS_JOB));
//		g.readTriplesFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), null, "rdf:type", g.resource(NS.OMNOM.CLASS_WORKFLOW_JOB));
//		return getResponse(g);
    	List<Class<? extends AbstractJobPojo>> pojoClasses = Arrays.asList(
			JobPojo.class,
			WorkflowJobPojo.class
		);
        List<SerializablePojo> jobList = new ArrayList<>();
    	for (Class<? extends AbstractJobPojo> pojoClass  : pojoClasses) {
    		GrafeoImpl g = new GrafeoImpl();
    		String pojoRdfClass = pojoClass.getAnnotation(RDFClass.class).value();
    		g.readTriplesFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), null, NS.RDF.PROP_TYPE, g.resource(pojoRdfClass));
    		for (GResource gres : g.listSubjects()) {
    			if (gres.isAnon()) continue;
    			AbstractJobPojo pojo = null;
				try {
					pojo = pojoClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					log.error("Could not instantiate {} for URI {}", pojoClass, gres);
					return throwServiceError("INTERNAL ERROR ");
				}
    			pojo.setId(gres.getUri());
				pojo.loadFromURI(pojo.getId());
    			jobList.add(pojo);
    		}
    	}
    	return Response.ok(jobList).build();
	}
	
	/**
	 * PUT /{resourceID} 		Accept: RDF		Content-Type: RDF
	 * @param resourceID
	 * @param bodyAsFile
	 * @return
	 */
	@PUT
	@Path("{resourceID}")
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
		log.info("Access to job: " + getRequestUriWithoutQuery());
		// FIXME BUG FIXME Why oh Why Does this fail randomly? resourceID is
		// often 'null' for no reason :( Using workaround now by parsing the URL.
		log.warn("Job ID of the job to PUT: " + resourceID);
		if (null == resourceID) {
			log.warn("Jersey thinks resourceID is null. Parsing the URL. ");
			resourceID = getRequestUriWithoutQuery().toString().replace(
					popPath(getRequestUriWithoutQuery()).toString(), "");
			log.warn("Parsed the resourceID as " + resourceID);
			if (null == resourceID) {
				throw new RuntimeException(new NullPointerException("resourceID is null"));
			}
//			return throwServiceError("resourceID is null");
		}
		String uriStr = uriInfo.getRequestUri().toString();
		Grafeo inputGrafeo;
		try {
			inputGrafeo = new GrafeoImpl(bodyAsFile);
		} catch (Exception e) {
			return throwServiceError(ErrorMsg.BAD_RDF);
		}
		
		log.trace("Skolemizing");
		GResource blank = inputGrafeo.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_JOB);
		if (blank != null) {
			blank.rename(uriStr);
		} else {
			blank = inputGrafeo.findTopBlank(NS.OMNOM.CLASS_JOB);
			if (blank != null) {
				blank.rename(uriStr);
			}
		}
		inputGrafeo.skolemizeUUID(uriStr, NS.OMNOM.PROP_LOG_ENTRY, "log");
		inputGrafeo.skolemizeUUID(uriStr, NS.OMNOM.PROP_ASSIGNMENT, "assignment");
		
		log.debug("Instantiating " + uriStr);
		GrafeoImpl outputGrafeo = new GrafeoImpl();
		if (inputGrafeo.resource(uriStr).isa(NS.OMNOM.CLASS_WORKFLOW_JOB)) {
			log.debug("Will instantiate as WorkflowJobPojo : " + uriStr);
			WorkflowJobPojo workflowJobPojo;
			try {
				workflowJobPojo = inputGrafeo.getObjectMapper().getObject(WorkflowJobPojo.class, uriStr);
			} catch (Exception e) {
				log.info("Instantiation exception: " + e);
				e.printStackTrace();
				return throwServiceError(e);
			}
			outputGrafeo.getObjectMapper().addObject(workflowJobPojo);
		}
		else {
			log.debug("Will instantiate as JobPojo : " + uriStr);
			try {
				JobPojo jobPojo = inputGrafeo.getObjectMapper().getObject(JobPojo.class, uriStr);
				outputGrafeo.getObjectMapper().addObject(jobPojo);
			} catch (Exception e) {
				log.warn("Instantiation exception: {}", e);
				e.printStackTrace();
				return throwServiceError(e);
			}
		}
		
		
		return this.putJob(outputGrafeo, outputGrafeo.get(uriStr));
	}

	/**
	 * POST /					Accept: RDF		Content-Type: RDF
	 * @param bodyAsString
	 * @return
	 */
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
	public Response postJobRDFHandler(String bodyAsString) {
		log.trace(LogbackMarkers.DATA_DUMP, "Job posted: {}", bodyAsString);
		Grafeo inputGrafeo;
		try {
			inputGrafeo = new GrafeoImpl();
			inputGrafeo.readHeuristically(bodyAsString);
		} catch (Exception e) {
			return throwServiceError(ErrorMsg.BAD_RDF);
		}
		GResource blank = inputGrafeo.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_JOB);
		if (blank == null) {
			blank = inputGrafeo.findTopBlank(NS.OMNOM.CLASS_JOB);
			if (blank == null)
				return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE + inputGrafeo.getTurtle());
		}
		String uriStr = getWebServicePojo().getId() + "/" + UUID.randomUUID().toString();;
		blank.rename(uriStr);
//		log.info(inputGrafeo.getNTriples());
		
		log.trace("Skolemizing");
		inputGrafeo.skolemizeUUID(uriStr, NS.OMNOM.PROP_ASSIGNMENT, "assignment");
		inputGrafeo.skolemizeUUID(uriStr, NS.OMNOM.PROP_LOG_ENTRY, "log");
		
		log.debug("Instantiating " + uriStr);
		GrafeoImpl outputGrafeo = new GrafeoImpl();
		if (blank.isa(NS.OMNOM.CLASS_WORKFLOW_JOB)) {
			log.debug("Will instantiate as WorkflowJobPojo : " + uriStr);
			WorkflowJobPojo workflowJobPojo;
			try {
				workflowJobPojo = inputGrafeo.getObjectMapper().getObject(WorkflowJobPojo.class, uriStr);
			} catch (Exception e) {
				log.warn("Instantiation exception: " + e);
				e.printStackTrace();
				return throwServiceError(e);
			}
			log.debug("Instantiated");
			// read workflow description
			WorkflowPojo w = workflowJobPojo.getWorkflow();
			if (null != w && null != w.getId())
				outputGrafeo.load(w.getId());
			outputGrafeo.getObjectMapper().addObject(workflowJobPojo);
		}
		else {
			log.debug("Will instantiate as JobPojo : " + uriStr);
			JobPojo jobPojo = inputGrafeo.getObjectMapper().getObject(JobPojo.class, uriStr);
			WebservicePojo w = jobPojo.getWebService();
			if (null != w && null != w.getId())
				outputGrafeo.load(w.getId());
			outputGrafeo.getObjectMapper().addObject(jobPojo);
		}
		
		return this.postJob(outputGrafeo, blank);
	}
	
	/**
	 * GET /{resourceID}		Accept: *		Content-Type: RDF
	 * @return
	 */
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
	public Response getJobRDFHandler() {
        URI uri = getRequestUriWithoutQuery();
        Grafeo g = new GrafeoImpl();
        log.debug("Reading job from endpoint " + Config.get(ConfigProp.ENDPOINT_QUERY));
        try {
            g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), uri);
        } catch (Exception e1) {
            // if we couldn't read the job, try again once in a second
            try { Thread.sleep(1000); } catch (InterruptedException e) { }
            try { g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), uri);
            } catch (Exception e) {
                return throwServiceError(e);
            }
        }
        return this.getJob(g, g.resource(uri));
    }
	
	@GET
	@Path("{id}")
	@Produces({
		MediaType.APPLICATION_JSON
	})
	public Response getJobJSONHandler() {
		URI uri = getRequestUriWithoutQuery();
        Grafeo g = new GrafeoImpl();
        log.debug("Reading job from endpoint " + Config.get(ConfigProp.ENDPOINT_QUERY));
        try {
            g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), uri);
        } catch (Exception e1) {
            // if we couldn't read the job, try again once in a second
            try { Thread.sleep(1000); } catch (InterruptedException e) { }
            try { g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), uri);
            } catch (Exception e) {
                return throwServiceError(e);
            }
        }
        SerializablePojo pojo;
        if (g.resource(uri).isa(NS.OMNOM.CLASS_WORKFLOW_JOB)) {
        	pojo = g.getObjectMapper().getObject(WorkflowJobPojo.class, uri);
        } else if(g.resource(uri).isa(NS.OMNOM.CLASS_JOB)) {
        	pojo = g.getObjectMapper().getObject(JobPojo.class, uri);
        } else  {
        	return throwServiceError(ErrorMsg.WRONG_RDF_TYPE);
        }
        return Response.ok().entity(pojo).build();
	}
	
	/**
	 * GET /{id}/status			Accept: *		Content-Type: TEXT
	 * Get the job status as a string.
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id}/status")
	public Response getJobStatus(@PathParam("id") String id) {
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/status$", "");
		// this must be a concrete JobPojo but it still uses only the means of AbstractJobPojo
		// so it should work for workflowjobs as well.
		AbstractJobPojo jobPojo = new JobPojo();
		try {
			jobPojo.loadFromURI(resourceUriStr);
			return Response.ok(jobPojo.getJobStatus()).build();
		} catch (Exception e) {
			return throwServiceError(e);
		}
	}

	/**
	 * PUT /{id}/status			Accept: TEXT,	Content-Type: *
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
			.delete("?s <" + NS.OMNOM.PROP_JOB_STATUS + "> ?p")
			.insert("<" + jobUri + "> <" + NS.OMNOM.PROP_JOB_STATUS + "> \"" + newStatus.toString() + "\"")
			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
			.graph(jobUri)
			.build();
		log.debug(LogbackMarkers.DATA_DUMP, "Updating status with query: {}", sparul);
		sparul.execute();
		
		return Response.created(getRequestUriWithoutQuery()).build();
	}

	/**
	 * POST /{id}/log			Accept: RDF		Content-Type: *
	 * @param logRdfStr
	 * @return
	 */
	@POST
	@Path("/{id}/log")
	@Consumes({ 
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE 
	})
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
		GResource blank = gEntry.findTopBlank(NS.OMNOM.CLASS_LOG_ENTRY);
		if (null == blank) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		blank.rename(entryUri.toString());
		gEntry.addTriple(jobUri, NS.OMNOM.PROP_LOG_ENTRY, entryUri.toString());
		log.debug(LogbackMarkers.DATA_DUMP, gEntry.getNTriples());
		gEntry.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), jobUri);
		return Response.created(entryUri).build();
	}

	/**
	 * POST /{id}/log			Accept: TEXT	Content-Type: *
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
		entry.setTimestamp(DateTime.now());
		Grafeo outG = entry.getGrafeo();
		outG.addTriple(jobUri, NS.OMNOM.PROP_LOG_ENTRY, entry.getId());
		outG.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), jobUri);
		return Response.created(entry.getIdAsURI()).build();
	}

	/**
	 * GET /{id}/log			Accept: *		Content-Type: RDF
	 * @param minLevelStr
	 * @param maxLevelStr
	 * @return
	 */
	@GET
	@Path("/{id}/log")
	@Produces({ 
		DM2E_MediaType.TEXT_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML })
	public Response listLogEntries(@QueryParam("minLevel") String minLevelStr, @QueryParam("maxLevel") String maxLevelStr) {
	
		URI resourceUri  = popPath(getRequestUriWithoutQuery());
		AbstractJobPojo jobPojo = new JobPojo();
		try {
			jobPojo.loadFromURI(resourceUri);
		} catch (Exception e) {
			log.warn("Could not reload job pojo.", e);
			throwServiceError(e);
		}
		Set<LogEntryPojo> logEntries = jobPojo.getLogEntries(minLevelStr, maxLevelStr);
		Grafeo logGrafeo = new GrafeoImpl();
		for (LogEntryPojo logEntry : logEntries) {
			logGrafeo.getObjectMapper().addObject(logEntry);
		}
		return getResponse(logGrafeo);
	}

	/**
	 * GET /{id}/log			Accept: *		Content-Type: TEXT_LOG
	 * @param minLevelStr
	 * @param maxLevelStr
	 * @return
	 */
	@GET
	@Path("/{id}/log")
	@Produces(DM2E_MediaType.TEXT_X_LOG)
	public Response listLogEntriesAsLogFile(@QueryParam("minLevel") String minLevelStr, @QueryParam("maxLevel") String maxLevelStr) {
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		AbstractJobPojo jobPojo = new JobPojo();
		try {
			jobPojo.loadFromURI(resourceUriStr);
		} catch (Exception e) {
			log.warn("Could not reload job pojo.", e);
			throwServiceError(e);
		}
		return Response.ok().entity(jobPojo.toLogString(minLevelStr, maxLevelStr)).build();
	}

	/**
	 * GET /{id}				Accept: TEXT_LOG		Content-Type: TEXT
	 * @param minLevelStr
	 * @param maxLevelStr
	 * @return
	 */
	@GET
	@Path("/{id}")
	@Produces({ "text/x-log" })
	public Response listLogEntriesAsLogFileFromJob(@QueryParam("minLevel") String minLevelStr, @QueryParam("maxLevel") String maxLevelStr) {
		return this.listLogEntriesAsLogFile(minLevelStr, maxLevelStr);
	}

	/**
	 * POST /{id}/assignment	Accept: RDF				Content-Type: RDF
	 * @param bodyAsFile
	 * @return
	 */
	@POST
	@Consumes(MediaType.WILDCARD)
	@Path("/{id}/assignment")
	public Response postAssignment(File bodyAsFile) {
		Grafeo inputGrafeo;
		try {
			inputGrafeo = new GrafeoImpl(bodyAsFile);
		} catch (Exception e) {
			return throwServiceError(ErrorMsg.BAD_RDF);
		}
		GResource blank = inputGrafeo.findTopBlank(NS.OMNOM.CLASS_PARAMETER_ASSIGNMENT);
		if (blank == null) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		URI jobUri = popPath();
		URI assUri = appendPath(createUniqueStr());
		blank.rename(assUri);
		ParameterAssignmentPojo ass = inputGrafeo.getObjectMapper().getObject(ParameterAssignmentPojo.class, assUri);
		ass.setId(assUri);
		
		Grafeo outputGrafeo = new GrafeoImpl();
		outputGrafeo.getObjectMapper().addObject(ass);
		outputGrafeo.addTriple(jobUri.toString(), NS.OMNOM.PROP_ASSIGNMENT, assUri.toString());
		SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete("?s ?p ?o.")
				.insert(outputGrafeo.getNTriples())
				.graph(jobUri)
				.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
				.build();
		sparul.execute();
		return Response.created(assUri).entity(getResponseEntity(ass.getGrafeo())).build();
	}
	
    /**
     * GET /{id}/assignment/{assId}		Accept: *		Content-Type: RDF
     * @param id
     * @param assId
     * @return
     */
    @GET
    @Path("{id}/assignment/{assId}")
    public Response getAssignment( @PathParam("id") String id, @PathParam("assId") String assId) {
        log.debug("Output Assignment " + assId + " of job requested: " + uriInfo.getRequestUri());
        URI uri = popPath(popPath());
        return Response.status(303).location(uri).build();
    }
}
