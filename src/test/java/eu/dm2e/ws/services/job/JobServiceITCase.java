package eu.dm2e.ws.services.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.Client;

public class JobServiceITCase {
	private static final String BASE_URI = "http://localhost:9998";
	Logger log = Logger.getLogger(getClass().getName());
	private Client client;
	private WebResource webResource;
	private URI globalJob;
	
	private Map<WSConf, String> configString = new HashMap<>();
	private Map<WSConf, File> configFile = new HashMap<>();
	private enum WSConf {
		DEMO_JOB("/job/demo_job.ttl");
		
		final String path;
		WSConf(String path) { this.path = path; }
		String getPath() { return path; }
	};

	@Before
	public void setUp() throws Exception {
		client = new Client();
		webResource = client.getJerseyClient().resource(BASE_URI + "/job");
		for (WSConf wsconf : WSConf.values()) { 
			URL testConfigURL = this.getClass().getResource(wsconf.getPath());
			configFile.put(wsconf, new File(testConfigURL.getFile()));
			configString.put(wsconf, IOUtils.toString(testConfigURL.openStream()));
		}
		ClientResponse resp = webResource.post(ClientResponse.class, configString.get(WSConf.DEMO_JOB));
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
		ClientResponse resp = webResource.post(ClientResponse.class, configString.get(WSConf.DEMO_JOB));
		assertEquals(201, resp.getStatus());
	}

	@Test
	public void testGetJobStatus() throws URISyntaxException {
		URI statusURI = new URI(globalJob.toString() + "/status");
		WebResource wr = client.getJerseyClient().resource(statusURI);
		ClientResponse resp = wr.get(ClientResponse.class);
		assertEquals("NOT_STARTED", resp.getEntity(String.class));
	}

	@Test
	public void testUpdateJobStatus() throws URISyntaxException {
		URI statusURI = new URI(globalJob.toString() + "/status");
		WebResource wr = client.getJerseyClient().resource(statusURI);
		String newStatus = "STARTED";
		ClientResponse resp1 = wr.put(ClientResponse.class, newStatus);
		assertEquals(201, resp1.getStatus());
		ClientResponse resp2 = wr.get(ClientResponse.class);
		assertEquals(newStatus, resp2.getEntity(String.class));
	}

//	@Test
//	public void testAddLogEntryAsRDF() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testAddLogEntryAsText() throws URISyntaxException {
		URI statusURI = new URI(globalJob.toString() + "/log");
		WebResource wr = client.getJerseyClient().resource(statusURI);
		ClientResponse resp = wr.post(ClientResponse.class, "FOO BAR");
		assertEquals(201, resp.getStatus());
		
		URI jobURI = new URI(globalJob.toString());
		WebResource wrJob = client.getJerseyClient().resource(jobURI);
		InputStream respJob = wrJob.get(InputStream.class);
		GrafeoImpl g = new GrafeoImpl(respJob);
		log.info(g.getTurtle());
		assertTrue(g.containsStatementPattern(null, "omnom:hasLogMessage", g.literal("FOO BAR")));
	}

//	@Test
//	public void testListLogEntries() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testListLogEntriesAsLogFile() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testListLogEntriesAsLogFileFromJob() {
//		fail("Not yet implemented");
//	}

}
