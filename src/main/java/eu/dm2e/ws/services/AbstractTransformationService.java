package eu.dm2e.ws.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;

/**
 * TODO document
 */
public abstract class AbstractTransformationService extends AbstractRDFService implements Runnable {
	
    /**
     * The JobPojo object for the worker part of the service (to be used in the run() method)
     */
    protected JobPojo jobPojo;
    
	/**
	 * Creating Jersey API clients is relatively expensive so we do it once per Service statically
	 */
	protected static Client jerseyClient = new Client();
	
	protected static final String FILE_SERVICE_URI = Config.getString("dm2e.service.file.base_uri");

    public void setJobPojo(JobPojo jobPojo) {
        this.jobPojo = jobPojo;
    }
    

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
         * Build JobPojo
         * */
        JobPojo job = new JobPojo();
        job.setWebService(wsConf.getWebservice());
        job.setWebserviceConfig(wsConf);
        job.publish();

        /*
         * Let the asynchronous worker handle the job
         */
        try {
            AbstractTransformationService instance = getClass().newInstance();
            Method method = getClass().getMethod("setJobPojo",JobPojo.class);
            method.invoke(instance, job);
            TransformationExecutorService.INSTANCE.handleJob(instance);
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

    /**
     * Convenience method that accepts a configuration, publishes it
     * directly to the ConfigurationService and then calls the TransformationService
     * with the persistent URI.
     *
     * Only to be used for development, not for production!
     *
     * @param rdfString
     * @return
     */
    @POST
    @Consumes(MediaType.WILDCARD)
    public Response postConfig(String rdfString) {
        WebserviceConfigPojo conf = new WebserviceConfigPojo().constructFromRdfString(rdfString);
        if (null == conf) {
        	return throwServiceError("Invalid RDF string passed as configuration.");
        }
        conf.publish();
        return this.startService(conf.getId());
    }
    
    protected String storeAsFile(String fileData, String mediaTypeStr) {
		WebResource fileResource = jerseyClient.resource(FILE_SERVICE_URI);
	    jobPojo.trace("Building file service multipart envelope.");
		FormDataMultiPart form = new FormDataMultiPart();

		// add file part
		MediaType fileType = MediaType.valueOf(mediaTypeStr);
		FormDataBodyPart fileFDBP = new FormDataBodyPart("file", fileData, fileType);
		form.bodyPart(fileFDBP);

		// add metadata part
		FilePojo fileDesc = new FilePojo();
		fileDesc.setGeneratorJob(jobPojo);
		fileDesc.setMediaType(fileType.toString());
		MediaType n3_type = MediaType.valueOf(DM2E_MediaType.TEXT_RDF_N3);
		FormDataBodyPart metaFDBP = new FormDataBodyPart("meta", fileDesc.getNTriples(), n3_type);
		form.bodyPart(metaFDBP);

		// build the response stub
		Builder builder = fileResource
				.type(MediaType.MULTIPART_FORM_DATA)
				.accept(DM2E_MediaType.TEXT_TURTLE)
				.entity(form);
		
		// Post the file to the file service
		jobPojo.info("Posting result to the file service.");
		ClientResponse resp = builder.post(ClientResponse.class);
		if (resp.getStatus() >= 400) {
			jobPojo.fatal("File storage failed: " + resp.getEntity(String.class));
			jobPojo.setFailed();
			return null;
		}
		String fileLocation = resp.getLocation().toString();
		if (fileLocation == null) {
			jobPojo.warn("File Service didn't respond with a Location header.");
			return null;
		}
		jobPojo.info("File stored at: " + fileLocation);
		try {
			fileDesc.setFileRetrievalURI(fileLocation);
			fileDesc.publish();
		} catch(Exception e) {
			jobPojo.fatal(e);
		}
		return fileLocation;
    }

}
