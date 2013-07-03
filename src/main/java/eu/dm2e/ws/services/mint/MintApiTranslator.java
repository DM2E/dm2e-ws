package eu.dm2e.ws.services.mint;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.FilePojo;

public final class MintApiTranslator {
	
	public static enum API_TYPE {
		MAPPING,
//		DATASET,
		DATASET_DATAUPLOAD,
//		DATASET_TRANSFORMATION
	}
	
	private final String mint_file_base;
	private final String mint_api_base;
		
//	public MintApiTranslator() {
//		mint_file_base = Config.getString("dm2e.service.mint-file.base_uri");
//		mint_api_base = Config.getString("dm2e.service.mint-file.mint_base");
//	}
	public MintApiTranslator(String localBase, String mintBase) {
		mint_file_base = localBase;
		mint_api_base = mintBase;
	}
		
	
	
	public List<FilePojo> fileListFromMappingList(String apiResonse) {
		return fileListFromApiResponse(apiResonse, API_TYPE.MAPPING);
	}
	public List<FilePojo> fileListFromDatasetDataUpload(String apiResonse) {
		return fileListFromApiResponse(apiResonse, API_TYPE.DATASET_DATAUPLOAD);
	}
	
	public List<FilePojo> fileListFromApiResponse(String apiResponse, API_TYPE api_type ) {
		
		List<FilePojo> retList = new ArrayList<>();
		
		JsonElement jsonResponseElem = new JsonParser().parse(apiResponse);
		if (! jsonResponseElem.isJsonObject()) {
			throw new IllegalArgumentException();
		}
		JsonObject jsonResponseObject = jsonResponseElem.getAsJsonObject();
		if (null == jsonResponseObject.get("result")) {
			throw new IllegalArgumentException();
		}
		JsonElement jsonFileListElem = jsonResponseObject.get("result");
		if (! jsonFileListElem.isJsonArray()) {
			throw new IllegalArgumentException();
		}
		JsonArray jsonFileList = jsonFileListElem.getAsJsonArray();
		
		for (JsonElement jsonFileElem : jsonFileList) {
			if (! jsonFileElem.isJsonObject()) {
				throw new IllegalArgumentException();
			}
			JsonObject jsonFileObj = jsonFileElem.getAsJsonObject();
			FilePojo fp = null;
			if (api_type.equals(API_TYPE.MAPPING)) {
				fp = createMappingFilePojoFromJsonObject(jsonFileObj);
			} else if (api_type.equals(API_TYPE.DATASET_DATAUPLOAD)) {
				fp = createDatasetDatauploadFilePojoFromJsonObject(jsonFileObj);
			}
			if (null == fp)
				continue;
			retList.add(fp);
		}
		return retList;
	}
	
	protected FilePojo createDatasetDatauploadFilePojoFromJsonObject(JsonObject json) {
		final String uploadID = json.get("dbID").getAsString();
		final String datasetType = json.get("type").getAsString();
		if (! datasetType.equals("DataUpload"))
			return null;
		
		FilePojo fp = new FilePojo();
		
		fp.setId(mint_file_base + "/upload" + uploadID);
		fp.setCreated(DateTime.parse(json.get("created").getAsString()));
		fp.setLastModified(DateTime.parse(json.get("lastModified").getAsString()));
		fp.setOriginalName(json.get("name").getAsString());
		fp.setLabel(json.get("name").getAsString());
		// TODO this will result in a tgz-archived download ... how to get the uncompressed data?
		//http://mint-projects.image.ntua.gr/dm2e/Download?datasetId=1026
		fp.setFileRetrievalURI(URI.create(String.format("%sDownload?datasetId=%s", 
					mint_api_base,
					uploadID)));
		// TODO media type
		// TODO file size
		// TODO this is not the real type of course
		fp.setFileType(NS.OMNOM_TYPES.TGZ);
		return fp;
	}
	protected FilePojo createMappingFilePojoFromJsonObject(JsonObject json) {
		
		final String mappingID = json.get("dbID").getAsString();
		FilePojo fp = new FilePojo();
		fp.setId(mint_file_base + "/mapping" + mappingID);
		fp.setMediaType("application/xslt+xml");
		fp.setFileType(NS.OMNOM_TYPES.XSLT);
		fp.setFileRetrievalURI(
			URI.create(	
				String.format("%sUrlApi?type=Mappingxsl&id=%s", 
					mint_api_base,
					mappingID)));
		try {
			fp.setFileEditURI(URI.create(mint_api_base
						+ "Home.action?kConnector=html.page&url=DoMapping.action"
						+ URLEncoder.encode(
								String.format("?mapid=%s&uploadid=%s&kTitle=Mapping+Tool",
										mappingID,
										// TODO MINT should send at least one uploadid with the API response
										"NO_UPLOAD_ID"
								), "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		// http://mint-projects.image.ntua.gr/dm2e/Home.action?kConnector=html.page&
		//url=DoMapping.action%3Fmapid%3D1166%26uploadId%3D1130&kTitle=Mapping+Tool
		fp.setLabel(json.get("name").getAsString());
		fp.setLastModified(DateTime.parse(json.get("lastModified").getAsString()));
		return fp;
	}

}
