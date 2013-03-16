package eu.dm2e.ws.services.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlUpdate;

/**
 * The service includes all necessary methods to upload a new file (no matter
 * which mimetype) to a file storage and/or create a new file reference in the
 * system for a file which is already stored in a given place.
 * 
 * @author Konstantin Baierer
 * @author Robert Meusel
 * 
 */
@Path("/file")
public class FileService extends AbstractRDFService {

	// private static Configuration config = Config.getConfig();
	private static final String STORAGE_ENDPOINT = Config.getString("dm2e.ws.sparql_endpoint");
	private static final String STORAGE_ENDPOINT_STATEMENTS = Config
		.getString("dm2e.ws.sparql_endpoint_statements");

	private static final String NS_DM2E = Config.getString("dm2e.ns.dm2e");
	private static final String PROP_DM2E_FILE_RETRIEVAL_URI = NS_DM2E + "file_retrieval_uri";
	private static final String PROP_DM2E_FILE_LOCATION = NS_DM2E + "file_location";

	// this defines the states a file can be in, in addition to the
	// HTTP response when retrieving the file data.
	private enum FileStatus {
		AVAILABLE, // the file can be retrieved
		WAITING, // the file is not yet ready
		DELETED // the file was deleted
	}

	Logger log = Logger.getLogger(getClass().getName());

	/**
	 * Saves the sent file to disk and stores derived metadata in the graph
	 * {@code uri} of Grafeo {@code g}
	 * 
	 * @param filePart
	 *            the file BodyPart
	 * @param fileDisposition
	 *            the file's content disposition
	 * @param g
	 *            The graph this file should be stored in
	 * @param uriStr
	 *            The URI that represents this file
	 * @return the file as a {@link File}
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * 
	 */
	private void storeAndDescribeFile(
			InputStream fileInStream,
			GrafeoImpl g,
			URI uri)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		// store the file
		// TODO think about where to store
		MessageDigest md;
		md = MessageDigest.getInstance("MD5");

		DigestInputStream fileDigestInStream = new DigestInputStream(fileInStream, md);
		// @formatter:off
		// the file name will be the URI with everything non-alpha-numeric deleted
		String uriStr = uri.toString();
		String fileName = uriStr.replaceAll("[^A-Za-z0-9_]", "_");
		fileName = fileName.replaceAll("__+", "_");
		File f = new File(String.format(
				"%s/%s",
				Config.getString("dm2e.service.file.store_directory"), 
				fileName));
		IOUtils.copy(fileDigestInStream, new FileOutputStream(f));
		// @formatter:on

		// calculate and format checksum
		byte[] mdBytes = md.digest();
		StringBuilder mdStrBuilder = new StringBuilder();
		for (int i = 0; i < mdBytes.length; i++) {
			mdStrBuilder.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		String mdStr = mdStrBuilder.toString();

		// TODO add right predicates here
		// store file-based/implicit metadata
		g.addTriple(uriStr, "dm2e:md5", g.literal(mdStr));
		g.addTriple(uriStr, "dm2e:file_location", g.literal(f.getAbsolutePath()));
		g.addTriple(uriStr, "dm2e:file_retrival_uri", uriStr);
		g.addTriple(uriStr, "dm2e:file_size", g.literal(f.length()));

		// these are only available if this is an upload field and not just a
		// form field
//		if (!filePart.isSimple()) {
			// TODO this is wrong most of the time
//			g.addTriple(uriStr, "dct:format", g.literal(filePart.getMediaType().toString()));
//			g.addTriple(uriStr, "dm2e:original_name", g.literal(fileDisposition.getFileName()));
//		}
	}
	
	/**
	 * TODO This is meant to be used for placeholders, so that a service can
	 * register a file and put it at the location later.
	 * 
	 * @return
	 */
	@POST
	@Path("empty")
	public Response postEmptyFile() {
		ByteArrayInputStream fakeInStream = new ByteArrayInputStream("".getBytes());
		// The model for this resource
		GrafeoImpl g = new GrafeoImpl();
		// Identifier for this resource (used in URI generation and for filename)
		String uniqueStr = createUniqueStr();
		// name of the new resource
		String uriStr = Config.getString("dm2e.service.file.store_prefix") + uniqueStr;
		URI uri;
		try {
			uri = new URI(uriStr);
		} catch (URISyntaxException e) {
			return throwServiceError(e);
		}
		try {
			storeAndDescribeFile(fakeInStream, g, uri);
		} catch (IOException | NoSuchAlgorithmException e) {
			return throwServiceError(e);
		}
		
		// set the status of the file to waiting
		g.addTriple(uri.toString(), "dm2e:fileStatus", g.literal(FileStatus.WAITING.toString()));
		
		// save it
		g.writeToEndpoint(STORAGE_ENDPOINT_STATEMENTS, uriStr);
		
		return Response.created(uri).entity(getResponseEntity(g)).build();
	}
	
