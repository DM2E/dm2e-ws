package eu.dm2e.ws.services.mint;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.AbstractRDFService;

@Path("/mint-file")
public class MintFileService extends AbstractRDFService {
	
	private MintApiTranslator mintApiTranslator = new MintApiTranslator(
			Config.getString("dm2e.service.mint-file.base_uri"),
			Config.getString("dm2e.service.mint-file.mint_base")
	);
	
	protected Set<Cookie> cookies = new HashSet<>();
	private static final String URI_URLAPI = Config.getString("dm2e.service.mint-file.uri_urlapi");
	private static final String URI_LOGIN = Config.getString("dm2e.service.mint-file.uri_login");
	private static final String URI_HOME = Config.getString("dm2e.service.mint-file.uri_home");
	
	private static final String USERNAME = Config.getString("dm2e.service.mint-file.username");
	private static final String PASSWORD = Config.getString("dm2e.service.mint-file.password");
	
	private Builder webResourceWithCookies(String uri) {
		Builder reqB = client.resource(uri).getRequestBuilder();
		for (Cookie cookie : cookies) {
			log.info("Adding cookie to web resource: " + cookie);
			reqB.cookie(cookie);
		}
		return reqB;
	}
	
	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.setLabel("MINT file service yay.");
		return ws;
	}
	
	/**
	 * Ensures that the service is logged into MINT.
	 * 
	 * @return true if logged in, false otherwise
	 */
	protected boolean isLoggedIn() {
		client.getJerseyClient().setFollowRedirects(false);
		ClientResponse resp = webResourceWithCookies(URI_HOME).get(ClientResponse.class);
		log.info("isLoggedIn response: " + resp);
		client.getJerseyClient().setFollowRedirects(true);
		return resp.getStatus() == 200;
		
	}
	
	/**
	 * Make sure that we're logged in into MINT so the UrlApi works.
	 */
	protected void ensureLoggedIn() {
		WebResource wr = client.resource(URI_LOGIN);
		if (isLoggedIn())
			return;
		else 
			this.cookies.clear();
		
		MultivaluedMap<String,String> form = new MultivaluedMapImpl();
		form.add("username", USERNAME);
		form.add("password", PASSWORD);
		client.getJerseyClient().setFollowRedirects(false);
		ClientResponse resp = wr
				.type(MediaType.APPLICATION_FORM_URLENCODED)
				.entity(form)
				.post(ClientResponse.class);
		client.getJerseyClient().setFollowRedirects(true);
		
		final URI location = resp.getLocation();
		log.fine("Login Response: " + resp);
		if (resp.getStatus() != 302 || null == location) {
			log.severe("Login failed :(");
			return;
		}
		
		log.fine("Login redirect location: " + location);
		List<NewCookie> theseCookies = resp.getCookies();
		log.severe("Cookies sent back: " + theseCookies);
		for (Cookie thisCookie : theseCookies) {
			this.cookies.add(thisCookie);
		}
		
	}
	
	@Produces({
		MediaType.WILDCARD
	})
	@GET
	@Path("/")
	public Response getFileList() {
		
		
		Grafeo g = buildGrafeoFromMintFiles();
		
		return Response.ok().entity(getResponseEntity(g)).build();
	}

	/**
	 * @return
	 */
	private Grafeo buildGrafeoFromMintFiles() {
		ensureLoggedIn();
		Grafeo g = new GrafeoImpl();
		log.info("Add mappings");
		{
			// http://mint-projects.image.ntua.gr/dm2e/UrlApi?isApi=true&action=list&type=Mapping
			String mappingListUri = URI_URLAPI + "action=list&type=Mapping";
			ClientResponse resp = webResourceWithCookies(mappingListUri).get(ClientResponse.class);
			final String respStr = resp.getEntity(String.class);
			if (resp.getStatus() > 200)
				throw new RuntimeException(respStr);
			final List<FilePojo> list = mintApiTranslator.fileListFromMappingList(respStr);
			for (FilePojo fp : list) g.getObjectMapper().addObject(fp);
		}
		log.info("Add uploads");
		{
			// http://mint-projects.image.ntua.gr/dm2e/UrlApi?isApi=true&action=list&type=Dataset
			String datasetListUri = URI_URLAPI + "action=list&type=Dataset";
			ClientResponse resp = webResourceWithCookies(datasetListUri).get(ClientResponse.class);
			final String respStr = resp.getEntity(String.class);
			if (resp.getStatus() > 200)
				throw new RuntimeException(respStr);
			final List<FilePojo> list = mintApiTranslator.fileListFromDatasetDataUpload(respStr);
			log.info("# of datauploads: " + list.size());
			for (FilePojo fp : list) g.getObjectMapper().addObject(fp);
		}
		return g;
	}
	
	/**
	 * @param uri
	 * @return
	 */
	private FilePojo getFilePojoForUri(URI uri) {
		Grafeo g = buildGrafeoFromMintFiles();
		if (!g.containsResource(uri)) {
			return null;
		}
		FilePojo filePojo = g.getObjectMapper().getObject(FilePojo.class, uri);
		return filePojo;
	}
	
	/**
	 * Retrieve metadata/file data for a file stored in MINT
	 * 
	 * @param fileId
	 * @return
	 */
	@GET
	@Path("{id}")
	public Response getFileById() {
		return getFile(getRequestUriWithoutQuery());
	}

	/**
	 * Decides whether to fire the get method for file data or metadata.
	 * 
	 * @param uri
	 * @return
	 */
	Response getFile(URI uri) {
        log.info("File requested: " + uri);
		// if the accept header is a RDF type, send metadata, otherwise data
		if (DM2E_MediaType.expectsRdfResponse(headers)) {
			log.info("METADATA will be sent");
            return getFileMetaDataByUri(uri);
		} else {
            log.info("FILE will be sent");
            return getFileDataByUri(uri);
		}
	}


	private Response getFileMetaDataByUri(URI uri) {
		FilePojo filePojo = getFilePojoForUri(uri);
		if (null == filePojo)
			return Response.status(404).entity("No such file in MINT.").build();
		Grafeo outG = new GrafeoImpl();
		outG.getObjectMapper().addObject(filePojo);
		return getResponse(outG);
	}

	
	private Response getFileDataByUri(URI uri) {
		FilePojo filePojo = getFilePojoForUri(uri);
		if (null == filePojo)
			return Response.status(404).entity("No such file in MINT.").build();
		return Response.seeOther(filePojo.getFileRetrievalURI()).build();
	}

}