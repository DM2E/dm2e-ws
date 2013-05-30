// <<<<<<< HEAD
package eu.dm2e.ws.services.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
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

public class DataServiceITCase {

	private static final String BASE_URI = "http://localhost:9998";
	Logger log = Logger.getLogger(getClass().getName());
	private Client client;
	private WebResource webResource;
	
	private Map<WSConf, String> configString = new HashMap<>();
	private Map<WSConf, File> configFile = new HashMap<>();
	private enum WSConf {
		DEMO_SERVICE_WORKING("/webserviceConfig/demo_config.ttl"),
		DEMO_SERVICE_BROKEN1("/webserviceConfig/demo_config_no_top_blank.ttl");
		
		final String path;
		WSConf(String path) { this.path = path; }
		String getPath() { return path; }
	};

	@Before
	public void setUp()
			throws Exception {
		client = new Client();
		webResource = client.getJerseyClient().resource(BASE_URI + "/data/configurations");
		for (WSConf wsconf : WSConf.values()) { 
			URL testConfigURL = this.getClass().getResource(wsconf.getPath());
			configFile.put(wsconf, new File(testConfigURL.getFile()));
			configString.put(wsconf, IOUtils.toString(testConfigURL.openStream()));
		}
	}
	
	@Test
	public void testPostBadSyntax() {
		ClientResponse resp = webResource.post(ClientResponse.class, "FOO");
		assertEquals(400, resp.getStatus());
		String respStr = resp.getEntity(String.class);
		assertThat(respStr, containsString("Bad RDF syntax"));
	}
	
	@Test
	public void testPostNoBlank() {
		ClientResponse resp = webResource.post(ClientResponse.class, configString.get(WSConf.DEMO_SERVICE_BROKEN1));
		assertEquals(400, resp.getStatus());
		String respStr = resp.getEntity(String.class);
		assertThat(respStr, containsString("No suitable top blank node."));
	}

	@Test
	public void testPostGoodSyntax() {
		Grafeo gOut = new GrafeoImpl(configFile.get(WSConf.DEMO_SERVICE_WORKING));
		ClientResponse resp = webResource.post(ClientResponse.class, gOut.getCanonicalNTriples());
		assertEquals(201, resp.getStatus());
		assertNotNull(resp.getLocation());
		File respFile = resp.getEntity(File.class);		
		Grafeo gIn = new GrafeoImpl(respFile);
		assertEquals("Contains no blank nodes", 0,  gIn.listAnonStatements(null, null, null).size());
		log.info(gIn.getCanonicalNTriples());
	}
    @Test
    public void testData() {
        ClientResponse response = webResource.post(ClientResponse.class, "[] <http://purl.org/dc/terms/creator> <http://localhost/kai>; <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://onto.dm2e.eu/omnom/WebServiceConfig> .");
        Grafeo g = new GrafeoImpl();
        g.readHeuristically(response.getEntity(String.class));
        Grafeo g2 = new GrafeoImpl();
        g2.addTriple(response.getLocation().toString(), "http://purl.org/dc/terms/creator", g.resource("http://localhost/kai"));
        g2.addTriple(response.getLocation().toString(), "rdf:type", g.resource("omnom:WebServiceConfig"));
        assert(g.isGraphEquivalent(g2));
        assertEquals(g.getCanonicalNTriples(), g2.getCanonicalNTriples());

        g2 = new GrafeoImpl();
        g2.addTriple(response.getLocation().toString(), "http://doesnotexist.org/bla", g.resource("http://localhost/kai"));
        g2.addTriple(response.getLocation().toString(), "rdf:type", g.resource("omnom:WebServiceConfig"));
        assertFalse(g.isGraphEquivalent(g2));

    }

}
// ||||||| merged common ancestors
// =======
// package eu.dm2e.ws.services.data;

// import com.sun.jersey.api.client.Client;
// import com.sun.jersey.api.client.ClientResponse;
// import com.sun.jersey.api.client.WebResource;
// import eu.dm2e.ws.grafeo.Grafeo;
// import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
// import org.junit.After;
// import org.junit.Before;
// import org.junit.Test;

// import java.util.logging.Logger;

// import static org.junit.Assert.assertFalse;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
 */
// public class DataServiceITCase {

        // Logger log = Logger.getLogger(getClass().getName());

        // private Client client;

        // @Before
        // public void setUp()
                // throws Exception {
            // client = new Client();
        // }

        // @After
        // public void tearDown() {
        // }

        // @Test
        // public void testData() {
            // // fail("Not yet implemented");
            // String URI_BASE = "http://localhost:9998";
            // WebResource webResource = client.resource(URI_BASE + "/data/configurations");
            // ClientResponse response = webResource.post(ClientResponse.class, "[] <http://purl.org/dc/terms/creator> <http://localhost/kai>; <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://onto.dm2e.eu/omnom/WebServiceConfig> .");
            // Grafeo g = new GrafeoImpl();
            // g.readHeuristically(response.getEntity(String.class));
            // Grafeo g2 = new GrafeoImpl();
            // g2.addTriple(response.getLocation().toString(), "http://purl.org/dc/terms/creator", g.resource("http://localhost/kai"));
            // g2.addTriple(response.getLocation().toString(), "rdf:type", g.resource("omnom:WebServiceConfig"));
            // assert(g.isGraphEquivalent(g2));

            // g2 = new GrafeoImpl();
            // g2.addTriple(response.getLocation().toString(), "http://doesnotexist.org/bla", g.resource("http://localhost/kai"));
            // g2.addTriple(response.getLocation().toString(), "rdf:type", g.resource("omnom:WebServiceConfig"));
            // assertFalse(g.isGraphEquivalent(g2));

        // }

// }
// >>>>>>> 68d300afc1582bdff27f84676ce8922e0d4ef67b
