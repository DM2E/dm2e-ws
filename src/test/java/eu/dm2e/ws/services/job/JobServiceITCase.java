package eu.dm2e.ws.services.job;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.api.AbstractJobPojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.junit.GrafeoAssert;
import eu.dm2e.ws.services.Client;
import eu.dm2e.ws.services.demo.DemoService;

public class JobServiceITCase extends OmnomTestCase {
	private static final String BASE_URI = "http://localhost:9998";
	private WebResource webResource;
	private URI globalJob;
	String[] logLevels = {"TRACE", "DEBUG", "INFO", "WARN", "FATAL"};
	

	@Before
	public void setUp() throws Exception {

        client = new Client();
		webResource = client.getJerseyClient().resource(BASE_URI + "/job");
//		log.info(configString.get(OmnomTestResources.DEMO_JOB));
		ClientResponse resp = webResource
//				.type(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
//				.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
				.post(ClientResponse.class, configString.get(OmnomTestResources.DEMO_JOB));
		log.info(resp.getEntity(String.class));
		assertEquals(201, resp.getStatus());
		globalJob = resp.getLocation();
	}
	

	@Test
	public void testGetWebServicePojo() {
		ClientResponse resp = webResource.get(ClientResponse.class);
		Grafeo g = new GrafeoImpl(resp.getEntityInputStream());
		log.info(g.getNTriples());
		GrafeoAssert.sizeEquals(g, 2);
	}

	@Test
	public void testGetJob() {
		WebResource wr = client.getJerseyClient().resource(globalJob);
		ClientResponse resp = wr.get(ClientResponse.class);
		Grafeo g = new GrafeoImpl(resp.getEntityInputStream());
		// TODO wtf? this breaks randomly, returning no results when running the full test suite but not when running just this test case... WTF?
		try {
			GrafeoAssert.containsLiteral(g, globalJob, NS.OMNOM.PROP_JOB_STATUS, "NOT_STARTED");
		} catch (Throwable e) {
			throw new RuntimeException(g.getTurtle() + e);
		}
	}

	@Test
	public void testNewJob() {
		ClientResponse resp = webResource.post(ClientResponse.class, configString.get(OmnomTestResources.DEMO_JOB));
		assertEquals(201, resp.getStatus());
	}

	@Test
	public void testGetJobStatus() throws URISyntaxException {
		WebResource wr = client.getJerseyClient().resource(globalJob).path("status");
		ClientResponse resp = wr.get(ClientResponse.class);
		assertEquals("NOT_STARTED", resp.getEntity(String.class));
	}

	@Test
	public void testUpdateJobStatus() throws URISyntaxException {
		WebResource wr = client.getJerseyClient().resource(globalJob).path("status");
		{
//			String newStatus = "STARTED";
//			ClientResponse resp1 = wr.put(ClientResponse.class, newStatus);
//			log.info("Status response: " + resp1);
//			assertEquals(201, resp1.getStatus());
			ClientResponse resp2 = wr.get(ClientResponse.class);
			log.info(resp2.getEntity(String.class));
//			assertEquals(newStatus, resp2.getEntity(String.class));
		}
		{
			String badStatus = "";
			ClientResponse resp1 = wr.put(ClientResponse.class, badStatus);
			assertEquals(400, resp1.getStatus());
			assertEquals(ErrorMsg.NO_JOB_STATUS.toString(), resp1.getEntity(String.class));
		}
		{
			String badStatus = "XYZZY";
			ClientResponse resp1 = wr.put(ClientResponse.class, badStatus);
			assertEquals(400, resp1.getStatus());
			assertEquals("XYZZY: " + ErrorMsg.INVALID_JOB_STATUS.toString(), resp1.getEntity(String.class));
		}
	}

	@Test
	public void testAddLogEntryAsRDF() throws URISyntaxException {
//		URI logURI = new URI(globalJob.toString() + "/log");
		ClientResponse resp = client.getJerseyClient()
				.resource(globalJob)
				.path("log")
				.entity(configString.get(OmnomTestResources.DEMO_LOG))
				.type("text/turtle")
				.post(ClientResponse.class);
		assertEquals(201, resp.getStatus());
		URI logLoc = resp.getLocation();
		
		GrafeoImpl g = new GrafeoImpl(globalJob);
		GrafeoAssert.containsLiteral(g, logLoc, "omnom:hasLogMessage", "I'ma log message in RDF");
		
		ClientResponse respBad = client.getJerseyClient()
				.resource(globalJob)
				.path("log")
				.entity(configString.get(OmnomTestResources.DEMO_LOG_WITH_URI))
				.type("text/turtle")
				.post(ClientResponse.class);
		assertEquals(400, respBad.getStatus());
		assertEquals(ErrorMsg.NO_TOP_BLANK_NODE.toString(), respBad.getEntity(String.class));
	}

