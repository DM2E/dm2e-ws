package eu.dm2e.ws.services.mint;



import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.query.ResultSet;
import com.sun.jersey.api.client.ClientResponse;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlSelect;

public class MintFileServiceTest extends OmnomTestCase {
	
	public static final String SERVICE_URI = URI_BASE + "mint-file";
	
	private String randomFileUri;
	private FilePojo randomFilePojo;
	
	@Before
	public void setUp() throws Exception {
//        System.setProperty("http.keepAlive", "false");
		chooseRandomFile();
	}
	
	private void chooseRandomFile() throws IOException {
		GrafeoImpl listG = new GrafeoImpl();
		ClientResponse resp = client.resource(SERVICE_URI).get(ClientResponse.class);
		assertEquals(200, resp.getStatus());
		listG.readHeuristically(resp.getEntityInputStream());
		log.info("Choose random file");
		ResultSet iter = new SparqlSelect.Builder()
			.select("?s")
			.where("?s <" + NS.RDF.PROP_TYPE +"> <" + NS.OMNOM.CLASS_FILE + ">")
        	.grafeo(listG)
        	.limit(1)
        	.build()
        	.execute();
        randomFileUri = listG.resource(iter.next().get("?s").asResource().toString()).toString();
        log.info("Random file chosen: " + randomFileUri);
        randomFilePojo = listG.getObjectMapper().getObject(FilePojo.class, randomFileUri);
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
	public void testGetFileDataByUri() {
        log.info("Get file data");
        client.getJerseyClient().setFollowRedirects(false);
        ClientResponse respData = client.resource(randomFileUri.toString())
        		.get(ClientResponse.class);
        client.getJerseyClient().setFollowRedirects(true);
        assertEquals(303, respData.getStatus());
        assertEquals(randomFilePojo.getFileRetrievalURI(), respData.getLocation());
	}

	@Test
	public void testGetFileMetadataByUriJson() {
		log.info("Get file metadata as JSON");
        ClientResponse respMetadataJson = client.resource(randomFileUri.toString())
        		.accept(MediaType.APPLICATION_JSON)
        		.get(ClientResponse.class);
        assertEquals(200, respMetadataJson.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, respMetadataJson.getType());
        log.info(respMetadataJson.getEntity(String.class));
	}

	@Test
	public void testGetFileMetadataByUriRdf() throws IOException {
		log.info("Get file metadata as RDF");
        ClientResponse respMetadataRdf = client.resource(randomFileUri.toString())
        		.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
        		.get(ClientResponse.class);
        assertEquals(200, respMetadataRdf.getStatus());
		GrafeoImpl fileG = new GrafeoImpl();
        fileG.readHeuristically(respMetadataRdf.getEntityInputStream());
        FilePojo actualFP = fileG.getObjectMapper().getObject(FilePojo.class, randomFileUri);
        assertEquals(randomFilePojo.getId(), actualFP.getId());
        assertEquals(randomFilePojo.getLabel(), actualFP.getLabel());
        assertEquals(randomFilePojo.getLastModified(), actualFP.getLastModified());
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
