package eu.dm2e.ws.services.mint;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.query.ResultSet;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlSelect;

public class MintFileServiceITCase extends OmnomTestCase {

	public static String SERVICE_URI;

	private String randomFileUri;
	private FilePojo randomFilePojo;

	@Before
	public void setUp() throws Exception {
		// System.setProperty("http.keepAlive", "false");
		SERVICE_URI  = URI_BASE + "mint-file";
		chooseRandomFile();
	}

	private void chooseRandomFile()
			throws IOException {
		GrafeoImpl listG = new GrafeoImpl();
		log.debug(SERVICE_URI);
		Response resp = client.target(SERVICE_URI).request().get();
		assertEquals(200, resp.getStatus());
		listG.readHeuristically(resp.readEntity(InputStream.class));
		log.info("Choose random file");
		ResultSet iter = new SparqlSelect.Builder()
				.select("?s")
				.where("?s <" + NS.RDF.PROP_TYPE + "> <" + NS.OMNOM.CLASS_FILE + ">")
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
		Response resp = client.target(SERVICE_URI).request().get();
		log.info("GET /mint-file response: " + resp);
		assertEquals(200, resp.getStatus());
		GrafeoImpl respG = new GrafeoImpl();
		respG.readHeuristically(resp.readEntity(InputStream.class));
		assertThat(respG.findByClass(NS.OMNOM.CLASS_FILE).size(), greaterThan(10));
		assertTrue("Contains at least one XML file", respG.containsTriple("?s",
				"?p",
				NS.OMNOM_TYPES.XML));
		assertTrue("Contains at least one TGZ-XML file", respG.containsTriple("?s",
				"?p",
				NS.OMNOM_TYPES.TGZ_XML));
		assertTrue("Contains at least one XSLT file", respG.containsTriple("?s",
				"?p",
				NS.OMNOM_TYPES.XSLT));
	}

	@Test
	public void testGetFileDataByUriConvert()
			throws UnsupportedEncodingException {
		// http://mint-projects.image.ntua.gr/dm2e/Download?datasetId=1059
		log.info("Get file data converting TGZ to XML");

		String reqUri = SERVICE_URI + "/dataByURI?uri="
				+ URLEncoder.encode(SERVICE_URI + "/upload" + 1059, "UTF-8");
		log.info("REQUESTING: " + reqUri);

		client.getJerseyClient().property(ClientProperties.FOLLOW_REDIRECTS, false);
		Response resp = client.target(reqUri).request().get();
		client.getJerseyClient().property(ClientProperties.FOLLOW_REDIRECTS, true);

		assertEquals(200, resp.getStatus());
		assertEquals(MediaType.APPLICATION_XML_TYPE, resp.getMediaType());
	}

	@Test
	public void testGetFileDataByUri() throws UnsupportedEncodingException {
		log.info("Get file data 303");

		String reqUri = SERVICE_URI + "/dataByURI?uri=" + URLEncoder.encode(randomFileUri, "UTF-8");
		log.info("REQUESTING: " + reqUri);

		client.getJerseyClient().property(ClientProperties.FOLLOW_REDIRECTS, false);
		Response resp = client.target(reqUri).request().get();
		client.getJerseyClient().property(ClientProperties.FOLLOW_REDIRECTS, true);

		final String respStr = resp.readEntity(String.class);
		log.info("" + respStr);
		assertEquals(303, resp.getStatus());
		assertEquals(randomFilePojo.getFileRetrievalURI(), resp.getLocation());
	}

	@Test
	public void testGetFileMetadataByUriJson() {
		log.info("Get file metadata as JSON");
		Response respMetadataJson = client
			.target(randomFileUri.toString())
			.request(MediaType.APPLICATION_JSON_TYPE)
			.get();
		assertEquals(200, respMetadataJson.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, respMetadataJson.getMediaType());
		assertEquals(randomFilePojo.toJson(), respMetadataJson.readEntity(String.class));
//		log.info(respMetadataJson.readEntity(String.class));
	}

	@Test
	public void testGetFileMetadataByUriRdf()
			throws IOException {
		log.info("Get file metadata as RDF");
		Response respMetadataRdf = client
			.target(randomFileUri.toString())
			.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
			.get();
		assertEquals(200, respMetadataRdf.getStatus());
		GrafeoImpl fileG = new GrafeoImpl();
		fileG.readHeuristically(respMetadataRdf.readEntity(InputStream.class));
		FilePojo actualFP = fileG.getObjectMapper().getObject(FilePojo.class, randomFileUri);
		log.info(fileG.getTerseTurtle());
		assertEquals(randomFilePojo.getId(), actualFP.getId());
		assertEquals(randomFilePojo.getLabel(), actualFP.getLabel());
		assertEquals(randomFilePojo.getModified(), actualFP.getModified());
	}

}
