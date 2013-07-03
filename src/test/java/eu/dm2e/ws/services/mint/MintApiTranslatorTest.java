package eu.dm2e.ws.services.mint;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.api.FilePojo;

public class MintApiTranslatorTest extends OmnomTestCase {
	
	private MintApiTranslator mintApiTranslator = new MintApiTranslator(
			Config.getString("dm2e.service.mint-file.base_uri"),
			Config.getString("dm2e.service.mint-file.mint_base")
	);
	
	@Test
	public void testTranslateSingleMapping() {
		String jsonStr = configString.get(OmnomTestResources.MINT_MAPPING_SINGLE_JSON);
		JsonObject jsonObj = new JsonParser().parse(jsonStr).getAsJsonObject();
		log.info(testGson.toJson(jsonObj));
		FilePojo fp = mintApiTranslator.createMappingFilePojoFromJsonObject(jsonObj);
		log.info(fp.getTerseTurtle());
		assertEquals("UBER-PolytechnischesJournal", fp.getLabel());
	}
	
	@Test
	public void testTranslateSingleDataUpload() {
		String jsonStr = configString.get(OmnomTestResources.MINT_DATASET_SINGLE_DATAUPLOAD_JSON);
		JsonObject jsonObj = new JsonParser().parse(jsonStr).getAsJsonObject();
		log.info(testGson.toJson(jsonObj));
		FilePojo fp = mintApiTranslator.createDatasetDatauploadFilePojoFromJsonObject(jsonObj);
		log.info(fp.getTerseTurtle());
		assertEquals("vischer_aesthetik0301_1851.TEI-P5.xml", fp.getLabel());
	}
	
	@Test
	public void testTranslateMappings() {
		String jsonStr = configString.get(OmnomTestResources.MINT_MAPPING_LIST_JSON);
		List<FilePojo> list = mintApiTranslator.fileListFromMappingList(jsonStr);
		assertEquals(58, list.size());
	}
	
	@Test
	public void testTranslateDatasetDatauploads() {
		String jsonStr = configString.get(OmnomTestResources.MINT_DATASET_LIST_JSON);
		List<FilePojo> list = mintApiTranslator.fileListFromDatasetDataUpload(jsonStr);
		assertEquals(62, list.size());
	}

}
