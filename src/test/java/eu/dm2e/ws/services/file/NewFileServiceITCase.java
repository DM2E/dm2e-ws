package eu.dm2e.ws.services.file;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.wsmanager.ManageService;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

public class NewFileServiceITCase extends OmnomTestCase {
	
	public class MockFileService extends FileService { }
	
	public FileService mockFileService = new MockFileService();
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFileService() {
		FileService fs = new FileService();
		assertNotNull(fs);
        ManageService.startAll();

    }

	@Test
	public void testPostEmptyFile() {
		// Minimal valid file
		{
			Grafeo metaGrafeo = new GrafeoImpl(configFile.get(OmnomTestResources.MINIMAL_FILE));
			FormDataMultiPart fdmp = client.createFileFormDataMultiPart(metaGrafeo, null);
			ClientResponse resp = client.getFileWebResource()
				.path("empty")
				.entity(fdmp)
				.type(MediaType.MULTIPART_FORM_DATA)
				.post(ClientResponse.class);
			log.severe(resp.getEntity(String.class));
			assertEquals("Empty file posted", 201, resp.getStatus());
			URI fileLoc = resp.getLocation();
			assertNotNull(fileLoc);
			
			GrafeoImpl g = new GrafeoImpl(configFile.get(OmnomTestResources.MINIMAL_FILE));
			g.findTopBlank().rename("foo");
			FilePojo fpOrig = g.getObjectMapper().getObject(FilePojo.class, g.resource("foo"));
			
			FilePojo fp = new FilePojo();
			fp.loadFromURI(fileLoc.toString());
			
			assertEquals("Should have metadata from the file desc", fpOrig.getOriginalName(), fp.getOriginalName());
			assertNotNull("Should have metadata created by the service", fp.getMd5());
			
		}
		// Minimal invalid file
		{
			Grafeo metaGrafeo = new GrafeoImpl(configFile.get(OmnomTestResources.ILLEGAL_EMPTY_FILE));
			FormDataMultiPart fdmp = client.createFileFormDataMultiPart(metaGrafeo, null);
			ClientResponse resp = client.getFileWebResource()
				.path("empty")
				.entity(fdmp)
				.type(MediaType.MULTIPART_FORM_DATA)
				.post(ClientResponse.class);
			assertEquals("Empty file is also okay ", 201, resp.getStatus());
		}
		// Neither metadata nor file data
		{
			FormDataMultiPart fdmp = client.createFileFormDataMultiPart((String)null, null);
			ClientResponse resp = client.getFileWebResource()
				.path("empty")
				.entity(fdmp)
				.type(MediaType.MULTIPART_FORM_DATA)
				.post(ClientResponse.class);
			assertEquals("Empty file posted", 201, resp.getStatus());
			URI fileLoc = resp.getLocation();
			assertNotNull(fileLoc);
			FilePojo fp = new FilePojo();
			fp.loadFromURI(fileLoc.toString());
			assertEquals(FileStatus.WAITING.toString(), fp.getFileStatus());
		}
	}

