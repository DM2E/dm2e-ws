package eu.dm2e.ws.services;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 5/27/13
 * Time: 12:29 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTransformationService extends AbstractRDFService implements Runnable {
    protected JobPojo jobPojo;

    /**
     * Default Constructor, to be used by JAX-RS for the REST API
     */
    protected AbstractTransformationService() {
    }

    /**
     * This constructor is used by the executor service, where one instance is created for each job.
     * @param jobPojo
     */
    protected AbstractTransformationService(JobPojo jobPojo) {
        this.jobPojo = jobPojo;
    }


    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response runDemoService(String configURI) {

        /*
           * Resolve configURI to WebserviceConfigPojo
           */
//		WebserviceConfigPojo wsConf = resolveWebSerivceConfigPojo(configURI);
        // TODO not very elegant
        WebserviceConfigPojo wsConfDummy = new WebserviceConfigPojo();
        wsConfDummy.setId(configURI);
        WebserviceConfigPojo wsConf = wsConfDummy.readFromEndpoint();

        /*
           * Build JobPojo
           * */
        JobPojo job = new JobPojo();
        // TODO the job probably doesn't even need a webservice reference since it's in the conf already
        job.setWebService(wsConf.getWebservice());
        job.setWebserviceConfig(wsConf);
        job.publish();

        /*
           * Let the asynchronous worker handle the job
           */
        try {
            AbstractTransformationService instance = getClass().getConstructor(JobPojo.class).newInstance(job);
            TransformationExecutorService.INSTANCE.handleJob(instance);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        } catch (InstantiationException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }


        /*
           * Return JobPojo
           */
        return Response
                .ok()
                .entity(getResponseEntity(job.getGrafeo()))
                .location(job.getIdAsURI())
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
    public Response postDemoService(String rdfString) {
        WebserviceConfigPojo conf = new WebserviceConfigPojo().constructFromRdfString(rdfString);
        conf.publish();
        return this.runDemoService(conf.getId());
    }

}
