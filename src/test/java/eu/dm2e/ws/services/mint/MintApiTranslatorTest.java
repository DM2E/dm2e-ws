package eu.dm2e.ws.services.mint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;

import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.api.FilePojo;

public class MintApiTranslatorTest extends OmnomTestCase {
	
	public MintApiTranslatorTest() {
        System.setProperty("http.keepAlive", "false");
        Random randomGenerator = new Random();
        {
        	log.info("Retrieving list of Data Uploads");
        	List<FilePojo> list = mintApiTranslator.retrieveListOfDataUploads();
        	assertTrue(list.size() > 0);
        	int randomIdx = randomGenerator.nextInt(list.size());
        	randomDataUploadFP = list.get(randomIdx);
        	assertNotNull(randomDataUploadFP);
        	log.info(randomDataUploadFP.getId());
        	Pattern pat = Pattern.compile("upload(\\d+)$");
        	Matcher m = pat.matcher(randomDataUploadFP.getId());
        	if (m.find()) randomDataUploadMintId = m.group(1);
        	else fail("Invalid mapping URI: " + randomDataUploadFP.getId());
        }
        {
        	log.info("Retrieving list of Mappings");
        	List<FilePojo> list = mintApiTranslator.retrieveListOfMappings();
        	assertTrue(list.size() > 0);
        	int randomIdx = randomGenerator.nextInt(list.size());
        	randomMappingFP = list.get(randomIdx);
        	assertNotNull(randomMappingFP);
        	Pattern pat = Pattern.compile("mapping(\\d+)$");
        	Matcher m = pat.matcher(randomMappingFP.getId());
        	log.info("Getting Id from '" + randomMappingFP.getId() + "'");
        	if (m.find()) randomMappingMintId = m.group(1);
        	else fail("Invalid mapping URI: " + randomMappingFP.getId());
        }
	}
	
	private MintApiTranslator mintApiTranslator = new MintApiTranslator(
			Config.getString("dm2e.service.mint-file.base_uri"),
			Config.getString("dm2e.service.mint-file.mint_base"),
			Config.getString("dm2e.service.mint-file.username"),
			Config.getString("dm2e.service.mint-file.password")
	);
	
	private FilePojo randomMappingFP;
	private String randomMappingMintId;
	private FilePojo randomDataUploadFP;
	private String randomDataUploadMintId;
	
	@Before
	public void setUpMintApiTranslatorTest() {
//		mintApiTranslator.mintClient.clearCookies();
	}
	
	@Test
	public void testLogin() throws InterruptedException {
		log.info("Testing login");
		mintApiTranslator.mintClient.clearCookies();
		log.info(LogbackMarkers.SENSITIVE_INFORMATION, "Logging in as {}:{}"
			, Config.getString("dm2e.service.mint-file.username")
			, Config.getString("dm2e.service.mint-file.password"));
		assertFalse(mintApiTranslator.isLoggedIn());
		mintApiTranslator.ensureLoggedIn();
		log.info("Cookies: " + mintApiTranslator.mintClient.cookies);
		assertTrue(mintApiTranslator.isLoggedIn());
	}
	
	
	@Test
	public void testParseSingleMapping() {
		log.info("Test parsing a single mapping");
		String jsonStr = configString.get(OmnomTestResources.MINT_MAPPING_SINGLE_JSON);
		FilePojo fp = mintApiTranslator.parseFilePojoFromMappingJson(jsonStr);
		log.info(fp.getTerseTurtle());
		assertEquals("UBER-PolytechnischesJournal", fp.getLabel());
		{
			// TODO this takes waaaaaaaaay too long (~60 seconds and more)
//			ClientResponse resp = mintApiTranslator.mintClient
//					.resource(fp.getFileRetrievalURI())
//					.get(ClientResponse.class);
//			assertEquals(200, resp.getStatus());
//			assertEquals(DM2E_MediaType.APPLICATION_XSLT, resp.getType());
		}
		{
			ClientResponse resp = mintApiTranslator.mintClient
					.resource(fp.getFileEditURI())
					.get(ClientResponse.class);
			assertEquals(200, resp.getStatus());
			assertEquals(DM2E_MediaType.TEXT_HTML_UTF8, resp.getType());
		}
	}
	
