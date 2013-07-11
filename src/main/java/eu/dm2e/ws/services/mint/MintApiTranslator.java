package eu.dm2e.ws.services.mint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * Translates resources in MINT to dm2e-ws by using the MINT UrlApi.
 * 
 * TODO Use asUser in the UrlApi to ensure correct rights
 * 
 * @author Konstantin Baierer
 * 
 */
public final class MintApiTranslator {

	Logger log = LoggerFactory.getLogger(getClass().getName());

	public static enum API_TYPE {
		// Mappings, retrievable as XSLT
		MAPPING,
		// Uploads, e.g. XML or XMLZIP
		DATA_UPLOAD,
	}

	// mint_file_base = Config.getString("dm2e.service.mint-file.base_uri");
	// e.g. http://localhost:9998/mint-file/
	private final String mint_file_base;
	// mint_api_base = Config.getString("dm2e.service.mint-file.mint_base");
	// e.g. http://mint-projects.image.ntua.gr/dm2e/
	private final String mint_api_base;
	// mint_username = Config.getString("dm2e.service.mint-file.username"),
	private final String mint_username;
	// mint_password = Config.getString("dm2e.service.mint-file.password")
	private final String mint_password;

	// Wrapper object around jersey that keeps cookies
	protected MintClient mintClient = new MintClient();

	private final String mint_uri_home;
	private String mint_uri_login;
	private String mint_uri_list_mappings;
	private String mint_uri_list_dataupload;
	private String mint_uri_single_mapping;
	private String mint_uri_single_dataupload;

	/**
	 * @param localBase
	 *            URI of the local MINT wrapper service
	 * @param mintBase
	 *            URI of the MINT instance to bridge to
	 * @param mintUsername
	 *            MINT username of the API user
	 * @param mintPassword
	 *            MINT password of the API user
	 */
	public MintApiTranslator(String localBase, String mintBase, String mintUsername, String mintPassword) {
		
		mint_file_base = localBase;
		mint_api_base = mintBase;
		mint_username = mintUsername;
		mint_password = mintPassword;

		if (null == mint_file_base
				|| null == mint_api_base
				|| null == mint_username
				|| null == mint_password) {
			throw new RuntimeException(
					"MintApiTranslator initialized with null values. Check the config.xml");
		}

		mint_uri_home = mint_api_base + "Home.action";
		mint_uri_login = mint_api_base + "Login.action";
		mint_uri_list_mappings = mint_api_base + "UrlApi?isApi=true&action=list&type=Mapping";
		mint_uri_list_dataupload = mint_api_base + "UrlApi?isApi=true&action=list&type=DataUpload";
		mint_uri_single_mapping = mint_api_base + "UrlApi?isApi=truet&type=Mapping&id=";
		mint_uri_single_dataupload = mint_api_base + "UrlApi?isApi=truet&type=DataUpload&id=";
	}

	/**
	 * Ensures that the service is logged into MINT.
	 * 
	 * @return true if logged in, false otherwise
	 */
	protected boolean isLoggedIn() {
		Response resp = mintClient
				.target(mint_uri_home)
				.header("Origin", "http://mint-projects.image.ntua.gr")
				.header("Referer", "http://mint-projects.image.ntua.gr/dm2e/Login.action")
				.get();
		log.debug("isLoggedIn response: " + resp.getStatusInfo().toString());
		log.trace("Location: " + resp.getLocation());

		final String respStr = resp.readEntity(String.class);
		log.trace(LogbackMarkers.HTTP_RESPONSE_DUMP, "Body: " + respStr);

		if (resp.getStatus() != 200) {
			return false;
		}
		if (respStr.contains("URL=./Login_input.action")) {
			return false;
		}
		return true;

	}

