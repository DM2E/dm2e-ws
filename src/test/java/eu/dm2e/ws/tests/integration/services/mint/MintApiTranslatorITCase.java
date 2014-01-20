package eu.dm2e.ws.tests.integration.services.mint;

import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.tests.OmnomTestCase;
import eu.dm2e.ws.tests.OmnomTestResources;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

//@Ignore("MINT Tests always fail :-(")
public class MintApiTranslatorITCase extends OmnomTestCase {
	
	public MintApiTranslatorITCase() {
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
	
	private eu.dm2e.ws.services.mint.MintApiTranslator mintApiTranslator = new eu.dm2e.ws.services.mint.MintApiTranslator(
			Config.get(ConfigProp.MINT_BASE_URI),
			Config.get(ConfigProp.MINT_REMOTE_BASE_URI),
			Config.get(ConfigProp.MINT_USERNAME),
			Config.get(ConfigProp.MINT_PASSWORD)
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
//    @Ignore("MINT Tests always fail :-(")
	public void testLogin() throws InterruptedException {
		log.info("Testing login");
		mintApiTranslator.getMintClient().clearCookies();
		log.info(LogbackMarkers.SENSITIVE_INFORMATION, "Logging in as {}:{}"
			, Config.get(ConfigProp.MINT_USERNAME)
			, Config.get(ConfigProp.MINT_PASSWORD));
		assertFalse(mintApiTranslator.isLoggedIn());
		mintApiTranslator.ensureLoggedIn();
		log.info("Cookies: " + mintApiTranslator.getMintClient().getCookies());
		assertTrue(mintApiTranslator.isLoggedIn());
	}
	
	
	@Test
//    @Ignore("MINT Tests always fail :-(")
	public void testParseSingleMapping() {
		log.info("Test parsing a single mapping");
		String jsonStr = configString.get(OmnomTestResources.MINT_MAPPING_SINGLE_JSON);
		FilePojo fp = mintApiTranslator.parseFilePojoFromMappingJson(jsonStr);
		log.info(fp.getTerseTurtle());
		assertEquals("UBER-PolytechnischesJournal", fp.getLabel());
		assertNotNull(fp.getFileEditURI());
		assertNotNull(fp.getFileRetrievalURI());
		assertThat(fp.getFileRetrievalURI().toString(), containsString("MappingOptions"));
		{
			// TODO this takes waaaaaaaaay too long (~60 seconds and more)
//			ClientResponse resp = mintApiTranslator.mintClient
//					.resource(fp.getFileRetrievalURI())
//					.get();
//			assertEquals(200, resp.getStatus());
//			assertEquals(DM2E_MediaType.APPLICATION_XSLT, resp.getType());
		}
		{
			Response resp = mintApiTranslator.getMintClient()
					.target(fp.getFileEditURI())
					.get();
			assertEquals(200, resp.getStatus());
			assertEquals(DM2E_MediaType.TEXT_HTML_UTF8, resp.getMediaType());
		}
	}
	@Test
//    @Ignore("MINT Tests always fail :-(")
	public void testParseSingleMappingNoUploadId() {
		log.info("Test parsing a single mapping");
		String jsonStr = configString.get(OmnomTestResources.MINT_MAPPING_SINGLE_NO_UPLOAD_JSON);
		FilePojo fp = mintApiTranslator.parseFilePojoFromMappingJson(jsonStr);
//		log.info(fp.getTerseTurtle());
		assertEquals("UBER-PolytechnischesJournal", fp.getLabel());
		assertNull(fp.getFileEditURI());
		assertNotNull(fp.getFileRetrievalURI());
		assertThat(fp.getFileRetrievalURI().toString(), containsString("UrlApi"));
	}
	
	@Test
//    @Ignore("MINT Tests always fail :-(")
	public void testParseSingleDataUpload() {
		{
			FilePojo fp = mintApiTranslator.parseFilePojoFromDataUploadJson(
					configString.get(OmnomTestResources.MINT_DATASET_SINGLE_DATAUPLOAD_JSON_ZIPXML));
			assertEquals(NS.OMNOM_TYPES.ZIP_XML, fp.getFileType().toString());
			assertEquals("vischer_aesthetik0301_1851.TEI-P5.xml", fp.getLabel());
			log.debug("File Retrieval URI: " + fp.getFileRetrievalURI());
			Response resp = mintApiTranslator.getMintClient()
					.target(fp.getFileRetrievalURI())
					.get();
			log.debug("Response Status: " + resp.getStatus());
			log.debug("Response Type: " + resp.getMediaType());
			assertEquals(200, resp.getStatus());
			assertEquals(DM2E_MediaType.APPLICATION_X_TAR_UTF8_TYPE, resp.getMediaType());
		}
		{
			FilePojo fp = mintApiTranslator.parseFilePojoFromDataUploadJson(
					configString.get(OmnomTestResources.MINT_DATASET_SINGLE_DATAUPLOAD_JSON_XML));
			assertEquals(NS.OMNOM_TYPES.XML, fp.getFileType().toString());
			assertEquals("/cost-a32_xml/Ms-141_OA.xml", fp.getLabel());
			Response resp = mintApiTranslator.getMintClient()
					.target(fp.getFileRetrievalURI())
					.get();
			assertEquals(200, resp.getStatus());
			assertEquals(MediaType.APPLICATION_XML_TYPE, resp.getMediaType());
		}
		{
			FilePojo fp = mintApiTranslator.parseFilePojoFromDataUploadJson(
					configString.get(OmnomTestResources.MINT_DATASET_SINGLE_DATAUPLOAD_JSON_TGZXML));
			assertEquals(NS.OMNOM_TYPES.TGZ_XML, fp.getFileType().toString());
			assertEquals("EuPhoto-Extended.zip.tgz", fp.getLabel());
			Response resp = mintApiTranslator.getMintClient()
					.target(fp.getFileRetrievalURI())
					.get();
			assertEquals(200, resp.getStatus());
			// TODO this doesn't work for some reason on the MINT side
//			assertEquals(DM2E_MediaType.APPLICATION_X_TAR_UTF8, resp.getType());
		}
	}
	
	@Test
//    @Ignore("MINT Tests always fail :-(")
	public void testParseMappings() {
		String jsonStr = configString.get(OmnomTestResources.MINT_MAPPING_LIST_JSON);
		List<FilePojo> list = mintApiTranslator.parseFileListFromMappingList(jsonStr);
		assertEquals(58, list.size());
	}
	
	@Test
//    @Ignore("MINT Tests always fail :-(")
	public void testParseDatasetDatauploads() {
		String jsonStr = configString.get(OmnomTestResources.MINT_UPLOAD_LIST_JSON);
		List<FilePojo> list = mintApiTranslator.parseFileListFromDataUploadList(jsonStr);
		assertEquals(57, list.size());
	}
	
	@Test
//    @Ignore("MINT Tests always fail :-(")
	public void testRetrieveMappings() {
		List<FilePojo> list = mintApiTranslator.retrieveListOfMappings();
		assertTrue(list.size() > 0);
	}
	
	@Test
//    @Ignore("MINT Tests always fail :-(")
	public void testRetrieveDataUploads() {
		List<FilePojo> list = mintApiTranslator.retrieveListOfDataUploads();
		assertTrue(list.size() > 0);
	}

	@Test
//    @Ignore("MINT Tests always fail :-(")
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
//    @Ignore("MINT Tests always fail :-(")
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
//    @Ignore("MINT Tests always fail :-(")
	public void testRetrieveFilePojoForUriUpload() throws Exception {
		URI uri = URI.create(Config.get(ConfigProp.MINT_BASE_URI) + "/upload" + randomDataUploadMintId);
		FilePojo retFP = mintApiTranslator.retrieveFilePojoForUri(uri);
		try {
			assertEquals(randomDataUploadFP, retFP);
		} catch (Exception e) {
			throw new ComparisonFailure("", randomDataUploadFP.toJson(), retFP.toJson());
		}
	}
	
	@Test
//    @Ignore("MINT Tests always fail :-(")
	public void testRetrieveFilePojoForUriMapping() throws Exception {
		URI uri = URI.create(Config.get(ConfigProp.MINT_BASE_URI) + "/mapping" + randomMappingMintId);
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
    @Ignore("Have to find a proper example URL sometime")
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