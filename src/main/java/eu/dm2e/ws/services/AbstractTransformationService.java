package eu.dm2e.ws.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;

/**
 * TODO document
 */
public abstract class AbstractTransformationService extends AbstractAsynchronousRDFService {
	
	/**
	 * The JobPojo object for the worker part of the service (to be used in the run() method)
	 */
	private JobPojo jobPojo;
	public JobPojo getJobPojo() {return this.jobPojo; };
	public  void setJobPojo(JobPojo jobPojo) {};


    @Override
	@PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response startService(String configURI) {

        /*
         * Resolve configURI to WebserviceConfigPojo
         */
        WebserviceConfigPojo wsConf = new WebserviceConfigPojo();
		try {
			wsConf.loadFromURI(configURI, 1);
		} catch (Exception e) {
			return throwServiceError(e);
		}
		
		/*		
		 * Validate the configuration
		 */
		try {
			wsConf.validateConfig();
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
            WorkerExecutorSingleton.INSTANCE.handleJob(instance);
        } catch (NoSuchMethodException e) {
        	return throwServiceError(e);
        } catch (InvocationTargetException e) {
        	return throwServiceError(e);
        } catch (InstantiationException e) {
        	return throwServiceError(e);
        } catch (IllegalAccessException e) {
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

    @Override
	@POST
    @Consumes(MediaType.WILDCARD)
    public Response postConfig(String rdfString) {
    	WebResource webResource = client.resource("http://localhost:9998/config");
    	ClientResponse resp = webResource.post(ClientResponse.class, rdfString);
    	if (null == resp.getLocation()) {
    		log.severe("Invalid RDF string posted as configuration.");
    		return throwServiceError(resp.getEntity(String.class));
    	}
        return this.startService(resp.getLocation().toString());
    }

}
