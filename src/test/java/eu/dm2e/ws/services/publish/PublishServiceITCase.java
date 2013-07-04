package eu.dm2e.ws.services.publish;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.api.AbstractJobPojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.model.JobStatus;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
 */
public class PublishServiceITCase extends OmnomTestCase {

    String SERVICE_URI;

    @Before
    public void setUp() throws Exception {
        SERVICE_URI = URI_BASE + "publish";
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDescription() {

        log.info(SERVICE_URI);
        Grafeo g = new GrafeoImpl(client
                .resource(SERVICE_URI)
                .accept("text/turtle")
                .get(InputStream.class));
        log.info(g.getNTriples());
        assertTrue(g.containsTriple(SERVICE_URI, NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_WEBSERVICE));
        assertTrue(g.containsTriple(SERVICE_URI, NS.OMNOM.PROP_INPUT_PARAM, SERVICE_URI + "/param/to-publish"));
        assertTrue(g.containsTriple(SERVICE_URI + "/param/to-publish", NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_PARAMETER));
        assertTrue(g.containsTriple(SERVICE_URI, NS.OMNOM.PROP_INPUT_PARAM, SERVICE_URI + "/param/dataset-id"));
        assertTrue(g.containsTriple(SERVICE_URI + "/param/dataset-id", NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_PARAMETER));
        assertTrue(g.containsTriple(SERVICE_URI, "omnom:inputParam", SERVICE_URI + "/param/provider-id"));
        assertTrue(g.containsTriple(SERVICE_URI + "/param/provider-id", "rdf:type", "omnom:Parameter"));
        assertTrue(g.containsTriple(SERVICE_URI, NS.OMNOM.PROP_INPUT_PARAM, SERVICE_URI + "/param/endpoint-select"));
        assertTrue(g.containsTriple(SERVICE_URI + "/param/endpoint-select", NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_PARAMETER));
        assertTrue(g.containsTriple(SERVICE_URI, NS.OMNOM.PROP_INPUT_PARAM, SERVICE_URI + "/param/endpoint-update"));
        assertTrue(g.containsTriple(SERVICE_URI + "/param/endpoint-update", NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_PARAMETER));
        assertTrue(g.containsTriple(SERVICE_URI, NS.OMNOM.PROP_INPUT_PARAM, SERVICE_URI + "/param/label"));
        assertTrue(g.containsTriple(SERVICE_URI + "/param/label", NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_PARAMETER));
        assertTrue(g.containsTriple(SERVICE_URI, NS.OMNOM.PROP_INPUT_PARAM, SERVICE_URI + "/param/comment"));
        assertTrue(g.containsTriple(SERVICE_URI + "/param/comment", NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_PARAMETER));
    }


    @Test
    public void testPublish1() throws Exception {

        WebservicePojo ws = new WebservicePojo();
        ws.loadFromURI(SERVICE_URI);
        WebserviceConfigPojo config = new WebserviceConfigPojo();
        OmnomTestResources xmlRes = OmnomTestResources.PUBLISH_RDF;
        try {
            String xmlUri = client.publishFile(configFile.get(xmlRes));


            config.setWebservice(ws);
            config.addParameterAssignment("to-publish", xmlUri);
            config.addParameterAssignment("dataset-id", "test-dataset");
            config.addParameterAssignment("provider-id", "dm2edev");
            config.addParameterAssignment("label", "Test-Dataset (from PublishServiceITCase, Publish1)");
            config.addParameterAssignment("comment", "This dataset can safely be deleted.");
            // config.addParameterAssignment("endpoint-update", "http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest/statements");
            // config.addParameterAssignment("endpoint-select", "http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest");
            config.publishToService(client.getConfigWebResource());

            log.info("Configuration created for Test: " + config.getTurtle());

            ClientResponse response = client
                    .resource(SERVICE_URI)
                    .type(DM2E_MediaType.TEXT_PLAIN)
                    .put(ClientResponse.class, config.getId());
            log.info("JOB STARTED WITH RESPONSE: " + response.getStatus() + " / Location: " + response.getLocation() + " / Content: " + response.getEntity(String.class));
//            if (response.getStatus() > 299) {
//            }
            URI joburi = response.getLocation();
            log.error("Job URI:" + joburi);
            
           /**
             * WAIT FOR JOB TO BE FINISHED
             */
            long i = 0 ,
	             maxTries = 100,
	             sleeptime = 1500;
            AbstractJobPojo job = new JobPojo(joburi);
			do {
            	if (i++ >= maxTries) {
            		fail("Publishing took more than" + (maxTries * sleeptime / 1000) + " seconds.");
            		break;
            	}
            	
                log.info("Check for status: " + job.getStatus());
                log.info("Loop [# " + i + "] JOB SO FAR: " + job.getTurtle());
                try {
                    Thread.sleep(sleeptime);
                } catch (InterruptedException e) {
                    throw new RuntimeException("An exception occurred: " + e, e);
                }
                job.loadFromURI(joburi);
            } while ( ! (job.isFinished() || job.isFailed()) );

            /**
             * CHECK IF JOB IS FINISHED
             */
            log.info("Status: " + job.getStatus());
            assertEquals(JobStatus.FINISHED.name(), job.getStatus());

        } catch (Exception e) {
        	log.info("" + e);
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }
    @Test
    public void testPublish2() throws Exception {

        WebservicePojo ws = new WebservicePojo();
        ws.loadFromURI(SERVICE_URI);
        WebserviceConfigPojo config = new WebserviceConfigPojo();
        OmnomTestResources xmlRes = OmnomTestResources.PUBLISH_RDF;
        try {
            String xmlUri = client.publishFile(configFile.get(xmlRes));


            config.setWebservice(ws);
            config.addParameterAssignment(PublishService.PARAM_TO_PUBLISH, xmlUri);
            config.addParameterAssignment(PublishService.PARAM_DATASET_ID, "test-dataset");
            config.addParameterAssignment("provider-id", "dm2edev");
            config.addParameterAssignment(PublishService.PARAM_LABEL, "Test-Dataset (from Integration Test)");
            config.addParameterAssignment(PublishService.PARAM_COMMENT, "This can safely be deleted.");
            // config.addParameterAssignment("endpoint-update", "http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest/statements");
            // config.addParameterAssignment("endpoint-select", "http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest");
            log.info("Configuration created for Test: " + config.getTurtle());
            config.publishToService(client.getConfigWebResource());


            ClientResponse response = client
                    .resource(SERVICE_URI)
                    .type(DM2E_MediaType.TEXT_PLAIN)
                    .put(ClientResponse.class, config.getId());
            log.info("JOB STARTED WITH RESPONSE: " + response.getStatus() + " / Location: " + response.getLocation() + " / Content: " + response.getEntity(String.class));
            URI joburi = response.getLocation();
            /**
             * WAIT FOR JOB TO BE FINISHED
             */

            String status = JobStatus.NOT_STARTED.name();
            AbstractJobPojo job = null;
            while (status.equals(JobStatus.NOT_STARTED.name()) || status.equals(JobStatus.STARTED.name())) {
                Grafeo g = new GrafeoImpl(joburi.toString());
                job = g.getObjectMapper().getObject(JobPojo.class, joburi.toString());
                status = job.getStatus();
                log.info("Check for status: " + status);
                log.info("JOB SO FAR: " + job.getTurtle());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException("An exception occurred: " + e, e);
                }
            }

            /**
             * CHECK IF JOB IS FINISHED
             */
            log.info("Status: " + status);
            assert (status.equals(JobStatus.FINISHED.name()));

        } catch (Exception e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }

}