	@Test
	public void testPutFileAndOrMetadata() throws IOException {
		FilePojo origFP = new FilePojo();
		origFP.setOriginalName("Foo");
		origFP.setMd5("123");
		OmnomTestResources res = OmnomTestResources.ASCII_NOISE;
		// replace metadata
		{
			String fileUriStr = client.publishFile(configFile.get(res), origFP);
			FilePojo replacementFP = new FilePojo();
			replacementFP.setMd5("987");
			FormDataMultiPart fdmp = client.createFileFormDataMultiPart(replacementFP, null);
			ClientResponse resp = client.getFileWebResource()
					.queryParam("uri", fileUriStr)
					.type(MediaType.MULTIPART_FORM_DATA)
					.entity(fdmp)
					.put(ClientResponse.class);
			assertEquals(200, resp.getStatus());
			FilePojo curFP = new FilePojo();
			curFP.loadFromURI(fileUriStr);
			assertThat("md5 should be replaced", curFP.getMd5(), not(origFP.getMd5()));
			assertThat("md5 should be replaced", curFP.getMd5(), is("987"));
			assertThat("originalname should have been deleted", curFP.getOriginalName(), nullValue());
		}
		// replace file data
		{
			String fileUriStr = client.publishFile(configFile.get(res), origFP);
			String newFileContents = "NEW FILE CONTENT";
			FormDataMultiPart fdmp = client.createFileFormDataMultiPart("", newFileContents);
			ClientResponse resp = client.getFileWebResource()
					.queryParam("uri", fileUriStr)
					.type(MediaType.MULTIPART_FORM_DATA)
					.entity(fdmp)
					.put(ClientResponse.class);
			assertEquals(200, resp.getStatus());
			
			ClientResponse respData = client
					.resource(fileUriStr)
					.get(ClientResponse.class);
			assertThat(respData.getStatus(), is(200));
			
			String respDataStr = respData.getEntity(String.class);
			assertThat("file is updated", respDataStr, is(newFileContents));
			FilePojo curFP = new FilePojo();
			curFP.loadFromURI(fileUriStr);
			assertThat("metadata is unchanged", curFP.getOriginalName(), is(origFP.getOriginalName()));
		}
	}
//
	@Test
	public void testUploadFile() {
		// Neither metadata nor file data
		{
			FormDataMultiPart fdmp = client.createFileFormDataMultiPart((String)null, null);
			ClientResponse resp = client.getFileWebResource()
				.entity(fdmp)
				.type(MediaType.MULTIPART_FORM_DATA)
				.post(ClientResponse.class);
			assertEquals("Should be an error", 400, resp.getStatus());
			String respStr = resp.getEntity(String.class);
			assertEquals(ErrorMsg.NO_FILE_AND_NO_METADATA.toString(), respStr);
		}
		// minimal valid file
		{
			Grafeo metaGrafeo = new GrafeoImpl(configFile.get(OmnomTestResources.MINIMAL_FILE));
			FormDataMultiPart fdmp = client.createFileFormDataMultiPart(metaGrafeo, null);
			ClientResponse resp = client.getFileWebResource()
				.entity(fdmp)
				.type(MediaType.MULTIPART_FORM_DATA)
				.post(ClientResponse.class);
//			log.info(configString.get(OmnomTestResources.MINIMAL_FILE));
			log.info(resp.getEntity(String.class));
			assertEquals("Empty file posted", 201, resp.getStatus());
			URI fileLoc = resp.getLocation();
			assertNotNull(fileLoc);
		}
		// minimal valid file without top blank node
		{
			Grafeo metaGrafeo = new GrafeoImpl(configFile.get(OmnomTestResources.MINIMAL_FILE_WITH_URI));
			FormDataMultiPart fdmp = client.createFileFormDataMultiPart(metaGrafeo, null);
			ClientResponse resp = client.getFileWebResource()
				.entity(fdmp)
				.type(MediaType.MULTIPART_FORM_DATA)
				.post(ClientResponse.class);
			log.info(configString.get(OmnomTestResources.MINIMAL_FILE));
			log.info(resp.getEntity(String.class));
			assertEquals("posting rdf data with a uri", 201, resp.getStatus());
		}
		// minimal invalid file
		{
			Grafeo metaGrafeo = new GrafeoImpl(configFile.get(OmnomTestResources.ILLEGAL_EMPTY_FILE));
			FormDataMultiPart fdmp = client.createFileFormDataMultiPart(metaGrafeo, null);
			ClientResponse resp = client.getFileWebResource()
				.entity(fdmp)
				.type(MediaType.MULTIPART_FORM_DATA)
				.post(ClientResponse.class);
			String respStr = resp.getEntity(String.class);
			assertEquals("No fileRetrievalURI is an error here ", 400, resp.getStatus());
			assertThat(respStr, containsString(ErrorMsg.NO_FILE_RETRIEVAL_URI.toString()));
		}
		// bogus RDF metadata
		{
			String asciiNoise = configString.get(OmnomTestResources.ASCII_NOISE);
			FormDataMultiPart fdmp = client.createFileFormDataMultiPart(asciiNoise, null);
			ClientResponse resp = client.getFileWebResource()
				.entity(fdmp)
				.type(MediaType.MULTIPART_FORM_DATA)
				.post(ClientResponse.class);
			String respStr = resp.getEntity(String.class);
			assertEquals("BAD RDF!", 400, resp.getStatus());
			assertThat(respStr, containsString(ErrorMsg.BAD_RDF.toString()));
		}
	}
//
	@Test
	public void testGetFile() throws URISyntaxException, IOException {
		Set<OmnomTestResources> exampleResources = new HashSet<>();
		exampleResources.add(OmnomTestResources.ASCII_NOISE);
		exampleResources.add(OmnomTestResources.DEMO_JOB);
		for (OmnomTestResources res : exampleResources){
			log.info("Testing getting file and metadata for " + res.getPath());
			FilePojo filePojo = new FilePojo();
			String fileUriStr = client.publishFile(configFile.get(res), filePojo);
			URI fileURI = new URI(fileUriStr);
			{
				ClientResponse resp = client.resource(fileURI)
						.get(ClientResponse.class);
				assertEquals("No Accept header, file data", 200, resp.getStatus());
				assertEquals(configString.get(res), resp.getEntity(String.class));
			}
			{
				for (String mediaType : DM2E_MediaType.SET_OF_RDF_TYPES) {
					log.info("Getting file metadata as " + mediaType);
					ClientResponse resp = client.resource(fileURI)
							.accept(mediaType)
							.get(ClientResponse.class);
					assertEquals(200, resp.getStatus());
					String metaStr = resp.getEntity(String.class);
//					log.info(metaStr);
					Grafeo g = new GrafeoImpl(metaStr, true);
					assertEquals(7, g.size());
				}
			}
		}
	}
	@Test
	public void testDeleteFile() throws IOException {
		OmnomTestResources res = OmnomTestResources.ASCII_NOISE;
		String fileUri = client.publishFile(configFile.get(res), new FilePojo());
		{
			FilePojo fp = new FilePojo();
			fp.loadFromURI(fileUri);
			log.severe(fp.getTurtle());
			assertEquals("File is available", FileStatus.AVAILABLE.toString(), fp.getFileStatus());
		}
		{
			ClientResponse resp = client.resource(fileUri).delete(ClientResponse.class);
			assertEquals("aaaaand it's gone :)", 200, resp.getStatus());
		}
		{
			ClientResponse resp = client.resource(fileUri)
					.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.get(ClientResponse.class);
			GrafeoImpl g = new GrafeoImpl(resp.getEntityInputStream());
			g.containsStatementPattern(fileUri, "omnom:fileStatus", "DELETED");
			FilePojo fp = new FilePojo();
			fp.loadFromURI(fileUri);
			assertEquals("File is available", FileStatus.DELETED.toString(), fp.getFileStatus());
		}
	}
	