	/**
	 * TODO
	 * @param metaPart
	 * @param metaDisposition
	 * @param filePart
	 * @param fileDisposition
	 * @param uri
	 * @return
	 */
	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response putFileAndOrMetadata(
			@FormDataParam("meta") FormDataBodyPart metaPart,
			@FormDataParam("meta") FormDataContentDisposition metaDisposition,
			@FormDataParam("file") FormDataBodyPart filePart,
			@FormDataParam("file") FormDataContentDisposition fileDisposition,
			@QueryParam("uri") String uriStr
			) {
		URI uri;
		try {
			uri = getUriForString(uriStr);
		} catch (URISyntaxException e) {
			return throwServiceError(e);
		}
		
		// TODO Ideally we'd only have to do this:
		//		Grafeo g = new GrafeoImpl(uri.toString());
		// Unfortunately, Jena doesn't seem to be doing content-negotiation in a way that 
		// is compatible with ours. Must investigate deeper and fix this in GrafeoImpl
		Grafeo g;
		try {
			g = getGrafeoForUriWithContentNegotiation(uri);
		} catch (Exception e) {
			return throwServiceError("Could not read from URI: " + e);
		}
		
		// check if the uri is known
		if (null == g)
			return throwServiceError("Could not find metadata about URI");
		
		// TODO if metadata: delete graph, create new one
		// TODO if file data: store file at location
		
		return Response.ok(getResponseEntity(g)).build();
	}
	
	
	/**
	 * A file can be uploaded and/or meta-data for the/a file can be saved to
	 * the storage system. If no file is given, the metadata have to include the
	 * location of the file where it can be found.
	 * 
	 * @todo If a client sends a multipart/form-data without multipart
	 *       boundaries, this method is never reached and a silent 400 is sent
	 *       to the client
	 * 
	 * @param metaPart
	 *            the part with the metadata in RDF (either an upload or a
	 *            simple field)
	 * @param metaDisposition
	 *            the content disposition of meta if meta is an upload
	 * @param filePart
	 *            the part with the file data (should be an upload)
	 * @param fileDisposition
	 *            the content disposition of meta if meta is an upload
	 * @return
	 * @return The complete RDF data about the file, which are stored by this
	 *         service are returned.
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(
			@FormDataParam("meta") FormDataBodyPart metaPart,
			@FormDataParam("meta") FormDataContentDisposition metaDisposition,
			@FormDataParam("file") FormDataBodyPart filePart,
			@FormDataParam("file") FormDataContentDisposition fileDisposition) {

		// The model for this resource
		GrafeoImpl g = new GrafeoImpl();
		// Identifier for this resource (used in URI generation and for filename)
		String uniqueStr = createUniqueStr();
		// name of the new resource
		String uriStr = Config.getString("dm2e.service.file.store_prefix") + uniqueStr;
		URI uri;
		try {
			uri = new URI(uriStr);
		} catch (URISyntaxException e) {
			return throwServiceError(e);
		}

		//
		// Sanity check
		//
		if (filePart == null && metaPart == null) {
			String msg = "Can't add a file witout 'meta' and/or 'file' field.";
			log.warning(msg);
			return throwServiceError(msg);
		}

		// metadata is present
		if (metaPart != null) {
			
			String metaStr = metaPart.getValueAs(String.class);
			
			// try to read the metadata
			try {
				if (metaStr.length() != 0)
					g.readHeuristically(metaStr);
			} catch (RuntimeException e) {
				return throwServiceError(e);
			}
			// rename top blank node to the newly minted URI if it exists
			if (g.findTopBlank() != null) {
				g.findTopBlank().rename(uri.toString());
			}			
			
			// if the file part is null, make sure that a
			// dm2e:file_retrieval_uri is provided in meta
			if (filePart == null && ! g.containsStatementPattern(uri.toString(), PROP_DM2E_FILE_RETRIEVAL_URI, "?o")) {
				return throwServiceError("If no 'file' is set, <" + PROP_DM2E_FILE_RETRIEVAL_URI + "> is REQUIRED in 'meta'.");
			}
		}

		// There **is** a file to be processed
		if (filePart != null) { 
			try {
				InputStream fileInStream = filePart.getValueAs(InputStream.class);
				// store and describe file
				storeAndDescribeFile(fileInStream, g, uri);
				// it's stored, set to AVAILABLE
				g.addTriple(uriStr, "dm2e:fileStatus", g.literal(FileStatus.AVAILABLE.toString()));
			} catch (IOException | NoSuchAlgorithmException e) {
				return throwServiceError(e);
			}
		}

		// store RDF data
		g.writeToEndpoint(STORAGE_ENDPOINT_STATEMENTS, uri);
		return Response.created(uri).entity(getResponseEntity(g)).build();
	}

	/**
	 * Retrieve metadata/file data for a locally stored file
	 * 
	 * @param fileId
	 * @return
	 */
	@GET
	@Path("{id}")
	public Response getFileById(@PathParam("id") String fileId) {
		return getFile(getRequestUriWithoutQuery());
	}

