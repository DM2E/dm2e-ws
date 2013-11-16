package eu.dm2e.ws.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;

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
     * @return
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
        return Response.ok().entity(job).build();
    }

    /**
     * GET /{id}/status			Accept: *		Content-Type: TEXT
     * Get the job status as a string.
     * @param resourceId
     * @return
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
	 * @return
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
		try {
			wsConf.validate();
		} catch (Exception e) {
			return throwServiceError(e);
		}
		

        /*
         * Build JobPojo
         * */
        JobPojo job = new JobPojo();
        String uuid = UUID.randomUUID().toString();
        job.setId(this.getWebServicePojo().getId() + "/job/" + uuid);
        job.setWebService(wsConf.getWebservice());
        job.setCreated(DateTime.now());
        job.setWebserviceConfig(wsConf);
        job.setHumanReadableLabel();
        job.addLogEntry("JobPojo constructed by AbstractTransformationService", "TRACE");
        WorkerExecutorSingleton.INSTANCE.jobs.put(uuid,job);

        /*
         * Let the asynchronous worker handle the job
         */
        try {
            AbstractTransformationService instance = this.getClass().newInstance();
            Method method = getClass().getMethod("setJobPojo",JobPojo.class);
            method.invoke(instance, job);
            log.info("Job is before instantiation :" + job);
            WorkerExecutorSingleton.INSTANCE.handleJob(uuid, instance);
            log.debug("Thread should now have started...");
        } catch (NoSuchMethodException e) {
            log.error("Error 1");
            return throwServiceError(e);
        } catch (InvocationTargetException e) {
            log.error("Error 2");
            return throwServiceError(e);
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
