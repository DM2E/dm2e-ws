package eu.dm2e.ws.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public class ClientITCase extends OmnomTestCase {
	
	Client client;

	@Before
	public void setUp() throws Exception {
		client = new Client();
	}

	@Ignore("TODO")
	@Test
	public void testPublishFile() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testGetJerseyClient() {
		com.sun.jersey.api.client.Client jc = client.getJerseyClient();
		assertNotNull(jc);
	}

	@Test
	public void testConfigJobFile() {
		Map<String, WebResource>uriToWR = new HashMap<>();
		uriToWR.put(Config.getString("dm2e.service.config.base_uri"), client.getConfigWebResource());
		uriToWR.put(Config.getString("dm2e.service.file.base_uri"), client.getFileWebResource());
		uriToWR.put(Config.getString("dm2e.service.job.base_uri"), client.getJobWebResource());
		for (Map.Entry<String, WebResource> entry : uriToWR.entrySet()) {
			String uri = entry.getKey();
			WebResource wr = entry.getValue();
			assertNotNull(wr);
			assertNotNull(uri);
			ClientResponse resp = wr.get(ClientResponse.class);
			assertEquals(200, resp.getStatus());
			Grafeo g = new GrafeoImpl(resp.getEntityInputStream());
			log.info(g.getTurtle());
			assertTrue(
					g.containsStatementPattern(uri,
					"rdf:type",
					"omnom:Webservice")
			);
		}
	}

	@Ignore("TODO")
	@Test
	public void testGetFileWebResource() {
		fail("Not yet implemented");
	}

	@Ignore("TODO")
	@Test
	public void testGetJobWebResource() {
		fail("Not yet implemented");
	}

}
