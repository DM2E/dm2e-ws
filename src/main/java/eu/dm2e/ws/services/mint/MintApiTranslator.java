package eu.dm2e.ws.services.mint;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.joda.time.DateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public final class MintApiTranslator {
	
	// TODO asUser in UrlApi um mich als User auszugeben
	
	Logger log = Logger.getLogger(getClass().getName());
	
	public static enum API_TYPE {
		// Mappings, retrievable as XSLT
		MAPPING,
//		DATASET,
		// Uploads, e.g. XML or XMLZIP
		DATAUPLOAD,
//		DATASET_TRANSFORMATION
	}
	
	private final String mint_file_base;
	private final String mint_api_base;
	private final String mint_username;
	private final String mint_password;
	
	
	protected MintClient mintClient = new MintClient();
	
	private final String mint_uri_home;
	private String mint_uri_login;
	private String mint_uri_list_mappings;
	private String mint_uri_list_dataupload;
		
//	public MintApiTranslator() {
//		mint_file_base = Config.getString("dm2e.service.mint-file.base_uri");
//		mint_api_base = Config.getString("dm2e.service.mint-file.mint_base");
//		mint_username = Config.getString("dm2e.service.mint-file.username"),
//		mint_password = Config.getString("dm2e.service.mint-file.password")
//	}
	public MintApiTranslator(String localBase, String mintBase, String mintUsername, String mintPassword) {
		mint_file_base = localBase;
		mint_api_base = mintBase;
		mint_username = mintUsername;
		mint_password = mintPassword;
		
		mint_uri_home = mint_api_base + "Home.action";
		mint_uri_login = mint_api_base + "Login.action";
		mint_uri_list_mappings = mint_api_base + "UrlApi?isApi=true&action=list&type=Mapping";
		mint_uri_list_dataupload = mint_api_base + "UrlApi?isApi=true&action=list&type=DataUpload";
	}
	
	
	/**
	 * Ensures that the service is logged into MINT.
	 * 
	 * @return true if logged in, false otherwise
	 */
	protected boolean isLoggedIn() {
		ClientResponse resp = mintClient
				.resource(mint_uri_home)
				.header("Origin", "http://mint-projects.image.ntua.gr")
				.header("Referer", "http://mint-projects.image.ntua.gr/dm2e/Login.action")
				.get(ClientResponse.class);
		log.info("isLoggedIn response: " + resp);
		if (resp.getStatus() != 200) {
			return false;
		}
		final String respStr = resp.getEntity(String.class);
		log.info("isLoggedIn response body: " + respStr);
		if (respStr.contains("URL=./Login_input.action")) {
			return false;
		}
		return true;
		
	}
	
	/**
	 * Make sure that we're logged in into MINT so the UrlApi works.
	 */
	protected void ensureLoggedIn() {
		Builder wr = mintClient.resource(mint_uri_login);
		if (isLoggedIn())
			return;
		else 
			mintClient.clearCookies();
		
		MultivaluedMap<String,String> form = new MultivaluedMapImpl();
		form.add("username", mint_username);
		form.add("password", mint_password);
//		log.info("Logging in as " + mint_username + ":" + mint_password);
		ClientResponse resp = wr
				.type(MediaType.APPLICATION_FORM_URLENCODED)
				.entity(form)
				.post(ClientResponse.class);
		
		final URI location = resp.getLocation();
		log.info("Login Response: " + resp);
		if (resp.getStatus() != 302 || null == location) {
			log.severe("Login failed :(");
			return;
		}
		
		log.info("Login redirect location: " + location);
//		log.info("Login response: " + mintClient.resource(resp.getLocation()).get(String.class));
		log.severe("Logged in now? " + isLoggedIn());
		mintClient.addCookies(resp.getCookies());
		
	}
//	public Grafeo buildGrafeoFromMapping(String id) {
//		ensureLoggedIn();
//		Grafeo g = new GrafeoImpl();
//			ClientResponse resp = mintClient.resource(mappingListUri).get(ClientResponse.class);
//	}
	
	/**
	 * @return
	 */
	public Grafeo buildGrafeoFromMintFiles() {
		ensureLoggedIn();
		Grafeo g = new GrafeoImpl();
		log.info("Add mappings");
		{
			String mappingListUri = mint_uri_list_mappings;
			ClientResponse resp = mintClient.resource(mappingListUri).get(ClientResponse.class);
			final String respStr = resp.getEntity(String.class);
			if (resp.getStatus() > 200) {
				log.info(respStr);
				throw new RuntimeException(respStr);
			}
			final List<FilePojo> list = this.fileListFromMappingList(respStr);
			for (FilePojo fp : list) g.getObjectMapper().addObject(fp);
		}
		log.info("Add uploads");
		{
			// http://mint-projects.image.ntua.gr/dm2e/UrlApi?isApi=true&action=list&type=Dataset
			String datasetListUri = mint_uri_list_dataupload;
			ClientResponse resp = mintClient.resource(datasetListUri).get(ClientResponse.class);
			final String respStr = resp.getEntity(String.class);
			if (resp.getStatus() > 200)
				throw new RuntimeException(respStr);
			final List<FilePojo> list = this.fileListFromDatasetDataUpload(respStr);
			log.info("# of datauploads: " + list.size());
			for (FilePojo fp : list) g.getObjectMapper().addObject(fp);
		}
		return g;
	}
	
	/**
	 * @param uri
	 * @return
	 */
	public FilePojo getFilePojoForUri(URI uri) {
		Grafeo g = this.buildGrafeoFromMintFiles();
		if (!g.containsResource(uri)) {
			return null;
		}
		FilePojo filePojo = g.getObjectMapper().getObject(FilePojo.class, uri);
		return filePojo;
	}
	
	
		
	
	
	public List<FilePojo> fileListFromMappingList(String apiResonse) {
		return fileListFromApiResponse(apiResonse, API_TYPE.MAPPING);
	}
	public List<FilePojo> fileListFromDatasetDataUpload(String apiResonse) {
		return fileListFromApiResponse(apiResonse, API_TYPE.DATAUPLOAD);
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
			} else if (api_type.equals(API_TYPE.DATAUPLOAD)) {
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
