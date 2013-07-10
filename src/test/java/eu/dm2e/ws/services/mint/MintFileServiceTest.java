package eu.dm2e.ws.services.mint;


import com.hp.hpl.jena.query.ResultSet;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlSelect;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

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
		assertTrue("Contains at least one XML file", respG.containsTriple("?s", "?p", NS.OMNOM_TYPES.XML));
		assertTrue("Contains at least one TGZ-XML file", respG.containsTriple("?s", "?p", NS.OMNOM_TYPES.TGZ_XML));
		assertTrue("Contains at least one XSLT file", respG.containsTriple("?s", "?p", NS.OMNOM_TYPES.XSLT));
	}
	
	@Test
	public void testGetFileDataByUriConvert() throws UnsupportedEncodingException {
        // http://mint-projects.image.ntua.gr/dm2e/Download?datasetId=1059
        log.info("Get file data converting TGZ to XML");
        
        String reqUri = SERVICE_URI + "/dataByURI?uri=" + URLEncoder.encode(SERVICE_URI + "/upload" + 1059, "UTF-8");
        log.info("REQUESTING: " + reqUri);
        
        client.getJerseyClient().setFollowRedirects(false);
        ClientResponse resp = client.resource(reqUri)
        		.get(ClientResponse.class);
        client.getJerseyClient().setFollowRedirects(true);
        
        assertEquals(200, resp.getStatus());
        assertEquals(MediaType.APPLICATION_XML_TYPE, resp.getType());
	}
	
	@Test
	public void testGetFileDataByUri() throws UniformInterfaceException, UnsupportedEncodingException {
        log.info("Get file data 303");
        
        String reqUri = SERVICE_URI + "/dataByURI?uri=" + URLEncoder.encode(randomFileUri, "UTF-8");
        log.info("REQUESTING: " + reqUri);
        
        client.getJerseyClient().setFollowRedirects(false);
        ClientResponse resp = client.resource(reqUri)
        		.get(ClientResponse.class);
        client.getJerseyClient().setFollowRedirects(true);
        
        final String respStr = resp.getEntity(String.class);
        log.info("" + respStr);
        assertEquals(303, resp.getStatus());
        assertEquals(randomFilePojo.getInternalFileLocation(), resp.getLocation().toString());
	}

	@Test
	public void testGetFileMetadataByUriJson() {
		log.info("Get file metadata as JSON");
        ClientResponse respMetadataJson = client.resource(randomFileUri.toString())
        		.accept(MediaType.APPLICATION_JSON_TYPE)
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
        log.info(fileG.getTerseTurtle());
        assertEquals(randomFilePojo.getId(), actualFP.getId());
        assertEquals(randomFilePojo.getLabel(), actualFP.getLabel());
        assertEquals(randomFilePojo.getLastModified(), actualFP.getLastModified());
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
