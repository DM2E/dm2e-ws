package eu.dm2e.ws.services;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ValidationReport;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

/**
 * Abstract Base Class for services that transform data.
 *
 * <p>
 * PUTting the URL of a WebserviceConfig to an inheriting service
 * will create a job and start the service's run method.
 * </p>
 *
 * @author Konstantin Baierer
 */

public abstract class AbstractTransformationService extends AbstractAsynchronousRDFService {
	
	/**
	 * The JobPojo object for the worker part of the service (to be used in the run() method)
	 */
	private JobPojo jobPojo;
	public JobPojo getJobPojo() {return this.jobPojo; };
	public void setJobPojo(JobPojo jobPojo) { this.jobPojo = jobPojo; };




    /**
     * GET /{resourceID}		Accept: *		Content-Type: RDF
     */
    @GET
    @Path("/job/{resourceId}")
    @Produces({
            DM2E_MediaType.APPLICATION_RDF_TRIPLES,
            DM2E_MediaType.APPLICATION_RDF_XML,
            DM2E_MediaType.APPLICATION_X_TURTLE,
            DM2E_MediaType.TEXT_PLAIN,
            DM2E_MediaType.TEXT_RDF_N3,
            DM2E_MediaType.TEXT_TURTLE
    })
    public Response getJobRDFHandler(@PathParam("resourceId") String resourceId) {
        log.debug("Job requested as RDF: " + resourceId);
        JobPojo job = WorkerExecutorSingleton.INSTANCE.jobs.get(resourceId);
        if (job==null) {
            log.debug("Job not found: " + resourceId);
            return Response.status(404).build();
        }
        return Response.ok().entity(getResponseEntity(job.getGrafeo())).build();
    }

    @GET
    @Path("/job/{resourceId}")
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response getJobJSONHandler(@PathParam("resourceId") String resourceId) {
        log.debug("Job requested as JSON: " + resourceId);
        JobPojo job = WorkerExecutorSingleton.INSTANCE.jobs.get(resourceId);
        if (job==null) {
            log.debug("Job not found: " + resourceId);
            return Response.status(404).build();
        }
        return Response.ok().header("Access-Control-Allow-Origin", "*").entity(job).build();
    }

    /**
     * GET /{id}/status			Accept: *		Content-Type: TEXT
     * Get the job status as a string.
     * @param resourceId
     */
    @GET
    @Path("/job/{resourceId}/status")
    public Response getJobStatus(@PathParam("resourceId") String resourceId) {
        log.debug("Job status requested: " + resourceId);
        JobPojo job = WorkerExecutorSingleton.INSTANCE.jobs.get(resourceId);
        if (job==null) {
            log.debug("Job not found: " + resourceId);
            return Response.status(404).build();
        }
        return Response.ok(job.getJobStatus()).build();
    }


	/**
	 * GET /{id}/log			Accept: *		Content-Type: TEXT_LOG
	 * @param minLevelStr
	 * @param maxLevelStr
	 */
	@GET
	@Path("/job/{resourceId}/log")
	@Produces(DM2E_MediaType.TEXT_X_LOG)
	public Response listLogEntriesAsLogFile(
			@PathParam("resourceId") String resourceId,
			@QueryParam("minLevel") String minLevelStr,
			@QueryParam("maxLevel") String maxLevelStr) {
        log.debug("Job log requested: " + resourceId);
        JobPojo job = WorkerExecutorSingleton.INSTANCE.jobs.get(resourceId);
        if (job==null) {
        	log.debug("Job not found: " + resourceId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
		return Response.ok().entity(job.toLogString(minLevelStr, maxLevelStr)).build();
	}
	

    /* (non-Javadoc)
     * @see eu.dm2e.ws.services.AbstractAsynchronousRDFService#putConfigToService(java.lang.String)
     */
    @Override
	@PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response putConfigToService(String configURI) {

        /*
         * Resolve configURI to WebserviceConfigPojo
         */
        log.info("Request to start the service: " + configURI);
        WebserviceConfigPojo wsConf = new WebserviceConfigPojo();
		try {
			wsConf.loadFromURI(configURI, 1);
		} catch (Exception e) {
			log.error("Exception: " + e);
			return throwServiceError("Could not load configURI"+ e);
		}
		
		/*		
		 * Validate the configuration
		 */
        ValidationReport res = wsConf.validate();
        if (!res.valid()) {
            log.error("Configuration not valid, throwing Error");
            return throwServiceError(res.toString());
        }




            /*
           * Build JobPojo
           * */
        JobPojo job = new JobPojo();
        String uuid = UUID.randomUUID().toString();
        job.setWebService(wsConf.getWebservice());
        job.setCreated(DateTime.now());
        job.setWebserviceConfig(wsConf);
        job.setHumanReadableLabel();
        //job.setParentJob(job);
        job.addLogEntry("JobPojo constructed by AbstractTransformationService", "TRACE");
        // Temporary ID handling, the job is persisted with a job service URI,
        // but as long as the temporary ID is set, job service redirects here.
        job.setTemporaryID(URI.create(this.getWebServicePojo().getId() + "/job/" + uuid));
        String id = job.publishToService(Config.get(ConfigProp.JOB_BASEURI));
        job.setId(id);
        WorkerExecutorSingleton.INSTANCE.jobs.put(uuid,job);
        /*
         * Let the asynchronous worker handle the job
         */
        try {
            AbstractTransformationService instance = this.getClass().newInstance();
            instance.setJobPojo(job);
            log.info("Job is before instantiation :" + job);
            WorkerExecutorSingleton.INSTANCE.handleJob(uuid, instance);
            log.debug("Thread should now have started...");
        } catch (InstantiationException e) {
            log.error("Error 3");
            return throwServiceError(e);
        } catch (IllegalAccessException e) {
            log.error("Error 4");
            return throwServiceError(e);
        } catch (Exception e) {
            log.error("Error 5");
            return throwServiceError(e);
        	
        }

        /*
         * Return JobPojo
         */
        log.debug("Returning 202");
        try {
            return Response
        		.status(202)
                .location(job.getIdAsURI())
                .entity(getResponseEntity(job.getGrafeo()))
                .build();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

//    @Override
//	@POST
//    @Consumes(MediaType.WILDCARD)
    @Override
    public Response postGrafeo(Grafeo g) {
    	WebTarget webResource = client.getConfigWebTarget();
    	Response resp = webResource.request().post(g.getNTriplesEntity());
    	if (null == resp.getLocation()) {
    		log.error("Invalid RDF string posted as configuration.");
    		return throwServiceError(resp.readEntity(String.class));
    	}
        return this.putConfigToService(resp.getLocation().toString());
    }
}