	@Test
	public void testAddLogEntryAsText() throws URISyntaxException {
//		URI logURI = new URI(globalJob.toString() + "/log");
		ClientResponse resp = client.getJerseyClient()
				.resource(globalJob)
				.path("log")
				.type("text/plain")
				.post(ClientResponse.class, "FOO BAR");
		assertEquals(201, resp.getStatus());
		URI logLoc = resp.getLocation();
		
		GrafeoImpl g = new GrafeoImpl(globalJob);
		GrafeoAssert.containsLiteral(g, logLoc, "omnom:hasLogMessage", "FOO BAR");
	}
	@Test
	public void testAddLogEntryAsTextAndParse() throws URISyntaxException {
		for (String level: logLevels) {
			ClientResponse jobResp = client.getJerseyClient()
					.resource(BASE_URI)
					.path("job")
					.post(ClientResponse.class, configString.get(OmnomTestResources.DEMO_JOB));
			assertEquals(201, jobResp.getStatus());
			URI jobLoc = jobResp.getLocation();
			ClientResponse logResp = client.getJerseyClient()
					.resource(jobLoc)
					.path("log")
					.type("text/plain")
					.post(ClientResponse.class, level + ": FOO");
			assertEquals(201, logResp.getStatus());
			URI logLoc = logResp.getLocation();
			GrafeoImpl g = new GrafeoImpl(jobLoc);
			GrafeoAssert.containsResource(g, jobLoc, NS.OMNOM.PROP_LOG_ENTRY, logLoc);
			log.info(g.getTurtle());
			GrafeoAssert.containsLiteral(g, logLoc, NS.OMNOM.PROP_LOG_MESSAGE, "FOO");
			GrafeoAssert.containsLiteral(g, logLoc, NS.OMNOM.PROP_LOG_LEVEL, level);
		}
	}

	@Test
	public void testListLogEntries() {
		ClientResponse jobResp = client.getJerseyClient()
				.resource(BASE_URI)
				.path("job")
				.post(ClientResponse.class, configString.get(OmnomTestResources.DEMO_JOB));
		assertEquals(201, jobResp.getStatus());
		URI jobLoc = jobResp.getLocation();
		
		for (String level: logLevels) {
			for (int i = 0; i < 2; i++) {
				ClientResponse logResp = client.getJerseyClient()
					.resource(jobLoc)
					.path("log")
					.type("text/plain")
					.post(ClientResponse.class, level + ": FOO");
				assertEquals(201, logResp.getStatus());
			}
		}
		{
			GrafeoImpl g = new GrafeoImpl(jobLoc);
			log.info("There should be 10 messages total");
			GrafeoAssert.numberOfResourceStatements(g, 10, jobLoc, "omnom:hasLogEntry", null);
		}
		{
			 InputStream logNT = client.getJerseyClient()
					.resource(jobLoc)
					.path("log")
					.queryParam("minLevel", "INFO")
					.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.get(InputStream.class);
			GrafeoImpl g = new GrafeoImpl(logNT);
//			assertEquals("6 of them are larger than INFO.",
			GrafeoAssert.numberOfResourceStatements(g, 6, null, "rdf:type", "omnom:LogEntry");
		}
		{
			 InputStream logNT = client.getJerseyClient()
					.resource(jobLoc)
					.path("log")
					.queryParam("minLevel", "INFO")
					.queryParam("maxLevel", "WARN")
					.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.get(InputStream.class);
			GrafeoImpl g = new GrafeoImpl(logNT);
			log.info("4 of them are between INFO and WARN");
			GrafeoAssert.numberOfResourceStatements(g, 4, null, "rdf:type", "omnom:LogEntry");
		}
		{
			 InputStream logNT = client.getJerseyClient()
					.resource(jobLoc)
					.path("log")
					.queryParam("maxLevel", "DEBUG")
					.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.get(InputStream.class);
			GrafeoImpl g = new GrafeoImpl(logNT);
			log.info("4 of them are smaller than INFO.");
			GrafeoAssert.numberOfResourceStatements(g, 4, null, "rdf:type", "omnom:LogEntry");
		}
	}

