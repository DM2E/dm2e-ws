// <<<<<<< HEAD
package eu.dm2e.ws.services.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.Client;

public class ConfigServiceITCase extends OmnomTestCase{

	private static final String BASE_URI = "http://localhost:9998";
	Logger log = Logger.getLogger(getClass().getName());
	private Client client;
	private WebResource webResource;
	

	@Before
	public void setUp() throws Exception {
		client = new Client();
		webResource = client.getJerseyClient().resource(BASE_URI + "/config");
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
		ClientResponse resp = webResource.post(ClientResponse.class, configString.get(OmnomTestResources.DEMO_SERVICE_BROKEN1));
		assertEquals(400, resp.getStatus());
		String respStr = resp.getEntity(String.class);
		assertThat(respStr, containsString("No suitable top blank node."));
	}

	@Test
	public void testPostGoodSyntax() {
		Grafeo gOut = new GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING));
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
