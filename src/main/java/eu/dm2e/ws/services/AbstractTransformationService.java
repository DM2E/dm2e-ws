package eu.dm2e.ws.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.grafeo.Grafeo;

/**
 * TODO document
 */
public abstract class AbstractTransformationService extends AbstractAsynchronousRDFService {
	
	/**
	 * The JobPojo object for the worker part of the service (to be used in the run() method)
	 */
	private JobPojo jobPojo;
	public JobPojo getJobPojo() {return this.jobPojo; };
	public void setJobPojo(JobPojo jobPojo) { this.jobPojo = jobPojo; };


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
			log.severe("Exception: " + e);
			return throwServiceError(e);
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
        job.setWebService(wsConf.getWebservice());
        job.setWebserviceConfig(wsConf);
        job.addLogEntry("JobPojo constructed by AbstractTransformationService", "TRACE");
        job.publishToService();

        /*
         * Let the asynchronous worker handle the job
         */
        try {
            AbstractTransformationService instance = getClass().newInstance();
            Method method = getClass().getMethod("setJobPojo",JobPojo.class);
            method.invoke(instance, job);
            log.info("Job is before instantiation :" + job);
            WorkerExecutorSingleton.INSTANCE.handleJob(instance);
        } catch (NoSuchMethodException e) {
        	return throwServiceError(e);
        } catch (InvocationTargetException e) {
        	return throwServiceError(e);
        } catch (InstantiationException e) {
        	return throwServiceError(e);
        } catch (IllegalAccessException e) {
        	return throwServiceError(e);
        } catch (Exception e) {
        	return throwServiceError(e);
        	
        }

        /*
         * Return JobPojo
         */
        return Response
        		.status(202)
                .location(job.getIdAsURI())
                .entity(getResponseEntity(job.getGrafeo()))
                .build();
    }

//    @Override
//	@POST
//    @Consumes(MediaType.WILDCARD)
    @Override
    public Response postGrafeo(Grafeo g) {
    	WebResource webResource = client.getConfigWebResource();
    	ClientResponse resp = webResource.post(ClientResponse.class, g.getNTriples());
    	if (null == resp.getLocation()) {
    		log.severe("Invalid RDF string posted as configuration.");
    		return throwServiceError(resp.getEntity(String.class));
    	}
        return this.putConfigToService(resp.getLocation().toString());
    }
}
