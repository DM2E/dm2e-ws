package eu.dm2e.ws.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

import eu.dm2e.utils.UriUtils;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.grafeo.Grafeo;

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
        job.setWebService(wsConf.getWebservice());
        job.setWebserviceConfig(wsConf);
        log.info("Creating human-readable label");
        {
        	StringBuilder rdfsLabelSB = new StringBuilder();
        	rdfsLabelSB.append("Web service Job ");
        	rdfsLabelSB.append("'");
        	rdfsLabelSB.append(wsConf.getWebservice().getLabel());
        	rdfsLabelSB.append("'");
        	rdfsLabelSB.append(" ["); 
        	rdfsLabelSB.append(DateTime.now().toString());
        	rdfsLabelSB.append(" for ");
        	rdfsLabelSB.append(
        			null != wsConf.getCreator()
        			? UriUtils.lastUriSegment(wsConf.getCreator().getId())
        			: null != wsConf.getWasGeneratedBy()
        					? UriUtils.lastUriSegment(wsConf.getWasGeneratedBy().getId())
        					: "Unknown Creator");
        	job.setLabel(rdfsLabelSB.toString());
        }
        job.addLogEntry("JobPojo constructed by AbstractTransformationService", "TRACE");
        try {
        	log.debug("Posting job to job service <{}>", client.getJobWebTarget());
			job.publishToService(client.getJobWebTarget());
		} catch (Exception e1) {
			return throwServiceError(e1);
		}

        /*
         * Let the asynchronous worker handle the job
         */
        try {
            AbstractTransformationService instance = this.getClass().newInstance();
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
    	WebTarget webResource = client.getConfigWebTarget();
    	Response resp = webResource.request().post(g.getNTriplesEntity());
    	if (null == resp.getLocation()) {
    		log.error("Invalid RDF string posted as configuration.");
    		return throwServiceError(resp.readEntity(String.class));
    	}
        return this.putConfigToService(resp.getLocation().toString());
    }
}