	@Test
	public void testUpdateStatements() throws IOException {
		OmnomTestResources res = OmnomTestResources.ASCII_NOISE;
		FilePojo origFp = new FilePojo();
		origFp.setOriginalName("foo_bar.baz");
		String fileUri = client.publishFile(configFile.get(res), origFp);
		{
			FilePojo fp = new FilePojo();
			fp.loadFromURI(fileUri);
			log.severe(fp.getTurtle());
			assertEquals("File is available", FileStatus.AVAILABLE.toString(), fp.getFileStatus());
			assertEquals(origFp.getOriginalName(), fp.getOriginalName());
			assertEquals("d41d8cd98f00b204e9800998ecf8427e", fp.getMd5());
		}
		{
			FilePojo patchFp = new FilePojo();
			patchFp.setOriginalName("quux.zoot");
			ClientResponse resp = client.resource(fileUri)
					.path("patch")
					.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.post(ClientResponse.class, patchFp.getNTriples());
			assertEquals(200, resp.getStatus());
			FilePojo fp = new FilePojo();
			fp.loadFromURI(fileUri);
			assertThat(fp.getOriginalName(), not(origFp.getOriginalName()));
			assertThat(fp.getOriginalName(), is(patchFp.getOriginalName()));
			assertEquals("d41d8cd98f00b204e9800998ecf8427e", fp.getMd5());
		}
	}

}