	/**
	 * Make sure that we're logged in into MINT so the UrlApi works.
	 */
	protected void ensureLoggedIn() {

		log.trace("Cookies: " + mintClient.cookies);
		if (isLoggedIn())
			return;
		else {
			log.debug("Clearing cookies, trying to login.");
			mintClient.clearCookies();
		}

		Form form = new Form();
		form.param("username", mint_username);
		form.param("password", mint_password);
		log.info(LogbackMarkers.SENSITIVE_INFORMATION, "Logging in as " + mint_username + ":"
				+ mint_password);
		Response resp = mintClient
				.target(mint_uri_login)
				.header("Origin", "http://mint-projects.image.ntua.gr")
				.header("Referer", "http://mint-projects.image.ntua.gr/dm2e/Login.action")
				.post(Entity.form(form));

		final URI location = resp.getLocation();
		log.debug("Login Response: " + resp);
		if (resp.getStatus() != 302 || null == location
				|| !location.toString().matches(".*Home.action.*")) {
			log.debug("Location: " + resp.getLocation());
			String msg = "Login into MINT failed :(";
			RuntimeException e = new RuntimeException(msg);
			log.error(LogbackMarkers.SERVER_COMMUNICATION, msg, e);
			throw e;
		}

		log.debug("Login redirect location: " + location);
		// log.info("Login response: " +
		// mintClient.resource(resp.getLocation()).get(String.class));
		mintClient.addCookies(resp.getCookies().values());

	}

	/**
	 * Retrieve a list of all mappings and uploads as a Grafeo of serialized
	 * FilePojos.
	 * 
	 * @return Grafeo containing all mappings and uploads as FilePojos.
	 */
	public Grafeo retrieveAllMappingsAndDataUploadsAsGrafeo() {
		Grafeo g = new GrafeoImpl();
		log.info("Add mappings and DataUploads");
		final List<FilePojo> list = retrieveAllMappingsAndDataUploads();
		for (FilePojo fp : list)
			g.getObjectMapper().addObject(fp);
		return g;
	}

	/**
	 * Retrieve all mappings and uploads as a list of FilePojos.
	 * 
	 * @return list of all FilePojos
	 */
	public List<FilePojo> retrieveAllMappingsAndDataUploads() {
		List<FilePojo> fullList = new ArrayList<>();
		fullList.addAll(retrieveListOfMappings());
		fullList.addAll(retrieveListOfDataUploads());
		return fullList;
	}

	/**
	 * Try to dereference the fileRetrievalURI of a FilePojo, extract it and
	 * return the content.
	 * 
	 * MINT sends even single XML files compressed in tar.gz. This is a
	 * workaround to make those XML files easy to consume for transformation
	 * services.
	 * 
	 * @param fp
	 * @return
	 */
	public byte[] convertTGZtoXML(FilePojo fp) {

		if (!fp.getFileType().toString().equals(NS.OMNOM_TYPES.XML)) {
			return null;
		}

		log.debug("Downloading from {}", fp.getInternalFileLocation());
		Response resp = mintClient
				.target(fp.getInternalFileLocation())
				.get();
		if (!resp.getMediaType().equals(DM2E_MediaType.APPLICATION_X_TAR_UTF8_TYPE)) {
			log.info("Expected {}, but received {}. Probably a rights issue.",
					DM2E_MediaType.APPLICATION_X_TAR_UTF8_TYPE,
					resp.getMediaType());
			return null;
		}

		byte[] content = null;
		File temp;
		FileOutputStream tempOut = null;
		FileInputStream tempIn = null;
		try {
			temp = File.createTempFile("temp", ".txt");
			temp.deleteOnExit();
			tempOut = new FileOutputStream(temp);
			IOUtils.copy(resp.readEntity(InputStream.class), tempOut);
			tempOut.close();

			tempIn = new FileInputStream(temp);
			TarArchiveInputStream tarIn = new TarArchiveInputStream(new GZIPInputStream(tempIn));

			ArchiveEntry entry = tarIn.getNextEntry();
			content = new byte[(int) entry.getSize()];
			tarIn.read(content);

			tarIn.close();
			tempIn.close();
		} catch (IOException e) {
			log.error("Exception decompressing TGZ: {}", e, e);
		}

		return content;
	}

	/**
	 * Retrieves the list of all Mappings from MINT and creates a list of
	 * FilePojos from it.
	 * 
	 * @return
	 */
	public List<FilePojo> retrieveListOfDataUploads() {
		ensureLoggedIn();
		// http://mint-projects.image.ntua.gr/dm2e/UrlApi?isApi=true&action=list&type=Dataset
		String datasetListUri = mint_uri_list_dataupload;
		Response resp = mintClient.target(datasetListUri).get();
		final String respStr = resp.readEntity(String.class);
		if (resp.getStatus() > 200)
			throw new RuntimeException(respStr);
		log.debug(LogbackMarkers.DATA_DUMP, respStr);
		final List<FilePojo> list = this.parseFileListFromDataUploadList(respStr);
		log.trace("Number of DataUploads: " + list.size());
		return list;
	}