	@Test
	public void testParseSingleDataUpload() {
		{
			FilePojo fp = mintApiTranslator.parseFilePojoFromDataUploadJson(
					configString.get(OmnomTestResources.MINT_DATASET_SINGLE_DATAUPLOAD_JSON_ZIPXML));
			assertEquals(NS.OMNOM_TYPES.ZIP_XML, fp.getFileType().toString());
			assertEquals("vischer_aesthetik0301_1851.TEI-P5.xml", fp.getLabel());
			ClientResponse resp = mintApiTranslator.mintClient
					.resource(fp.getFileRetrievalURI())
					.get(ClientResponse.class);
			assertEquals(200, resp.getStatus());
			assertEquals(DM2E_MediaType.APPLICATION_X_TAR_UTF8, resp.getType());
		}
		{
			FilePojo fp = mintApiTranslator.parseFilePojoFromDataUploadJson(
					configString.get(OmnomTestResources.MINT_DATASET_SINGLE_DATAUPLOAD_JSON_XML));
			assertEquals(NS.OMNOM_TYPES.XML, fp.getFileType().toString());
			assertEquals("/cost-a32_xml/Ms-141_OA.xml", fp.getLabel());
			ClientResponse resp = mintApiTranslator.mintClient
					.resource(fp.getFileRetrievalURI())
					.get(ClientResponse.class);
			assertEquals(200, resp.getStatus());
			assertEquals(MediaType.APPLICATION_XML_TYPE, resp.getType());
		}
		{
			FilePojo fp = mintApiTranslator.parseFilePojoFromDataUploadJson(
					configString.get(OmnomTestResources.MINT_DATASET_SINGLE_DATAUPLOAD_JSON_TGZXML));
			assertEquals(NS.OMNOM_TYPES.TGZ_XML, fp.getFileType().toString());
			assertEquals("EuPhoto-Extended.zip.tgz", fp.getLabel());
			ClientResponse resp = mintApiTranslator.mintClient
					.resource(fp.getFileRetrievalURI())
					.get(ClientResponse.class);
			assertEquals(200, resp.getStatus());
			// TODO this doesn't work for some reason on the MINT side
//			assertEquals(DM2E_MediaType.APPLICATION_X_TAR_UTF8, resp.getType());
		}
	}
	
	@Test
	public void testParseMappings() {
		String jsonStr = configString.get(OmnomTestResources.MINT_MAPPING_LIST_JSON);
		List<FilePojo> list = mintApiTranslator.parseFileListFromMappingList(jsonStr);
		assertEquals(58, list.size());
	}
	
	@Test
	public void testParseDatasetDatauploads() {
		String jsonStr = configString.get(OmnomTestResources.MINT_UPLOAD_LIST_JSON);
		List<FilePojo> list = mintApiTranslator.parseFileListFromDataUploadList(jsonStr);
		assertEquals(57, list.size());
	}
	
	@Test
	public void testRetrieveMappings() {
		List<FilePojo> list = mintApiTranslator.retrieveListOfMappings();
		assertTrue(list.size() > 0);
	}
	
	@Test
	public void testRetrieveDataUploads() {
		List<FilePojo> list = mintApiTranslator.retrieveListOfDataUploads();
		assertTrue(list.size() > 0);
	}

	@Test
	public void testRetrieveMapping() {
		log.info("Retrieve a single mapping by ID.");
		FilePojo retFP = mintApiTranslator.retrieveMapping(randomMappingMintId);
		try {
			assertEquals(randomMappingFP, retFP);
		} catch (Exception e) {
			throw new ComparisonFailure("", randomMappingFP.toJson(), retFP.toJson());
		}
	}

	@Test
	public void testRetrieveDataUpload() throws Exception {
		log.info("Retrieve a single DataUpload by ID.");
		FilePojo retFP = mintApiTranslator.retrieveDataUpload(randomDataUploadMintId);
		try {
			assertEquals(randomDataUploadFP, retFP);
		} catch (Exception e) {
			throw new ComparisonFailure("", randomDataUploadFP.toJson(), retFP.toJson());
		}
	}

	@Test
	public void testRetrieveFilePojoForUriUpload() throws Exception {
		URI uri = URI.create(Config.getString("dm2e.service.mint-file.base_uri") + "/upload" + randomDataUploadMintId);
		FilePojo retFP = mintApiTranslator.retrieveFilePojoForUri(uri);
		try {
			assertEquals(randomDataUploadFP, retFP);
		} catch (Exception e) {
			throw new ComparisonFailure("", randomDataUploadFP.toJson(), retFP.toJson());
		}
	}
	
	@Test
	public void testRetrieveFilePojoForUriMapping() throws Exception {
		URI uri = URI.create(Config.getString("dm2e.service.mint-file.base_uri") + "/mapping" + randomMappingMintId);
		FilePojo retFP = mintApiTranslator.retrieveFilePojoForUri(uri);
		try {
			assertEquals(randomMappingFP, retFP);
		} catch (Exception e) {
			throw new ComparisonFailure("", randomMappingFP.toJson(), retFP.toJson());
		}
	}

	/**
	 * FIXME this could break on the future since the URI is hard-coded
	 */
	@Test
	public void testConvertTGZtoXML() throws Exception {
		FilePojo fp = new FilePojo();
		fp.setFileType(NS.OMNOM_TYPES.XML);
		fp.setInternalFileLocation("http://mint-projects.image.ntua.gr/dm2e/Download?datasetId=1059");
		byte[] bytes = mintApiTranslator.convertTGZtoXML(fp);
		assertNotNull(bytes);
		assertEquals(54419, bytes.length);
//		log.debug("Lenght: "  + bytes.length);
//		FileUtils.writeByteArrayToFile(new File("test.output"), bytes);
//		System.out.println(new String(bytes, "UTF-8"));
//		log.debug("'{}'", new String(bytes));
	}
}