package eu.dm2e.ws.services.mint;



import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.query.ResultSet;
import com.sun.jersey.api.client.ClientResponse;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.grafeo.jena.GResourceImpl;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlSelect;

public class MintFileServiceTest extends OmnomTestCase {
	
	public static final String SERVICE_URI = URI_BASE + "mint-file";
	
	@Before
	public void setUp() throws Exception {
//        System.setProperty("http.keepAlive", "false");
	}
	
	@Test
	public void testLogin() throws InterruptedException {
		MintFileService s = new MintFileService();
		assertFalse(s.isLoggedIn());
		s.ensureLoggedIn();
		log.info("Cookies: " + s.cookies);
		assertTrue(s.isLoggedIn());
	}
	
	@Test
	public void testGetFileList() throws InterruptedException, IOException {
		log.info(SERVICE_URI);
		ClientResponse resp = client.resource(SERVICE_URI).get(ClientResponse.class);
		log.info("GET /mint-file response: " + resp);
		assertEquals(200, resp.getStatus());
		GrafeoImpl respG = new GrafeoImpl();
		respG.readHeuristically(resp.getEntityInputStream());
		assertThat(respG.findByClass(NS.OMNOM.CLASS_FILE).size(), greaterThan(10));
		assertTrue("Contains at least one XSLT file", respG.containsTriple("?s", "?p", NS.OMNOM_TYPES.XSLT));
		assertTrue("Contains at least one DataUpload", respG.containsTriple("?s", "?p", NS.OMNOM_TYPES.TGZ));
	}
	
	@Test
	public void testGetSingleFile() throws IOException {
		log.info("Get file list");
		GrafeoImpl listG = new GrafeoImpl();
		GrafeoImpl fileG = new GrafeoImpl();
		{
			ClientResponse resp = client.resource(SERVICE_URI).get(ClientResponse.class);
			assertEquals(200, resp.getStatus());
			listG.readHeuristically(resp.getEntityInputStream());
		}
		
		log.info("Choose random file");
		ResultSet iter = new SparqlSelect.Builder()
			.select("?s")
			.where("?s <" + NS.RDF.PROP_TYPE +"> <" + NS.OMNOM.CLASS_FILE + ">")
        	.grafeo(listG)
        	.limit(1)
        	.build()
        	.execute();
        GResourceImpl randomFileUri = listG.resource(iter.next().get("?s").asResource().toString());
        log.info("Random file chosen: " + randomFileUri);
        
        log.info("Get file metadata");
        ClientResponse respMetadata = client.resource(randomFileUri.toString())
        		.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
        		.get(ClientResponse.class);
        assertEquals(200, respMetadata.getStatus());
        fileG.readHeuristically(respMetadata.getEntityInputStream());
        FilePojo shouldBeFP = listG.getObjectMapper().getObject(FilePojo.class, randomFileUri);
        FilePojo actualFP = fileG.getObjectMapper().getObject(FilePojo.class, randomFileUri);
        assertEquals(shouldBeFP.getId(), actualFP.getId());
        assertEquals(shouldBeFP.getLabel(), actualFP.getLabel());
        assertEquals(shouldBeFP.getLastModified(), actualFP.getLastModified());
        
        log.info("Get file data");
        client.getJerseyClient().setFollowRedirects(false);
        ClientResponse respData = client.resource(randomFileUri.toString())
        		.get(ClientResponse.class);
        client.getJerseyClient().setFollowRedirects(true);
        assertEquals(303, respData.getStatus());
        assertEquals(shouldBeFP.getFileRetrievalURI(), respData.getLocation());
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