	/**
	 * Retrieves the list of all Mappings from MINT and creates a list of
	 * FilePojos from it.
	 * 
	 * @return
	 */
	public List<FilePojo> retrieveListOfMappings() {
		ensureLoggedIn();
		String mappingListUri = mint_uri_list_mappings;
		Response resp = mintClient.target(mappingListUri).get();
		final String respStr = resp.readEntity(String.class);
		if (resp.getStatus() > 200) {
			log.info(respStr);
			throw new RuntimeException(respStr);
		}
		final List<FilePojo> list = this.parseFileListFromMappingList(respStr);
		log.trace("Number of Mappings: " + list.size());
		return list;
	}

	/**
	 * Retrieve a single Mapping from MINT and create a FilePojo from it.
	 * 
	 * @param id
	 * @return
	 */
	public FilePojo retrieveMapping(String id) {
		return retrieveAnyMintFileById(id, API_TYPE.MAPPING);
	}

	/**
	 * Retrieve a single DataUpload from MINT and create a FilePojo from it.
	 * 
	 * @param id
	 * @return
	 */
	public FilePojo retrieveDataUpload(String id) {
		return retrieveAnyMintFileById(id, API_TYPE.DATA_UPLOAD);
	}

	/**
	 * Retrieve any mint file (mapping or upload) by it's respective ID
	 * 
	 * @param id
	 * @param apiType
	 * @return
	 */
	private FilePojo retrieveAnyMintFileById(String id,
			API_TYPE apiType) {
		if (null == id) {
			throw new RuntimeException("Can't retrieve a file without an ID.");
		}
		ensureLoggedIn();
		String reqUri;
		if (apiType.equals(API_TYPE.DATA_UPLOAD)) {
			reqUri = mint_uri_single_dataupload + id;
		} else if (apiType.equals(API_TYPE.MAPPING)) {
			reqUri = mint_uri_single_mapping + id;
		} else {
			throw new RuntimeException("Unhandled API_TYPE: " + apiType);
		}
		Response resp = mintClient.target(reqUri).get();
		final String respStr = resp.readEntity(String.class);
		log.trace(LogbackMarkers.HTTP_RESPONSE_DUMP, "File request result: {}", respStr);
		if (respStr.contains("<h4>Exception</h4>")) {
			log.error("MINT croaked on '{}'", reqUri);
			throw new RuntimeException();
		}
		FilePojo fp = parseFilePojoFromApiResponse(respStr, apiType);
		return fp;
	}

	/**
	 * Retrieve a FilePojo for a MINT file with a certain URI.
	 * 
	 * @param uri
	 * @return
	 */
	public FilePojo retrieveFilePojoForUri(URI uri) {
		String uriPath = uri.getPath();

		Pattern mappingPattern = Pattern.compile("mapping(\\d+)$");
		Matcher mappingPatternMatcher = mappingPattern.matcher(uriPath);

		Pattern uploadPattern = Pattern.compile("upload(\\d+)(?:/data)?$");
		Matcher uploadPatternMatcher = uploadPattern.matcher(uriPath);

		if (mappingPatternMatcher.find()) {
			String id = mappingPatternMatcher.group(1);
			return retrieveMapping(id);
		} else if (uploadPatternMatcher.find()) {
			String id = uploadPatternMatcher.group(1);
			return retrieveDataUpload(id);
		} else {
			throw new RuntimeException("Unknown URI sent to MINT API Translator: " + uri);
		}
	}