	/**
	 * Decides whether to fire the get method for file data or metadata.
	 * 
	 * @param uri
	 * @return
	 */
	private Response getFile(URI uri) {

		// if the accept header is a RDF type, send metadata, otherwise data
		if (DM2E_MediaType.isRdfRequest(headers)) {
			return getFileMetaDataByUri(uri);
		} else {
			return getFileDataByUri(uri);
		}
	}
	
	/**
	 * Retrieve metadata/file data by passing a 'uri' parameter
	 * 
	 * @param uriStr
	 * @return
	 */
	@GET
	public Response getFileByUri(@QueryParam("uri") String uriStr) {

		URI uri;
		try {
			uri = getUriForString(uriStr);
		} catch (URISyntaxException e) {
			return throwServiceError(e);
		}

		// uri is required
		return getFile(uri);
	}

	/**
	 * Returns the metadata of a file identified by the given uri as RDF.
	 * 
	 * @param uri
	 *            the file identifier
	 * @return all stored meta-data about the file
	 */
	private Response getFileMetaDataByUri(URI uri) {
		Grafeo g = new GrafeoImpl();
		try {
			g.readFromEndpoint(STORAGE_ENDPOINT, uri);
		} catch (Exception e) {
			log.info(STORAGE_ENDPOINT);
			return throwServiceError(e);
		}
		if (!g.containsResource(uri)) {
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
	public Response getFileDataByUri(@QueryParam("uri") URI uriObject) {

		String uri = uriObject.toString();

		// create model from graph at uri
		GrafeoImpl g = new GrafeoImpl();
		g.readFromEndpoint(STORAGE_ENDPOINT, uri);

		// Unless the URI is prefixed with the URI prefix the @POST methods uses
		// i.e. if the URI is external, do a redirect
		if ( ! uri.startsWith(Config.getString("dm2e.service.file.store_prefix"))) {
			// redirect
			try {
				return Response.temporaryRedirect(new URI(uri)).build();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		String path;
		try {
			path = g.firstMatchingObject(uri, PROP_DM2E_FILE_LOCATION).toString();
		} catch (NullPointerException e) {
			return throwServiceError("File location of this file is unknown.");
		}
		RDFNode contentTypeNode = g.firstMatchingObject(uri, "dct:format");
		RDFNode originalNameNode = g.firstMatchingObject(uri, "dm2e:original_name");
		String contentType = contentTypeNode != null ? contentTypeNode.toString() : "text/plain";
		String originalName = originalNameNode != null ? originalNameNode.toString() : "rdf_file_info";
		
		FileInputStream fis;
		try {
			log.info(path);
			fis = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			log.info(e.toString());
			return Response.status(404).entity(
					"File '" + path + "' not found on the server. " + e.toString()).build();
		}
		return Response.ok(fis).header("Content-Type", contentType).header("Content-Disposition",
				"attachment; filename=" + originalName).build();
	}
	
	/**
	 * Delete a file from the metadata by marking it with dm2e:fileStatus @{link
	 * {@link FileStatus} DELETED
	 * 
	 * @param uriStr
	 *            The URI to delete
	 * @return
	 */
	@DELETE
	public Response deleteFileByUri(@QueryParam("uri") String uriStr) {

		URI uri;
		try {
			uri = getUriForString(uriStr);
		} catch (URISyntaxException e) {
			return throwServiceError(e);
		}

		// replace all fileStatus statements
//		@formatter:off
		SparqlUpdate s = new SparqlUpdate.Builder()
			.graph(uri.toString())
			.delete("<"+uri+"> <"+NS_DM2E+"fileStatus> ?s")
			.insert("<"+uri+"> <"+NS_DM2E+"fileStatus> \"" + FileStatus.DELETED.toString() +"\"")
			.endpoint(STORAGE_ENDPOINT_STATEMENTS)
			.build();
//		@formatter:on
		log.info(s.toString());
		s.execute();

		// return the metadata live from the store (g is not up-to-date anymore)
		return getFileMetaDataByUri(uri);
	}

	/**
	 * Delete a locally stored file. TODO Only wraps deleteFileByUri for now
	 * without doing on-disk deletion.
	 * 
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("{id}")
	public Response deleteFileById(@PathParam("id") String id) {

		URI uri = getRequestUriWithoutQuery();

		/**
		 * TODO the actual deletion of files on disk. The current storage
		 * solution isn't secure, so this is commented out for now.
		 */
		// String path = g.firstMatchingObject(uri,
		// PROP_DM2E_FILE_LOCATION).toString();
		// File file = new File(path);
		// file.delete();

		return deleteFileByUri(uri.toString());
	}

}
