package eu.dm2e.ws.tests.integration.services.demo;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoMongoImpl;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.tests.OmnomTestCase;
import eu.dm2e.ws.tests.OmnomTestResources;
import eu.dm2e.ws.wsmanager.ManageService;

/**
 * This file was created within the DM2E project. http://dm2e.eu
 * http://github.com/dm2e
 * 
 * Author: Kai Eckert, Konstantin Baierer
 */
public class DemoServiceITCase extends OmnomTestCase {

	String SERVICE_URI;

	@Before
	public void setUp() throws Exception {
		SERVICE_URI = URI_BASE + "service/demo";
		ManageService.startAll();
	}

	@Test
	@Ignore("DemoService has no more 'test' path")
	public void testPostReader() {
		FilePojo fp = new FilePojo();
		fp.setId("http://foo");
		{
			Response resp = client
					.target(SERVICE_URI)
					.path("test")
					.request(MediaType.APPLICATION_JSON)
					.post(fp.toJsonEntity());
			log.debug(resp.getStatusInfo().getReasonPhrase());
			assertEquals(200, resp.getStatus());
			final String respStr = resp.readEntity(String.class);
			log.debug("Response body: " + respStr);
		}
		{
			Response resp = client
					.target(SERVICE_URI)
					.path("test")
					.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.post(fp.getNTriplesEntity());
			log.debug(resp.getStatusInfo().getReasonPhrase());
			assertEquals(200, resp.getStatus());
			final String respStr = resp.readEntity(String.class);
			log.debug("Response body: " + respStr);
		}
	}

	@Test
	public void testDescription() {

		log.info(SERVICE_URI);
		Response resp = client.getJerseyClient().target(SERVICE_URI).request("text/turtle").get();
		String respStr = resp.readEntity(String.class);
		log.info(respStr);
		assertEquals(200, resp.getStatus());
		Grafeo g = new GrafeoMongoImpl();
		g.readHeuristically(respStr);
		log.info(g.getTurtle());
		assertTrue(g.containsTriple(SERVICE_URI, "rdf:type", "omnom:Webservice"));
		assertTrue(g.containsTriple(SERVICE_URI, NS.OMNOM.PROP_INPUT_PARAM, SERVICE_URI
				+ "/param/sleeptime"));
		assertTrue(g
				.containsTriple(SERVICE_URI + "/param/sleeptime", "rdf:type", "omnom:Parameter"));
		assertTrue(g.containsTriple(SERVICE_URI + "/param/sleeptime", "omnom:parameterType",
				g.literal(g.expand("xsd:int"))));
	}

	@Test
	public void testPut() throws InterruptedException {
		{
			Response confResp = client
					.getConfigWebTarget()
					.request(DM2E_MediaType.TEXT_TURTLE)
					.post(Entity.entity(
							configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING),
							DM2E_MediaType.TEXT_TURTLE));
			String confRespStr = confResp.readEntity(String.class);
			log.info("testPut: " + confRespStr);
			assertEquals(201, confResp.getStatus());
			log.info("POST finished successfully.");
			URI confLoc = confResp.getLocation();
			assertNotNull(confLoc);
			log.info("POST returned " + confLoc);
			log.info("Beginning PUT");
			Response serviceResp = client
					.target(SERVICE_URI)
					.request()
					.put(Entity.text(confLoc.toString()));
//			SparqlConstruct sparco = new SparqlConstruct.Builder()
//					.endpoint(Config.get(ConfigProp.MONGO))
//					.graph("?g")
//					.construct("?s ?p ?o")
//					.build();
//			GrafeoMongoImpl testG = new GrafeoMongoImpl();
//			sparco.execute(testG);
//			log.error(testG.getTerseTurtle());

			assertEquals(202, serviceResp.getStatus());
			log.info("PUT finished");
			log.info("Beginning GET");
			URI jobLoc = serviceResp.getLocation();
			JobPojo job = new JobPojo(jobLoc);
			assertNotNull(job.getId());
			log.info(job.getTurtle());
		}
	}

	// @Ignore("TODO")
	@Test
	public void testPostIllegal() {
		Response confResp = client
				.target(SERVICE_URI)
				.request()
				.post(Entity.entity(
						configFile.get(OmnomTestResources.DEMO_SERVICE_ILLEGAL_PARAMETER),
						DM2E_MediaType.TEXT_TURTLE));
		String confRespStr = confResp.readEntity(String.class);
		log.info("testPostIllegal: " + confRespStr);
		assertEquals(400, confResp.getStatus());
		assertThat(confRespStr, containsString(ErrorMsg.ILLEGAL_PARAMETER_VALUE.toString()));
	}

	@Test
	public void testDemo() {

		WebservicePojo ws = new WebservicePojo();
		try {
			ws.loadFromURI(SERVICE_URI);
		} catch (Exception e1) {
			log.error("Could reload job pojo." + e1);
			e1.printStackTrace();
		}
		WebserviceConfigPojo config = new WebserviceConfigPojo();
		config.setWebservice(ws);
		// config.setId(SERVICE_URI + "/" + )
		// client.publishPojoToConfigService(config);
		// config.addParameterAssignment("sleeptime", "2");
		client.publishPojoToConfigService(config);

		log.info("Configuration created for Test: " + config.getTurtle());

		Response response = client.target(SERVICE_URI).request().put(Entity.text(config.getId()));
		log.info("JOB STARTED WITH RESPONSE: " + response.getStatus() + " / Location: "
				+ response.getLocation() + " / Content: " + response.readEntity(String.class));
		assertEquals(202, response.getStatus());
		URI joburi = response.getLocation();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException("An exception occurred: " + e, e);
		}
		Grafeo g = new GrafeoMongoImpl(joburi.toString());
		JobPojo job = g.getObjectMapper().getObject(JobPojo.class, joburi.toString());
		String status = job.getJobStatus();
		log.info("Status after 1 seconds: " + status);
		assert (status.equals(JobStatus.STARTED.name()));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException("An exception occurred: " + e, e);
		}
		g = new GrafeoMongoImpl(joburi.toString());
		job = g.getObjectMapper().getObject(JobPojo.class, joburi.toString());
		status = job.getJobStatus();
		log.info("Status after 4 seconds: " + status);
		log.info(job.toLogString());
		assert (status.equals(JobStatus.FINISHED.name()));

	}

}
