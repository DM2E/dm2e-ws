package eu.dm2e.ws.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public class ClientITCase extends OmnomTestCase {
	
	Client client;
	private static final String FILE_CONTENTS = "FILE_CONTENTS";
	private static final String ORIGINAL_NAME = "FOO.BAR";
	private FilePojo FILE_POJO = new FilePojo();
	private File TEMP_FILE;
	
	private boolean isCorrectlyPosted(String uri) {
		String respStr = client.resource(uri).get(String.class);
		return respStr.equals(FILE_CONTENTS);
	}

	private boolean isMetadataKept(String fileUri) {
		FilePojo fp = new FilePojo();
		try {
			fp.loadFromURI(fileUri);
		} catch (Exception e) {
			log.severe("Could reload job pojo." + e);
			e.printStackTrace();
			return false;
		}
		if (null == fp.getOriginalName()) {
			return false;
		}
		return fp.getOriginalName().equals(FILE_POJO.getOriginalName());
	}

	@Before
	public void setUp() throws Exception {

        client = new Client();
		FILE_POJO.setOriginalName(ORIGINAL_NAME);
		TEMP_FILE = File.createTempFile("omnom_test", "txt");
		IOUtils.copy(IOUtils.toInputStream(FILE_CONTENTS), new FileOutputStream(TEMP_FILE));
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
					g.containsTriple(uri,
					"rdf:type",
					"omnom:Webservice")
			);
		}
	}
	@Test
	public void testPublishFileString() {
		String fileUri = client.publishFile(FILE_CONTENTS);
		assertTrue(isCorrectlyPosted(fileUri));
	}

	@Test
	public void testPublishFileStringGrafeo() {
		String fileUri = client.publishFile(FILE_CONTENTS, FILE_POJO.getGrafeo());
		assertTrue(isCorrectlyPosted(fileUri));
		assertTrue(isMetadataKept(fileUri));
	}

	@Test
	public void testPublishFileStringFilePojo() {
		String fileUri = client.publishFile(FILE_CONTENTS, FILE_POJO);
		assertTrue(isCorrectlyPosted(fileUri));
		assertTrue(isMetadataKept(fileUri));
	}

	@Test
	public void testPublishFileInputStream() throws IOException {
		String fileUri = client.publishFile(IOUtils.toInputStream(FILE_CONTENTS));
		assertTrue(isCorrectlyPosted(fileUri));
	}

	@Test
	public void testPublishFileInputStreamString() throws IOException {
		String fileUri = client.publishFile(IOUtils.toInputStream(FILE_CONTENTS), FILE_POJO.getNTriples());
		assertTrue(isCorrectlyPosted(fileUri));
		assertTrue(isMetadataKept(fileUri));
	}

	@Test
	public void testPublishFileInputStreamGrafeo() throws IOException {
		String fileUri = client.publishFile(IOUtils.toInputStream(FILE_CONTENTS), FILE_POJO.getGrafeo());
		assertTrue(isCorrectlyPosted(fileUri));
		assertTrue(isMetadataKept(fileUri));
	}

	@Test
	public void testPublishFileInputStreamFilePojo() throws IOException {
		String fileUri = client.publishFile(IOUtils.toInputStream(FILE_CONTENTS), FILE_POJO);
		assertTrue(isCorrectlyPosted(fileUri));
		assertTrue(isMetadataKept(fileUri));
	}

	@Test
	public void testPublishFileFile() throws IOException {
		String fileUri = client.publishFile(TEMP_FILE);
		assertTrue(isCorrectlyPosted(fileUri));
	}

	@Test
	public void testPublishFileFileString() throws IOException {
		String fileUri = client.publishFile(TEMP_FILE, FILE_POJO.getNTriples());
		assertTrue(isCorrectlyPosted(fileUri));
		assertTrue(isMetadataKept(fileUri));
	}

	@Test
	public void testPublishFileFileGrafeo() throws IOException {
		String fileUri = client.publishFile(TEMP_FILE, FILE_POJO.getGrafeo());
		assertTrue(isCorrectlyPosted(fileUri));
		assertTrue(isMetadataKept(fileUri));
	}

	@Test
	public void testPublishFileFileFilePojo() throws IOException {
		String fileUri = client.publishFile(TEMP_FILE, FILE_POJO);
		assertTrue(isCorrectlyPosted(fileUri));
		assertTrue(isMetadataKept(fileUri));
	}

	@Test
	public void testPublishFileStringString() {
		String fileUri = client.publishFile(FILE_CONTENTS, FILE_POJO.getNTriples());
		assertTrue(isCorrectlyPosted(fileUri));
		assertTrue(isMetadataKept(fileUri));
	}

	
	@Ignore("Not yet implemented")
	@Test
	public void testCreateFileFormDataMultiPartStringString() {
	}

	@Ignore("Not yet implemented")
	@Test
	public void testCreateFileFormDataMultiPartFilePojoString() {
		fail("Not yet implemented");
	}

	@Ignore("Not yet implemented")
	@Test
	public void testCreateFileFormDataMultiPartGrafeoString() {
		fail("Not yet implemented");
	}


}
