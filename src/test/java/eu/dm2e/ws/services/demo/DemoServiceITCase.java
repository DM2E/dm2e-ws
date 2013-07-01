package eu.dm2e.ws.services.demo;

import com.sun.jersey.api.client.ClientResponse;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.api.AbstractJobPojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.wsmanager.ManageService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
@SuppressWarnings("unused")
public class DemoServiceITCase extends OmnomTestCase {

    Logger log = Logger.getLogger(getClass().getName());
    
    String SERVICE_URI;

    @Before
    public void setUp() throws Exception {
    	SERVICE_URI = URI_BASE + "service/demo";
        ManageService.startAll();
    }

    @Test
    public void testDescription() {
    	
    	log.info(SERVICE_URI);
    	ClientResponse resp = client.getJerseyClient()
    			.resource(SERVICE_URI)
    			.accept("text/turtle")
    			.get(ClientResponse.class);
    	String respStr = resp.getEntity(String.class);
    	log.info(respStr);
		assertEquals(200, resp.getStatus());
    	Grafeo g = new GrafeoImpl();
    	g.readHeuristically(respStr);
    	log.info(g.getTurtle());
    	assertTrue(g.containsTriple(SERVICE_URI, "rdf:type", "omnom:Webservice"));
    	assertTrue(g.containsTriple(SERVICE_URI, "omnom:inputParam", SERVICE_URI + "/param/sleeptime"));
    	assertTrue(g.containsTriple(SERVICE_URI + "/param/sleeptime", "rdf:type", "omnom:Parameter"));
    	assertTrue(g.containsTriple(SERVICE_URI + "/param/sleeptime", "omnom:parameterType", g.literal(g.expand("xsd:int"))));
    }
    
    @Test
    public void testPut() throws InterruptedException {
    	{
    		ClientResponse confResp = client
    				.getConfigWebResource()
    				.type("text/turtle")
    				.post(ClientResponse.class, configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING));
    		String confRespStr = confResp.getEntity(String.class);
    		log.info("testPut: " + confRespStr);
    		assertEquals(201, confResp.getStatus());
    		log.info("POST finished successfully.");
    		URI confLoc = confResp.getLocation();
    		assertNotNull(confLoc);
    		log.info("POST returned " + confLoc);
    		log.info("Beginning PUT");
    		ClientResponse serviceResp = client
    				.resource(SERVICE_URI)
    				.type(DM2E_MediaType.TEXT_PLAIN)
    				.put(ClientResponse.class, confLoc.toString());
    		assertEquals(202, serviceResp.getStatus());
    		log.info("PUT finished");
    		log.info("Beginning GET");
    		URI jobLoc = serviceResp.getLocation();
    		JobPojo job = new JobPojo(jobLoc);
    		assertNotNull(job.getId());
    		log.info(job.getTurtle());
    	}
    }
    
//    @Ignore("TODO")
    @Test
    public void testPostIllegal() {
    	{
	    	ClientResponse confResp = client
	    			.resource(SERVICE_URI)
	    			.type("text/turtle")
	    			.post(ClientResponse.class, configFile.get(OmnomTestResources.DEMO_SERVICE_ILLEGAL_PARAMETER));
	    	String confRespStr = confResp.getEntity(String.class);
	    	log.info("testPostIllegal: " + confRespStr);
	    	assertEquals(400, confResp.getStatus());
	    	assertThat(confRespStr, containsString(ErrorMsg.ILLEGAL_PARAMETER_VALUE.toString()));
    	}
    }

    @Test
    public void testDemo() {
    	
        WebservicePojo ws = new WebservicePojo();
        try {
			ws.loadFromURI(SERVICE_URI);
		} catch (Exception e1) {
			log.severe("Could reload job pojo." + e1);
			e1.printStackTrace();
		}
        WebserviceConfigPojo config = new WebserviceConfigPojo();
        config.setWebservice(ws);
//        config.setId(SERVICE_URI + "/" + )
//        client.publishPojoToConfigService(config);
//        config.addParameterAssignment("sleeptime", "2");
        client.publishPojoToConfigService(config);
        
        log.info("Configuration created for Test: " + config.getTurtle());
        
        ClientResponse response = client
        		.resource(SERVICE_URI)
        		.type(DM2E_MediaType.TEXT_PLAIN)
        		.put(ClientResponse.class, config.getId());
        log.info("JOB STARTED WITH RESPONSE: " + response.getStatus() + " / Location: " + response.getLocation() + " / Content: " + response.getEntity(String.class));
        assertEquals(202, response.getStatus());
        URI joburi = response.getLocation();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
        Grafeo g = new GrafeoImpl(joburi.toString());
        AbstractJobPojo job = g.getObjectMapper().getObject(JobPojo.class, joburi.toString());
        String status =  job.getStatus();
        log.info("Status after 1 seconds: " + status);
        assert(status.equals(JobStatus.STARTED.name()));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
        g = new GrafeoImpl(joburi.toString());
        job = g.getObjectMapper().getObject(JobPojo.class, joburi.toString());
        status =  job.getStatus();
        log.info("Status after 4 seconds: " + status);
        log.info(job.toLogString());
        assert(status.equals(JobStatus.FINISHED.name()));

    }

}
