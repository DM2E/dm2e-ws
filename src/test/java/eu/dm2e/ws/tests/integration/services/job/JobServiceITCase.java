package eu.dm2e.ws.tests.integration.services.job;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.grafeo.junit.GrafeoAssert;
import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.*;
import eu.dm2e.ws.api.*;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.services.demo.DemoService;
import eu.dm2e.ws.services.xslt.XsltService;
import eu.dm2e.ws.tests.OmnomTestCase;
import eu.dm2e.ws.tests.OmnomTestResources;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class JobServiceITCase extends OmnomTestCase {
	private WebTarget webTarget;
	private URI globalJob;
	String[] logLevels = {"TRACE", "DEBUG", "INFO", "WARN", "FATAL"};
	

	@Before
	public void setUp() throws Exception {

		webTarget = client.getJerseyClient().target(URI_BASE).path("job");
//		log.info(configString.get(OmnomTestResources.DEMO_JOB));
		globalJob = postConfigToJob(OmnomTestResources.DEMO_JOB).getIdAsURI();
//				.type(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
//				.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
		Response resp = webTarget
				.request()
				.post(Entity.text(configString.get(OmnomTestResources.DEMO_JOB)));
		log.info(LogbackMarkers.HTTP_RESPONSE_DUMP, resp.readEntity(String.class));
		assertEquals(201, resp.getStatus());
		globalJob = resp.getLocation();
	}
	
	private static JobPojo postConfigToJob(OmnomTestResources testRes) {
		return postConfigToJob(configString.get(testRes));
	}
	private static JobPojo postConfigToJob(String rdfString) {
		Response resp = client.getJobWebTarget().request().post(Entity.text(rdfString));
		assertEquals(201, resp.getStatus());
		assertNotNull(resp.getLocation());
		return new JobPojo(resp.getLocation());
	}
	

	@Test
	public void testGetWebServicePojo() {
		Response resp = webTarget.request().get();
		Grafeo g = new GrafeoImpl(resp.readEntity(InputStream.class));
		log.info(g.getNTriples());
		GrafeoAssert.sizeEquals(g, 3);
	}

	/**
	 *  TODO BUG what's the deal with this test breaking randomly???
	 *  I guess the bug is in passing an InputStream to GrafeoImpl constructor [kb, Jul 12, 2013 12:11:34 AM]
	 *  
	 * @throws InterruptedException 
	 */
//	@Ignore("This will break randomly. Ignore it for now.")
	@Test
	public void testGetJob() throws InterruptedException {
		WebTarget wr = client.getJerseyClient().target(globalJob);
		Response resp = wr.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES).get();
		Grafeo g = new GrafeoImpl(resp.readEntity(String.class), true);
		// TODO wtf? this breaks randomly, returning no results when running the full test suite but not when running just this test case... WTF?
		try {
			GrafeoAssert.containsLiteral(g, globalJob, NS.OMNOM.PROP_JOB_STATUS, "NOT_STARTED");
		} catch (Throwable e) {
			throw new RuntimeException(g.getTerseTurtle() + e);
		}
	}

	@Test
	public void testNewJob() {
		Response resp = webTarget.request().post(Entity.text(configString.get(OmnomTestResources.DEMO_JOB)));
		assertEquals(201, resp.getStatus());
	}

	@Test
	public void testGetJobStatus() throws URISyntaxException {
		WebTarget wr = client.getJerseyClient().target(globalJob).path("status");
		Response resp = wr.request().get();
		assertEquals("NOT_STARTED", resp.readEntity(String.class));
	}

	@Test
	public void testUpdateJobStatus() throws URISyntaxException {
		WebTarget wr = client.getJerseyClient().target(globalJob).path("status");
		{
//			String newStatus = "STARTED";
//			Response resp1 = wr.request().put(, newStatus);
//			log.info("Status response: " + resp1);
//			assertEquals(201, resp1.getStatus());
			Response resp2 = wr.request().get();
			log.info(resp2.readEntity(String.class));
//			assertEquals(newStatus, resp2.readEntity(String.class));
		}
		{
			String badStatus = "";
			Response resp1 = wr.request().put(Entity.text( badStatus));
			assertEquals(400, resp1.getStatus());
			assertEquals(ErrorMsg.NO_JOB_STATUS.toString(), resp1.readEntity(String.class));
		}
		{
			String badStatus = "XYZZY";
			Response resp1 = wr.request().put( Entity.text( badStatus));
			assertEquals(400, resp1.getStatus());
			assertEquals("XYZZY: " + ErrorMsg.INVALID_JOB_STATUS.toString(), resp1.readEntity(String.class));
		}
	}

	@Test
	public void testAddLogEntryAsRDF() throws URISyntaxException {
//		URI logURI = new URI(globalJob.toString() + "/log");
		Response resp = client.getJerseyClient()
				.target(globalJob)
				.path("log")
				.request()
				.post(Entity.entity(configString.get(OmnomTestResources.DEMO_LOG), DM2E_MediaType.TEXT_TURTLE));
		assertEquals(201, resp.getStatus());
		URI logLoc = resp.getLocation();
		
		GrafeoImpl g = new GrafeoImpl(globalJob);
		GrafeoAssert.containsLiteral(g, logLoc, "omnom:hasLogMessage", "I'ma log message in RDF");
		
		Response respBad = client.getJerseyClient()
				.target(globalJob)
				.path("log")
				.request()
				.post(Entity.entity(configString.get(OmnomTestResources.DEMO_LOG_WITH_URI), DM2E_MediaType.TEXT_TURTLE));
		assertEquals(400, respBad.getStatus());
		assertEquals(ErrorMsg.NO_TOP_BLANK_NODE.toString(), respBad.readEntity(String.class));
	}

	@Test
	public void testAddLogEntryAsText() throws URISyntaxException {
//		URI logURI = new URI(globalJob.toString() + "/log");
		Response resp = client.getJerseyClient()
				.target(globalJob)
				.path("log")
				.request().post(Entity.text("FOO BAR"));
		assertEquals(201, resp.getStatus());
		URI logLoc = resp.getLocation();
		
		GrafeoImpl g = new GrafeoImpl(globalJob);
		GrafeoAssert.containsLiteral(g, logLoc, "omnom:hasLogMessage", "FOO BAR");
	}
	@Test
	public void testAddLogEntryAsTextAndParse() throws URISyntaxException {
		for (String level: logLevels) {
			Response jobResp = client.getJerseyClient()
					.target(URI_BASE)
					.path("job")
					.request().post(Entity.text( configString.get(OmnomTestResources.DEMO_JOB)));
			assertEquals(201, jobResp.getStatus());
			URI jobLoc = jobResp.getLocation();
			Response logResp = client.getJerseyClient()
					.target(jobLoc)
					.path("log")
					.request().post(Entity.text( level + ": FOO"));
			assertEquals(201, logResp.getStatus());
			URI logLoc = logResp.getLocation();
			GrafeoImpl g = new GrafeoImpl(jobLoc);
//			GrafeoAssert.containsResource(g, jobLoc, NS.OMNOM.PROP_LOG_ENTRY, logLoc);
			log.info(g.getTurtle());
			GrafeoAssert.containsLiteral(g, logLoc, NS.OMNOM.PROP_LOG_MESSAGE, "FOO");
			GrafeoAssert.containsLiteral(g, logLoc, NS.OMNOM.PROP_LOG_LEVEL, level);
		}
	}

	@Test
	public void testListLogEntries() throws IOException {
		Response jobResp = client.getJerseyClient()
				.target(URI_BASE)
				.path("job")
				.request().post(Entity.text(configString.get(OmnomTestResources.DEMO_JOB)));
		assertEquals(201, jobResp.getStatus());
		URI jobLoc = jobResp.getLocation();
		
		for (String level: logLevels) {
			for (int i = 0; i < 2; i++) {
				Response logResp = client.getJerseyClient()
					.target(jobLoc)
					.path("log")
					.request()
					.post(Entity.text(level + ": FOO"));
				assertEquals(201, logResp.getStatus());
			}
		}
		{
			GrafeoImpl g = new GrafeoImpl(jobLoc);
			log.info("There should be 10 messages total");
			GrafeoAssert.numberOfResourceStatements(g, 10, jobLoc, NS.OMNOM.PROP_LOG_ENTRY, null);
		}
		{
			 Response resp = client.target(jobLoc)
					.path("log")
					.queryParam("minLevel", "INFO")
					.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.get();
			 String respStr = resp.readEntity(String.class);
			 log.info("ERRRORRRR: " + resp  + respStr);
			 GrafeoImpl g = new GrafeoImpl(respStr, true);
//			 assertEquals("6 of them are larger than INFO.",
			 GrafeoAssert.numberOfResourceStatements(g, 6, null, "rdf:type", "omnom:LogEntry");
		}
		{
			 InputStream logNT = client.getJerseyClient()
					.target(jobLoc)
					.path("log")
					.queryParam("minLevel", "INFO")
					.queryParam("maxLevel", "WARN")
					.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.get(InputStream.class);
			GrafeoImpl g = new GrafeoImpl(logNT);
			log.info("4 of them are between INFO and WARN");
			GrafeoAssert.numberOfResourceStatements(g, 4, null, "rdf:type", "omnom:LogEntry");
		}
		{
			 InputStream logNT = client.getJerseyClient()
					.target(jobLoc)
					.path("log")
					.queryParam("maxLevel", "DEBUG")
					.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.get(InputStream.class);
			GrafeoImpl g = new GrafeoImpl(logNT);
			log.info("4 of them are smaller than INFO.");
			GrafeoAssert.numberOfResourceStatements(g, 4, null, "rdf:type", "omnom:LogEntry");
		}
	}

	@Test
	public void testListLogEntriesAsLogFile() {
		Response jobResp = client.getJerseyClient()
				.target(URI_BASE)
				.path("job")
				.request().post(Entity.text( configString.get(OmnomTestResources.DEMO_JOB)));
		assertEquals(201, jobResp.getStatus());
		URI jobLoc = jobResp.getLocation();
		log.info("JOB URI: " + jobLoc);
		for (int i = 0; i < 10; i++) {
			Response logResp = client.getJerseyClient()
					.target(jobLoc)
					.path("log")
					.request().post(Entity.text("FOO"));
			log.info("Log post response: " + logResp);
			assertEquals(201, logResp.getStatus());
		}
		String logStr = client.target(jobLoc)
				.path("log")
				.request(DM2E_MediaType.TEXT_X_LOG)
				.get(String.class);
		String[] lines = logStr.split("\r\n|\r|\n");
		log.info("Log: " + logStr);
		assertEquals("Log should be 10 lines long", 10, lines.length);
	}
	
	@Test
	public void testListLogEntriesAsLogFileFromJob() {
		Response jobResp = client.getJerseyClient()
				.target(URI_BASE)
				.path("job")
				.request().post(Entity.text( configString.get(OmnomTestResources.DEMO_JOB)));
		assertEquals(201, jobResp.getStatus());
		URI jobLoc = jobResp.getLocation();
		String logStr = client.getJerseyClient()
				.target(jobLoc)
				.path("log")
				.request("text/x-log")
				.get(String.class);
		String logStrFromJob = client.getJerseyClient()
				.target(jobLoc)
				.request("text/x-log")
				.get(String.class);
		assertEquals("GET CT text/xlog on the job should yield the same", logStr, logStrFromJob);
	}
	
	@Test
	public void testPojoPublishStatus() throws Exception {
		JobPojo job = new JobPojo();
		job.publishToService(client.getJobWebTarget());
		{
			job.setFailed();
			String getJobNT = client.target(job.getId()).request(DM2E_MediaType.APPLICATION_RDF_TRIPLES).get(String.class);
			GrafeoImpl getjobGrafeo = new GrafeoImpl(getJobNT, true);
			log.info(getjobGrafeo.getTurtle());
			
			JobPojo getjob = getjobGrafeo.getObjectMapper().getObject(JobPojo.class, job.getId());
			assertEquals(JobStatus.FAILED.toString(), getjob.getJobStatus());
			client.target(job.getId()).path("status").request().put(Entity.text("FINISHED"));
			getjob.loadFromURI(job.getId());
			assertEquals(JobStatus.FINISHED.toString(), getjob.getJobStatus());
		}
	}
	
	@Test
	public void testWorkflowPojoPublish() {
		JobPojo job = new JobPojo();
		{
			assertThat(job.getId(), is(nullValue()));
			job.info("FOO");
			job.debug("FOO");
			job.trace("FOO");
			job.warn("FOO");
			job.fatal("FOO");
			assertThat(job.getId(), is(nullValue()));
			job.setStarted();
			job.setFailed();
			job.setFinished();
			assertThat(job.getId(), is(nullValue()));
			job.publishToService(client.getJobWebTarget());
			assertThat(job.getId(), not(nullValue()));
		}	
//		{
//			WorkflowJobPojo job2 = (WorkflowJobPojo) job.copy();
//			assertNotNull(job2);
//			assertNotNull(job2.getId());
//			assertEquals(JobStatus.FINISHED.toString(), job2.getStatus());
//			job2.setStatus(JobStatus.FAILED);
//			job2.publishToService();
//		}
	}
	
	@Test
	public void testPojoPublish() {
		JobPojo job = new JobPojo();
		{
			assertThat(job.getId(), is(nullValue()));
			job.info("FOO");
			job.debug("FOO");
			job.trace("FOO");
			job.warn("FOO");
			job.fatal("FOO");
			assertThat(job.getId(), is(nullValue()));
			job.setStarted();
			job.setFailed();
			job.setFinished();
			assertThat(job.getId(), is(nullValue()));
			job.publishToService(client.getJobWebTarget());
			assertThat(job.getId(), not(nullValue()));
		}
		
		
//		log.info(job.getId());
//		job.publishToService();
	}

	@Test
	public void testPojoPublishAssignment() {
		JobPojo job = new JobPojo();
		try {
			job.addOutputParameterAssignment("foo", "bar");
		} catch (Exception e) {
			log.info("This should fail because of missing webservice." + e);
			assertTrue("This should fail because of missing webservice.", true);
		}

		job.setWebService(new DemoService().getWebServicePojo());
		job.setWebserviceConfig(new WebserviceConfigPojo());
		job.getWebserviceConfig().setWebservice(job.getWebService());
		job.getWebserviceConfig().addParameterAssignment("sleeptime", "5");
		job.getWebserviceConfig().publishToService(client.getConfigWebTarget());

		try {
			job.addOutputParameterAssignment("foo", "bar");
			fail("This should fail, because 'foo' is not a valid output parameter.");
		} catch (Exception e) {
			log.error("" + e);
		}
		
		ParameterAssignmentPojo ass = job.addOutputParameterAssignment(DemoService.PARAM_RANDOM_OUTPUT, "bar");
		assertTrue("This should succeed.", true);

		assertThat(ass.getId(), is(nullValue()));
		assertTrue("There are blank nodes for the job and the assignments", job
			.getGrafeo()
			.listAnonResources()
			.size() > 0);
		Grafeo gJobBefore = job.getGrafeo();
		job.publishToService(client.getJobWebTarget());
		Grafeo gJobAfter = job.getGrafeo();
		GrafeoAssert.graphsAreStructurallyEquivalent(gJobBefore,gJobAfter);
		
		log.info(job.getTerseTurtle());
		assertEquals("No more blank nodes after publishing", 0, job.getGrafeo().listAnonResources().size());
//		
		

		ParameterAssignmentPojo ass1 = job.getInputParameterAssignments().iterator().next();
		assertThat("Assignment has a URI", ass1.getId(), not(nullValue()));
		log.info("" + job.getOutputParameterAssignments().iterator().next().getForParam());
		ParameterAssignmentPojo ass2 = job.getInputParameterAssignmentForParam("sleeptime");
		assertNotNull(ass1);
		assertNotNull(ass2);
		assertEquals("There should one assignment", ass1.getId(), ass2.getId());
	}
	
	@Test
	public void testGetAssignment() {
		JobPojo job = postConfigToJob(OmnomTestResources.DEMO_JOB);
		log.info(job.getTerseTurtle());
		final String forParam = XsltService.PARAM_XML_OUT;
		final String paramValue = "bar";
		job.addOutputParameterAssignment(forParam, paramValue);
		assertNull(job.getOutputParameterAssignments().iterator().next().getId());
		job.publishToService(client.getJobWebTarget());
		ParameterAssignmentPojo apiAss = job.getOutputParameterAssignments().iterator().next();
		{
			ParameterAssignmentPojo webAss = client.loadPojoFromURI(ParameterAssignmentPojo.class, apiAss.getId());
			GrafeoAssert.graphsAreEquivalent(apiAss.getGrafeo(), webAss.getGrafeo());
		}
		{
    		client.getJerseyClient().property(ClientProperties.FOLLOW_REDIRECTS, false);
			Response resp = client.target(apiAss.getId()).request().get();
			assertEquals(303, resp.getStatus());
		}
		{
    		client.getJerseyClient().property(ClientProperties.FOLLOW_REDIRECTS, true);
			Response resp = client.target(apiAss.getId()).request().get();
			assertEquals(200, resp.getStatus());
		}
			
	}

}
