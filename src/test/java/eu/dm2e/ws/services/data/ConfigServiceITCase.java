// <<<<<<< HEAD
package eu.dm2e.ws.services.data;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.Client;

public class ConfigServiceITCase extends OmnomTestCase{

	private static final String BASE_URI = "http://localhost:9998";
	private Client client;
	private WebTarget webTarget;
	

	@Before
	public void setUp() throws Exception {
		client = new Client();
		webTarget = client.getJerseyClient().target(BASE_URI + "/config");
    }
	
	@Test
	public void testPostBadSyntax() {
		Response resp = webTarget.request().post(Entity.text("FOO"));
		assertEquals(400, resp.getStatus());
		String respStr = resp.readEntity(String.class);
		assertThat(respStr, containsString(ErrorMsg.BAD_RDF.toString()));
	}
	
	@Test
	public void testPostNoBlank() {
		Response resp = webTarget.request().post(Entity.text(configString.get(OmnomTestResources.DEMO_SERVICE_NO_TOP_BLANK)));
		assertEquals(400, resp.getStatus());
		String respStr = resp.readEntity(String.class);
		assertThat(respStr, containsString(ErrorMsg.NO_TOP_BLANK_NODE.toString()));
	}

	@Test
	public void testPostGoodSyntax() {
		Grafeo gOut = new GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING));
		Response resp = webTarget.request().post(Entity.text(gOut.getCanonicalNTriples()));
		assertEquals(201, resp.getStatus());
		assertNotNull(resp.getLocation());
		File respFile = resp.readEntity(File.class);		
		Grafeo gIn = new GrafeoImpl(respFile);
		assertEquals("Contains no blank nodes", 0,  gIn.listAnonStatements(null, null, null).size());
		log.info(gIn.getCanonicalNTriples());
	}
	
	@Test
	public void testPut() {
		Grafeo gOut = new GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING));
		Response respPOST = webTarget
				.request().post(gOut.getNTriplesEntity());
		assertEquals(201, respPOST.getStatus());
		assertNotNull(respPOST.getLocation());
		
		Response respGET1 = client
				.target(respPOST.getLocation())
				.request()
				.get();
		assertEquals(200, respGET1.getStatus());
		GrafeoImpl gGET1 = new GrafeoImpl(respGET1.readEntity(InputStream.class));
		assertEquals(gOut.size(), gGET1.size());
		
		Response respPUT = client
				.target(respPOST.getLocation())
				.request()
				.put(gOut.getNTriplesEntity());
		assertEquals(201, respPUT.getStatus());
		assertNotNull(respPUT.getLocation());
		assertEquals(respPOST.getLocation(), respPUT.getLocation());
		
		Response respGET2 = client
				.target(respPUT.getLocation())
				.request()
				.get();
		assertEquals(200, respGET2.getStatus());
		GrafeoImpl gGET2 = new GrafeoImpl(respGET2.readEntity(InputStream.class));
		assertEquals(gGET1.getCanonicalNTriples(), gGET2.getCanonicalNTriples());
//		assertTrue(gIn.isGraphEquivalent(gOut));
		
	}
	
	@Test
//	@Ignore("This is *not* a valid webserviceConfig because it omits a reference to the Webservice.")
	public void testData() {
		Response response = webTarget
			.request()
			.post(Entity.text(
					"[] <http://purl.org/dc/terms/creator> <http://localhost/kai>; <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://onto.dm2e.eu/omnom/WebserviceConfig> ; <http://onto.dm2e.eu/omnom/webservice> <http://localhost:9998/service/xslt>."
					));
        Grafeo g = new GrafeoImpl();
        final String confRespStr = response.readEntity(String.class);
        log.info(confRespStr);
		g.readHeuristically(confRespStr);
        log.info(g.getTurtle());
        Grafeo g2 = new GrafeoImpl();
        // NOTE: dc:creator isn't in a WebserviceConfigPojo so it will *not* be stored
        g2.addTriple(response.getLocation().toString(), NS.OMNOM.PROP_WEBSERVICE, g.resource("http://localhost:9998/service/xslt"));
        g2.addTriple(response.getLocation().toString(), "http://purl.org/dc/terms/creator", g.resource("http://localhost/kai"));
        g2.addTriple(response.getLocation().toString(), "rdf:type", g.resource(NS.OMNOM.CLASS_WEBSERVICE_CONFIG));
        assert(g.isGraphEquivalent(g2));
        assertEquals(g2.getCanonicalNTriples(), g.getCanonicalNTriples());

        g2 = new GrafeoImpl();
        g2.addTriple(response.getLocation().toString(), "http://doesnotexist.org/bla", g.resource("http://localhost/kai"));
        g2.addTriple(response.getLocation().toString(), "rdf:type", g.resource("omnom:WebServiceConfig"));
        assertFalse(g.isGraphEquivalent(g2));
    }
	
	@Test
	public void testValidate() {
		log.info("Validating Valid ...");
		{
			Grafeo gOut = new GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING));
			Response respPOST = client.getConfigWebTarget()
					.request().post(gOut.getNTriplesEntity());
			assertEquals(201, respPOST.getStatus());
			URI uri = respPOST.getLocation();
			assertNotNull(uri);
			String validateUri = uri.toString() + "/validate";
			Response resp = client.target(validateUri).request().get();
			assertEquals(200, resp.getStatus());
		}
		log.info("Validating Invalid ...");
		{
			Grafeo gOut = new GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_NO_TOP_BLANK));
			Response respPOST = client.getConfigWebTarget()
					.request().post(gOut.getNTriplesEntity());
			assertEquals(400, respPOST.getStatus());
			String respStr = respPOST.readEntity(String.class);
			assertThat(respStr, containsString(ErrorMsg.NO_TOP_BLANK_NODE.toString()));
		}
//		log.info("Validating Invalid ...");
//		{
//			Grafeo gOut = new GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_NO_TOP_BLANK));
//			Response respPOST = client.getConfigWebTarget()
//					.request().post(Response.class, gOut.getCanonicalNTriples());
//			assertEquals(400, respPOST.getStatus());
//			String respStr = respPOST.readEntity(String.class);
//			assertThat(respStr, containsString(ErrorMsg.NO_TOP_BLANK_NODE.toString()));
//		}
	}

}
