package eu.dm2e.ws.services.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.logging.Logger;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * The service includes all necessary methods to upload a new file (no matter
 * which mimetype) to a file storage and/or create a new file reference in the
 * system for a file which is already stored in a given place.
 * 
 * @author Robert Meusel
 * 
 */
@Path("/file")
public class FileService extends AbstractRDFService {

	private static final String STORAGE_ENDPOINT = "http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest";
	private static final String STORAGE_ENDPOINT_STATEMENTS = "http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest/statements";
	private static final String WS_ENDPOINT = "http://localhost:9998/file";
	private static final String NS_DM2E = "http://onto.dm2e.eu/onto#";
	private static final String PROP_DM2E_FILE_RETRIEVAL_URI = NS_DM2E + "file_retrieval_uri";
	private static final String PROP_DM2E_FILE_LOCATION = NS_DM2E + "file_location";

	Logger log = Logger.getLogger(getClass().getName());

	/**
	 * A file can be uploaded and/or meta-data for the/a file can be saved to
	 * the storage system. If no file is given, the metadata have to include the
	 * location of the file where it can be found.
	 * 
	 * @param formDataMultiPart
	 *            a {@link FormDataMultiPart} including maximum two
	 *            {@link FormDataBodyPart} which are references by "file" and
	 *            "meta". The file reference includes the file which should be
	 *            uploaded. The "meta" references includes a string which holds
	 *            the meta information as RDF.
	 * @return The complete RDF data about the file, which are stored by this
	 *         service are returned.
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@FormDataParam("meta") FormDataBodyPart metaPart,
			@FormDataParam("meta") FormDataContentDisposition metaDisposition,
			@FormDataParam("file") FormDataBodyPart filePart,
			@FormDataParam("file") FormDataContentDisposition fileDisposition) {

		// Sanity check
		if (filePart == null && metaPart == null) {
			return throwServiceError("Can't add a file witout 'meta' and/or 'file' field.");
		}

		// parse meta data
		GrafeoImpl g = null;
		if (metaPart != null) {
			File metaRaw = metaPart.getValueAs(File.class);
			try {
				g = new GrafeoImpl(metaRaw);
			} catch (Exception e) {
				return throwServiceError(e);
			}
		}
		if (g == null) {
			g = new GrafeoImpl();
		}

		String uri = "http://data.dm2e.eu/file/fromWS/" + new Date().getTime() + "" + UUID.randomUUID().toString();
		// rename top blank node to the uri
		if (g.findTopBlank() != null) {
			g.findTopBlank().rename(uri);
		}
		// create retrieval URI (make sure this matches the paths for the @GET
		// method below
		URI fileRetrievalUri;
		try {
			fileRetrievalUri = new URI(WS_ENDPOINT + "/get?uri=" + uri);
		} catch (URISyntaxException e) {
			return throwServiceError(e);
		}

		if (filePart == null) {
			// if the file part is null, make sure that a
			// dm2e:file_retrieval_uri is provided in meta
			if (!g.containsStatementPattern(uri, PROP_DM2E_FILE_RETRIEVAL_URI, "?o")) {
				return throwServiceError("If no 'file' is set, <" + PROP_DM2E_FILE_RETRIEVAL_URI
						+ "> is REQUIRED in 'meta'.");
			}
		} else {
			// store the file
			// TODO think about where to store
			InputStream fileInStream = filePart.getValueAs(InputStream.class);
			File f = new File("files/upload_" + String.valueOf(new Date().getTime()));
			try {
				IOUtils.copy(fileInStream, new FileOutputStream(f));
			} catch (IOException e) {
				return throwServiceError(e);
			}

			// store file-based/implicit metadata
			// TODO add right predicates here
			if (!filePart.isSimple()) {
				// these are only available if this is a file field not just a
				// form field
				g.addTriple(uri, "dm2e:original_name", g.literal(fileDisposition.getFileName()));
				g.addTriple(uri, "dm2e:file_size", "" + g.literal(""+fileDisposition.getSize()));
				g.addTriple(uri, "dm2e:file_retrival_uri", fileRetrievalUri.toString());
			}
			g.addTriple(uri, "dct:format", g.literal(filePart.getMediaType().toString()));
			g.addTriple(uri, "dm2e:file_location", g.literal(f.getAbsolutePath()));
		}

		g.writeToEndpoint(STORAGE_ENDPOINT_STATEMENTS, uri);
		return Response.created(fileRetrievalUri).entity(getResponseEntity(g)).build();
	}
	
	/**
	 * Decides whether to fire the get method for file data or metadata.
	 * 
	 * @param uri
	 * @return
	 */
	@GET
	public Response getFileByUri(@QueryParam("uri") String uri) {
		
		// uri is required
		if (null == uri)
			return Response.status(400).entity("Must provide 'uri' query parameter").build();
		
		// if the accept header is a RDF type, send metadata, otherwise data
		if (DM2E_MediaType.isRdfRequest(headers)) {
			return getFileMetaDataByUri(uri);
		} else {
			return getFileByUri(uri);
		}
	}



	/**
	 * Returns the metadata of a file identified by the given uri as RDF.
	 * 
	 * @param uri
	 *            the file identifier
	 * @return all stored meta-data about the file
	 */
	@GET
	@Path("meta")
	public Response getFileMetaDataByUri(@QueryParam(value = "uri") String uri) {
		try {
			uri = URLDecoder.decode(uri, "utf8");
		} catch (UnsupportedEncodingException e) {
			return throwServiceError(e);
		}
		Grafeo g = new GrafeoImpl();
		try {
			g.readFromEndpoint(STORAGE_ENDPOINT, uri);
		} catch (Exception e) {
			return throwServiceError(e);
		}
		if (! g.containsResource(uri)) {
			return Response.status(404).entity("No such file in the triplestore.").build();
		}
		// TODO we need a link to the "get" ws where the file can be retrieved
		// from because the internal information is not really useful
		return getResponse(g);
	}

	/**
	 * Returns the file. If the file is not stored by the file storage the
	 * request is redirected. Otherwise the internal file is returned directly.
	 * 
	 * @param uri
	 *            the identifier of the file.
	 * @return the file or redirected to the location of the file.
	 */
	@GET
	@Path("get")
	public Response getFile(@QueryParam("uri") String uri) {

		try {
			uri = URLDecoder.decode(uri, "utf8");
		} catch (UnsupportedEncodingException e) {
			return throwServiceError(e);
			// throw new RuntimeException("An exception occurred: " + e, e);
		}
		
		// create model from graph at uri
		GrafeoImpl g = new GrafeoImpl();
		g.readFromEndpoint(STORAGE_ENDPOINT, uri);
		
		String path = g.firstMatchingObject(uri, PROP_DM2E_FILE_LOCATION).toString();
		String contentType = g.firstMatchingObject(uri, "dct:format").toString();
		String originalName = g.firstMatchingObject(uri, "dm2e:original_name").toString();
		log.info(path);

		if (path.startsWith("http")) {
			// redirect
			URI pathUri;
			try {
				pathUri = new URI(path);
			} catch (URISyntaxException e) {
				return throwServiceError(e);
			}
			return Response.temporaryRedirect(pathUri).build();
		}
		else if (path.startsWith("file")) {
			path = path.replaceAll("^file:[/]+?/","");
		}
		log.info(path);
		FileInputStream fis;
		try {
			fis = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			log.info(e.toString());
			return Response.status(404).entity("File '" + path + "' not found on the server. " + e.toString()).build();
		}
		return Response
				.ok(fis)
				.header("Content-Type", contentType)
				.header("Content-Disposition", "attachment; filename=" + originalName)
				.build();
	}

}