	/**
	 * Translate one raw MINT api response to a list of FilePojos.
	 * 
	 * @param apiResponse
	 * @param api_type
	 * @return
	 */
	protected List<FilePojo> parseFileListFromApiResponse(String apiResponse,
			API_TYPE api_type) {

		List<FilePojo> retList = new ArrayList<>();

		JsonElement jsonResponseElem = new JsonParser().parse(apiResponse);
		if (!jsonResponseElem.isJsonObject()) {
			throw new IllegalArgumentException();
		}
		JsonObject jsonResponseObject = jsonResponseElem.getAsJsonObject();
		if (null == jsonResponseObject.get("result")) {
			throw new IllegalArgumentException();
		}
		JsonElement jsonFileListElem = jsonResponseObject.get("result");
		if (!jsonFileListElem.isJsonArray()) {
			throw new IllegalArgumentException();
		}
		JsonArray jsonFileList = jsonFileListElem.getAsJsonArray();

		for (JsonElement jsonFileElem : jsonFileList) {
			if (!jsonFileElem.isJsonObject()) {
				throw new IllegalArgumentException();
			}
			JsonObject jsonFileObj = jsonFileElem.getAsJsonObject();
			FilePojo fp = null;
			if (api_type.equals(API_TYPE.MAPPING)) {
				fp = parseFilePojoFromMappingJson(jsonFileObj);
			} else if (api_type.equals(API_TYPE.DATA_UPLOAD)) {
				fp = parseFilePojoFromDataUploadJson(jsonFileObj);
			}
			if (null == fp)
				continue;
			retList.add(fp);
		}
		return retList;
	}

	/**
	 * Translate one raw MINT API Mapping response to a list of FilePojos.
	 * 
	 * @param apiResonse
	 * @return
	 */
	public List<FilePojo> parseFileListFromMappingList(String apiResonse) {
		return parseFileListFromApiResponse(apiResonse, API_TYPE.MAPPING);
	}

	/**
	 * Translate one raw MINT API DataUpload response to a list of FilePojos.
	 * 
	 * @param apiResonse
	 * @return
	 */
	public List<FilePojo> parseFileListFromDataUploadList(String apiResonse) {
		return parseFileListFromApiResponse(apiResonse, API_TYPE.DATA_UPLOAD);
	}

	/**
	 * Translate one raw MINT API response to a single FilePojo.
	 * 
	 * @param apiResponse
	 * @param api_type
	 * @return
	 */
	protected FilePojo parseFilePojoFromApiResponse(String apiResponse,
			API_TYPE api_type) {

		log.trace(LogbackMarkers.HTTP_RESPONSE_DUMP, "Parsing API response: {}", apiResponse);

		FilePojo retPojo = null;

		JsonElement jsonResponseElem = new JsonParser().parse(apiResponse);
		if (!jsonResponseElem.isJsonObject()) {
			throw new IllegalArgumentException();
		}
		JsonObject jsonResponseObject = jsonResponseElem.getAsJsonObject();
		if (null == jsonResponseObject.get("result")) {
			throw new IllegalArgumentException();
		}
		JsonElement jsonFileListElem = jsonResponseObject.get("result");
		if (!jsonFileListElem.isJsonObject()) {
			throw new IllegalArgumentException();
		}
		JsonObject jsonFileObject = jsonFileListElem.getAsJsonObject();
		if (api_type.equals(API_TYPE.MAPPING)) {
			retPojo = parseFilePojoFromMappingJson(jsonFileObject);
		} else if (api_type.equals(API_TYPE.DATA_UPLOAD)) {
			retPojo = parseFilePojoFromDataUploadJson(jsonFileObject);
		}

		return retPojo;
	}

	/**
	 * Translate one JSON representation of a MINT DataUpload to a Omnom
	 * FilePojo.
	 * 
	 * @param json
	 * @return
	 */
	public FilePojo parseFilePojoFromDataUploadJson(String jsonStr) {
		JsonObject jsonObj = new JsonParser().parse(jsonStr).getAsJsonObject();
		return parseFilePojoFromDataUploadJson(jsonObj);
	}

