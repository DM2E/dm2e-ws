package eu.dm2e.ws.services.job;

import static org.junit.Assert.assertEquals;
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
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.Client;

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
		ClientResponse resp = webResource.post(ClientResponse.class, configString.get(OmnomTestResources.DEMO_JOB));
//		log.info(resp.getEntity(String.class));
		assertEquals(201, resp.getStatus());
		globalJob = resp.getLocation();
	}
	

	@Test
	public void testGetWebServicePojo() {
		ClientResponse resp = webResource.get(ClientResponse.class);
		Grafeo g = new GrafeoImpl(resp.getEntityInputStream());
		assertEquals(2, g.size());
	}

	@Test
	public void testGetJob() {
		WebResource wr = client.getJerseyClient().resource(globalJob);
		ClientResponse resp = wr.get(ClientResponse.class);
		GrafeoImpl g = new GrafeoImpl(resp.getEntityInputStream());
		assertTrue(g.containsStatementPattern(globalJob.toString(), "omnom:status", g.literal("NOT_STARTED")));
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
			String newStatus = "STARTED";
			ClientResponse resp1 = wr.put(ClientResponse.class, newStatus);
			assertEquals(201, resp1.getStatus());
			ClientResponse resp2 = wr.get(ClientResponse.class);
			assertEquals(newStatus, resp2.getEntity(String.class));
		}
		{
			String badStatus = "";
			ClientResponse resp1 = wr.put(ClientResponse.class, badStatus);
			assertEquals(400, resp1.getStatus());
			assertEquals(ErrorMsg.NO_JOB_STATUS.getMessage(), resp1.getEntity(String.class));
		}
		{
			String badStatus = "XYZZY";
			ClientResponse resp1 = wr.put(ClientResponse.class, badStatus);
			assertEquals(400, resp1.getStatus());
			assertEquals("XYZZY: " + ErrorMsg.INVALID_JOB_STATUS.getMessage(), resp1.getEntity(String.class));
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
		assertTrue(g.containsStatementPattern(logLoc.toString(), "omnom:hasLogMessage", g.literal("I'ma log message in RDF")));
		
		ClientResponse respBad = client.getJerseyClient()
				.resource(globalJob)
				.path("log")
				.entity(configString.get(OmnomTestResources.DEMO_LOG_WITH_URI))
				.type("text/turtle")
				.post(ClientResponse.class);
		assertEquals(400, respBad.getStatus());
		assertEquals(ErrorMsg.NO_TOP_BLANK_NODE.getMessage(), respBad.getEntity(String.class));
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
		assertTrue(g.containsStatementPattern(logLoc.toString(), "omnom:hasLogMessage", g.literal("FOO BAR")));
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
			assertTrue(g.containsStatementPattern(jobLoc.toString(), "omnom:hasLogEntry", logLoc.toString()));
			log.info(g.getTurtle());
			assertTrue(g.containsStatementPattern(logLoc.toString(), "omnom:hasLogMessage", g.literal("FOO")));
			assertTrue(g.containsStatementPattern(logLoc.toString(), "omnom:hasLogLevel", g.literal(level)));
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
			assertEquals("There should be 10 messages total",
					10,
					g.listResourceStatements(jobLoc.toString(), "omnom:hasLogEntry", null).size());
		}
		{
			 InputStream logNT = client.getJerseyClient()
					.resource(jobLoc)
					.path("log")
					.queryParam("minLevel", "INFO")
					.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.get(InputStream.class);
			GrafeoImpl g = new GrafeoImpl(logNT);
			assertEquals("6 of them are larger than INFO.",
					6,
					g.listResourceStatements(null, "rdf:type", "omnom:LogEntry").size());
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
			assertEquals("4 of them are between INFO and WARN",
					4,
					g.listResourceStatements(null, "rdf:type", "omnom:LogEntry").size());
		}
		{
			 InputStream logNT = client.getJerseyClient()
					.resource(jobLoc)
					.path("log")
					.queryParam("maxLevel", "DEBUG")
					.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.get(InputStream.class);
			GrafeoImpl g = new GrafeoImpl(logNT);
			assertEquals("4 of them are smaller than INFO.",
					4,
					g.listResourceStatements(null, "rdf:type", "omnom:LogEntry").size());
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
		for (int i = 0; i < 10; i++) {
			ClientResponse logResp = client.getJerseyClient()
					.resource(jobLoc)
					.path("log")
					.type("text/plain")
					.post(ClientResponse.class, "FOO");
			assertEquals(201, logResp.getStatus());
		}
		String logStr = client.getJerseyClient()
				.resource(jobLoc)
				.path("log")
				.accept("text/x-log")
				.get(String.class);
		String[] lines = logStr.split("\r\n|\r|\n");
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

}
