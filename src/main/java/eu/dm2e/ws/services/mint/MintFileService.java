package eu.dm2e.ws.services.mint;

import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.json.OmnomJsonSerializer;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.AbstractRDFService;

@Path("/mint-file")
public class MintFileService extends AbstractRDFService {
	
	protected MintApiTranslator mintApiTranslator = new MintApiTranslator(
			Config.getString("dm2e.service.mint-file.base_uri"),
			Config.getString("dm2e.service.mint-file.mint_base"),
			Config.getString("dm2e.service.mint-file.username"),
			Config.getString("dm2e.service.mint-file.password")
	);
	
	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.setLabel("MINT file service yay.");
		return ws;
	}
	
	
	/**
	 * GET /
	 * @return
	 */
	@Produces({
//		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
//		DM2E_MediaType.APPLICATION_RDF_XML,
//		DM2E_MediaType.APPLICATION_X_TURTLE,
//		// DM2E_MediaType.TEXT_PLAIN,
//		DM2E_MediaType.TEXT_RDF_N3,
//		DM2E_MediaType.TEXT_TURTLE
		MediaType.WILDCARD
	})
	@GET
	public Response getFileList() {
		Grafeo g = mintApiTranslator.retrieveAllMappingsAndDataUploadsAsGrafeo();
		
		return Response.ok().entity(getResponseEntity(g)).build();
	}
	
	@Produces({
		MediaType.APPLICATION_JSON
	})
	@GET
	public String getFileListJson() {
		List<FilePojo> fileList = mintApiTranslator.retrieveAllMappingsAndDataUploads();
        String jsonStr = OmnomJsonSerializer.serializeToJSON(fileList, FilePojo.class);
        return jsonStr;
	}

	/**
	 * GET /{id}
	 * Retrieve metadata/file data for a file stored in MINT
	 * 
	 * @param fileId
	 * @return
	 */
	@GET
	@Path("{id}")
	public Response getFileById(@PathParam("id") String mintFileId) {
		URI uri = getRequestUriWithoutQuery();
        log.info("File requested: " + uri);
        
		// if the accept header is a RDF or JSONtype, send metadata, otherwise data
		if (expectsMetadataResponse()) {
			log.info("METADATA will be sent");
            return getFileMetadataByUri(uri);
		} else {
            log.info("FILE will be sent");
            return getFileDataByUri(uri);
		}
	}
	
	/**
	 * GET /{id}/data
	 * @param mintFileId
	 * @return
	 */
	@GET
	@Path("{id}/data")
	public Response getFileDataById(@PathParam("id") String mintFileId) {
		return getFileDataByUri(getRequestUriWithoutQuery());
	}

	/**
	 * GET /metadataByURI?uri...
	 * TODO FIXME
	 * @param uri
	 * @return
	 */
	@GET
	@Path("/metadataByURI")
	public Response getFileMetadataByUri(@QueryParam("uri") URI uri) {
		
		FilePojo filePojo = mintApiTranslator.retrieveFilePojoForUri(uri);
		if (null == filePojo)
			return Response.status(404).entity("No such file in MINT.").build();
		
		Response resp;
		if (expectsRdfResponse()) {
			Grafeo outG = new GrafeoImpl();
			outG.getObjectMapper().addObject(filePojo);
			resp = getResponse(outG);
		} else if (expectsJsonResponse()) {
			resp = Response
					.ok()
					.type(MediaType.APPLICATION_JSON)
					.entity(filePojo.toJson())
					.build();
		} else {
			log.warn("No metadata type could be detected, defaulting to RDF/N-TRIPLES.");
			Grafeo outG = new GrafeoImpl();
			outG.getObjectMapper().addObject(filePojo);
			resp = getResponse(outG);
		}
		return resp	;
	}
	
	/**
	 * GET /dataByURI?uri=...
	 * @param uri
	 * @return
	 */
	@GET
	@Path("/dataByURI")
	public Response getFileDataByUri(@QueryParam("uri") URI uri) {
		log.info("Requested data for URI " + uri);
		FilePojo filePojo = mintApiTranslator.retrieveFilePojoForUri(uri);
		if (null == filePojo)
			return Response.status(404).entity("No such file in MINT.").build();
		
		Response resp = null;
		if (filePojo.getFileType().toString().equals(NS.OMNOM_TYPES.XML)) {
			byte[] content = mintApiTranslator.convertTGZtoXML(filePojo);
			if (null != content) {
				resp = Response.ok()
						.entity(content)
						.type("application/xml")
						.header("Content-Disposition","attachment; filename = " + filePojo.getOriginalName())
						.build();
			} else {
				log.info("No content could be read from targ.gz");
				resp = Response.seeOther(URI.create(filePojo.getInternalFileLocation())) .build();
			}
		}
		if (null == resp)
			resp = Response.seeOther(filePojo.getFileRetrievalURI()).build();
		return resp;
	}

}