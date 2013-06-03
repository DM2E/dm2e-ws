package eu.dm2e.ws.services.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.InputStream;
import java.net.URI;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.model.JobStatusConstants;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public class DemoServiceITCase extends OmnomTestCase {

    Logger log = Logger.getLogger(getClass().getName());
    
    String SERVICE_URI;

    @Before
    public void setUp() throws Exception {
    	SERVICE_URI = URI_BASE + "service/demo";
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void testDescription() {
    	
    	log.info(SERVICE_URI);
    	Grafeo g = new GrafeoImpl(client.getJerseyClient()
    			.resource(SERVICE_URI)
    			.accept("text/turtle")
    			.get(InputStream.class));
    	log.info(g.getTurtle());
    	assertTrue(g.containsStatementPattern(SERVICE_URI, "rdf:type", "omnom:Webservice"));
    	assertTrue(g.containsStatementPattern(SERVICE_URI, "omnom:inputParam", SERVICE_URI + "/param/sleeptime"));
    	assertTrue(g.containsStatementPattern(SERVICE_URI + "/param/sleeptime", "rdf:type", "omnom:Parameter"));
    	assertTrue(g.containsStatementPattern(SERVICE_URI + "/param/sleeptime", "omnom:parameterType", g.literal(g.expand("xsd:int"))));
    }
    
    @Test
    public void testPut() {
    	{
    		ClientResponse confResp = client
    				.getConfigWebResource()
    				.type("text/turtle")
    				.post(ClientResponse.class, configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING));
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
    		log.info("PUT finished");
    		String serviceRespStr = serviceResp.getEntity(String.class);
    		System.out.println(serviceRespStr);
    		assertEquals(202, serviceResp.getStatus());
    	}
    }
    
    @Ignore("TODO")
    @Test
    public void testPostIllegal() {
    	{
	    	ClientResponse confResp = client
	    			.resource(SERVICE_URI)
	    			.type("text/turtle")
	    			.post(ClientResponse.class, configFile.get(OmnomTestResources.DEMO_SERVICE_ILLEGAL_PARAMETER));
	    	String confRespStr = confResp.getEntity(String.class);
	    	log.info(confRespStr);
	    	assertEquals(400, confResp.getStatus());
	    	assertThat(confRespStr, containsString(ErrorMsg.ILLEGAL_PARAMETER_VALUE.toString()));
    	}
    }

    @Ignore("Refactor this")
    @Test
    public void testDemo() {
    	
        WebservicePojo ws = new WebservicePojo();
        ws.loadFromURI(SERVICE_URI);
        WebserviceConfigPojo config = new WebserviceConfigPojo();
        config.setWebservice(ws);
//        config.setId(SERVICE_URI + "/" + )
        client.publishPojoToConfigService(config);
        config.addParameterAssignment("sleeptime", "2");
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
