package eu.dm2e.ws.services.demo;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.model.JobStatusConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 5/27/13
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class DemoServiceITCase {

    Logger log = Logger.getLogger(getClass().getName());

    private Client client;
    private static String URI_BASE = "http://localhost:9998";

    @Before
    public void setUp()
            throws Exception {
        client = new Client();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDemo() {
        // fail("Not yet implemented");
        WebResource webResource = client.resource(URI_BASE + "/service/demo");
        WebserviceConfigPojo config = new WebserviceConfigPojo();
        WebservicePojo ws = new WebservicePojo(webResource.getURI());
        config.setWebservice(ws);
//        config.getParameterAssignments().add(ws.getParamByName("sleeptime").createAssignment("2"));
        config.addParameterAssignment("sleeptime", "2");
        log.info("Configuration created for Test: " + config.getTurtle());
        ClientResponse response = webResource.post(ClientResponse.class, config.getTurtle());
        log.info("JOB STARTED WITH RESPONSE: " + response.getStatus() + " / Location: " + response.getLocation() + " / Content: " + response.getEntity(String.class));
        URI joburi = response.getLocation();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
        Grafeo g = new GrafeoImpl(joburi.toString());
        JobPojo job = g.getObjectMapper().getObject(JobPojo.class, joburi.toString());
        String status =  job.getStatus();
        log.info("Status after 1 seconds: " + status);
        assert(status.equals(JobStatusConstants.STARTED.name()));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
        g = new GrafeoImpl(joburi.toString());
        job = g.getObjectMapper().getObject(JobPojo.class, joburi.toString());
        status =  job.getStatus();
        log.info("Status after 4 seconds: " + status);
        assert(status.equals(JobStatusConstants.FINISHED.name()));

    }

}