	/**
	 * Translate one JSON representation of a MINT DataUpload to a Omnom
	 * FilePojo.
	 * 
	 * TODO user TODO organization
	 * 
	 * @param json
	 * @return
	 */
	private FilePojo parseFilePojoFromDataUploadJson(JsonObject json) {

		log.trace(LogbackMarkers.DATA_DUMP, "JSON to parse as DataUpload: {}", json);

		final String uploadID = json.get("dbID").getAsString();
		final String datasetType = json.get("type").getAsString();
		if (!datasetType.equals("DataUpload"))
			return null;

		FilePojo fp = new FilePojo();

		fp.setId(mint_file_base + "/upload" + uploadID);
		fp.setCreated(DateTime.parse(json.get("created").getAsString()));
		fp.setLastModified(DateTime.parse(json.get("lastModified").getAsString()));
		fp.setOriginalName(json.get("name").getAsString());
		fp.setLabel(json.get("name").getAsString());

		if (null != json.get("itemRootXpath")) {
			fp.setItemRootXPath(json.get("itemRootXpath").getAsString());
		}
		if (null != json.get("itemLabelXpath")) {
			fp.setItemLabelXPath(json.get("itemLabelXpath").getAsString());
		}

		// http://mint-projects.image.ntua.gr/dm2e/Download?datasetId=1026
		if (null != json.get("format")) {
			String mintFormat = json.get("format").getAsString();
			if ("TGZ-XML".equals(mintFormat))
				fp.setFileType(NS.OMNOM_TYPES.TGZ_XML);
			else if ("ZIP-XML".equals(mintFormat))
				fp.setFileType(NS.OMNOM_TYPES.ZIP_XML);
			else if ("XML".equals(mintFormat))
				fp.setFileType(NS.OMNOM_TYPES.XML);
			else {
				String msg = "Unknown format of this DataUpload: " + mintFormat;
				log.error(msg);
				throw new RuntimeException(msg);
			}
		} else {
			log.warn("In absence of explicit format, setting fileType to" + NS.OMNOM_TYPES.XML);
			fp.setFileType(NS.OMNOM_TYPES.XML);
		}

		// Set internalFileLocation to the Download?datasetId=... URI
		fp.setInternalFileLocation(String
				.format("%sDownload?datasetId=%s", mint_api_base, uploadID));

		// If the file is a single XML, let our convertTGZtoXML logic
		// dereference it (/{id}/data path in MintFileService, otherwise just
		// use the internalFileLocation
		if (fp.getFileType().toString().equals(NS.OMNOM_TYPES.XML)) {
			fp.setFileRetrievalURI(URI.create(fp.getId() + "/data"));
		} else {
			fp.setFileRetrievalURI(fp.getInternalFileLocation());
		}

		return fp;
	}

	/**
	 * Translate one JSON representation of a MINT Mapping to a Omnom FilePojo.
	 * 
	 * @param json
	 * @return
	 */
	public FilePojo parseFilePojoFromMappingJson(String jsonStr) {
		JsonObject jsonObj = new JsonParser().parse(jsonStr).getAsJsonObject();
		return parseFilePojoFromMappingJson(jsonObj);
	}

	/**
	 * Translate one JSON representation of a MINT Mapping to a Omnom FilePojo.
	 * TODO user TODO organization
	 * 
	 * @param json
	 * @return
	 */
	private FilePojo parseFilePojoFromMappingJson(JsonObject json) {

		log.trace(LogbackMarkers.DATA_DUMP, "JSON to parse as Mapping: {}", json);

		// http://mint-projects.image.ntua.gr/dm2e/Home.action?kConnector=html.page&
		// url=DoMapping.action%3Fmapid%3D1166%26uploadId%3D1130&kTitle=Mapping+Tool

		final String mappingID = json.get("dbID").getAsString();
		FilePojo fp = new FilePojo();
		fp.setId(mint_file_base + "/mapping" + mappingID);
		fp.setMediaType("application/xslt+xml");
		fp.setFileType(NS.OMNOM_TYPES.XSLT);
		fp.setLabel(json.get("name").getAsString());
		fp.setLastModified(DateTime.parse(json.get("lastModified").getAsString()));
		if (null != json.get("uploadId")) {
			String uploadID = json.get("uploadId").getAsString();
			// http://mint-projects.image.ntua.gr/dm2e/MappingOptions.action?selaction=downloadxsl&selectedMapping=1166&uploadId=1130&isApi=true
			fp
					.setFileRetrievalURI(URI
							.create(String
									.format("%s/MappingOptions?api=true&selaction=downloadxsl&selectedMapping=%s&uploadID=%s",
											mint_api_base,
											mappingID,
											uploadID)));
			try {
				fp.setFileEditURI(URI.create(mint_api_base
						+ "Home.action?kConnector=html.page&url=DoMapping.action"
						+ URLEncoder.encode(String
								.format("?mapid=%s&uploadid=%s&kTitle=Mapping+Tool",
										mappingID,
										uploadID), "UTF-8")));
			} catch (UnsupportedEncodingException e) {
				log.error("uploadID resulted in invalid UTF-8: ", e);
				throw new RuntimeException(e);
			}
		} else {
			fp.setFileRetrievalURI(URI.create(String.format("%sUrlApi?type=Mappingxsl&id=%s",
					mint_api_base,
					mappingID)));
		}

		return fp;
	}

}