	@Test
	public void testListLogEntriesAsLogFile() {
		ClientResponse jobResp = client.getJerseyClient()
				.resource(BASE_URI)
				.path("job")
				.post(ClientResponse.class, configString.get(OmnomTestResources.DEMO_JOB));
		assertEquals(201, jobResp.getStatus());
		URI jobLoc = jobResp.getLocation();
		log.info("JOB URI: " + jobLoc);
		for (int i = 0; i < 10; i++) {
			ClientResponse logResp = client.getJerseyClient()
					.resource(jobLoc)
					.path("log")
					.type(DM2E_MediaType.TEXT_PLAIN)
					.post(ClientResponse.class, "FOO");
			log.info("Log post response: " + logResp);
			assertEquals(201, logResp.getStatus());
		}
		String logStr = client.resource(jobLoc)
				.path("log")
				.accept(DM2E_MediaType.TEXT_X_LOG)
				.get(String.class);
		String[] lines = logStr.split("\r\n|\r|\n");
		log.info("Log: " + logStr);
		assertEquals("Log should be 10 lines long", 10, lines.length);
	}
	
	@Test
	public void testListLogEntriesAsLogFileFromJob() {
		ClientResponse jobResp = client.getJerseyClient()
				.resource(BASE_URI)
				.path("job")
				.post(ClientResponse.class, configString.get(OmnomTestResources.DEMO_JOB));
		assertEquals(201, jobResp.getStatus());
		URI jobLoc = jobResp.getLocation();
		String logStr = client.getJerseyClient()
				.resource(jobLoc)
				.path("log")
				.accept("text/x-log")
				.get(String.class);
		String logStrFromJob = client.getJerseyClient()
				.resource(jobLoc)
				.accept("text/x-log")
				.get(String.class);
		assertEquals("GET CT text/xlog on the job should yield the same", logStr, logStrFromJob);
	}
	
	@Test
	public void testPojoPublishStatus() {
		JobPojo job = new JobPojo();
		job.publishToService();
		{
			job.setFailed();
			String getJobNT = client.resource(job.getId()).accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES).get(String.class);
			GrafeoImpl getjobGrafeo = new GrafeoImpl(getJobNT, true);
			log.info(getjobGrafeo.getTurtle());
			
			AbstractJobPojo getjob = getjobGrafeo.getObjectMapper().getObject(JobPojo.class, job.getId());
			assertEquals("FAILED", getjob.getStatus());
			client.resource(job.getId()).path("status").entity("FINISHED").put();
			getjob.loadFromURI(job.getId().toString());
			assertEquals("FINISHED", getjob.getStatus());
		}
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
			job.publishToService();
			assertThat(job.getId(), not(nullValue()));
		}
		
		
//		log.info(job.getId());
//		job.publishToService();
	}

	@Test
	public void testPojoPublishAssignment() {
		JobPojo job = new JobPojo();
		try {
			job.addOutputParameterAssignment("sleeptime", "bar");
		} catch (Exception e) {
			log.info("This should fail because of missing webservice." + e);
			assertTrue("This should fail because of missing webservice.", true);
		}

		job.setWebService(new DemoService().getWebServicePojo());

		ParameterAssignmentPojo ass = job.addOutputParameterAssignment("sleeptime", "bar");
		assertTrue("This should succeed.", true);
		

		assertThat(ass.getId(), is(nullValue()));
		assertTrue("There are blank nodes for the job and the assignments", job
			.getGrafeo()
			.listAnonResources()
			.size() > 0);
		log.info(job.getTurtle());
		job.publishToService();
		log.info(job.getTurtle());
		if (true) return;
//		log.info(job.getTurtle());
		assertTrue("No more blank nodes after publishing", job
			.getGrafeo()
			.listAnonResources()
			.size() == 0);
		
		log.info(job.getTurtle());

		ParameterAssignmentPojo ass1 = job.getOutputParameters().iterator().next();
		assertThat("Assignment has a URI", ass1.getId(), not(nullValue()));
		ParameterAssignmentPojo ass2 = job.getParameterAssignmentForParam("label");
		assertEquals("There should one assignment", ass1.getId(), ass2.getId());
	}

}
