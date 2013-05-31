package eu.dm2e.ws.services.file;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlUpdate;
import eu.dm2e.ws.httpmethod.PATCH;
import eu.dm2e.ws.services.AbstractRDFService;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

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
	private static final String 
			SERVICE_URI = Config.getString("dm2e.service.file.base_uri"),
			STORAGE_ENDPOINT = Config.getString("dm2e.ws.sparql_endpoint"),
			STORAGE_ENDPOINT_STATEMENTS = Config.getString("dm2e.ws.sparql_endpoint_statements"),
			
			NS_DM2E = Config.getString("dm2e.ns.dm2e");
//			PROP_DM2E_FILE_RETRIEVAL_URI = NS_DM2E + "file_retrieval_uri",
//			PROP_DM2E_FILE_LOCATION = NS_DM2E + "file_location";

	// this defines the states a file can be in, in addition to the
	// HTTP response when retrieving the file data.
	private enum FileStatus {
		AVAILABLE, // the file can be retrieved
		WAITING, // the file is not yet ready
		DELETED // the file was deleted
	}

	Logger log = Logger.getLogger(getClass().getName());


    public FileService() {

    }

    /**
	 * Saves the sent file to disk and stores derived metadata in the graph
	 * {@code uri} of Grafeo {@code g}
	 * 
	 * @para FileInputStream 
	 *            The file as an InputStream
	 * @param oldG
	 *            The graph this file should be stored in
	 * @param uri
	 *            The URI that represents this file
	 * @return 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * 
	 */
	private FilePojo storeAndDescribeFile(
			InputStream fileInStream,
			Grafeo oldG,
			URI uri) throws FileNotFoundException, IOException {
		// store the file
		// TODO think about where to store
		MessageDigest md = null;
        byte[] mdBytes = null;
		try {
			md = MessageDigest.getInstance("MD5");
            mdBytes = md.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 algorithm not available: " + e, e);
		}

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

		StringBuilder mdStrBuilder = new StringBuilder();
        for (byte mdByte : mdBytes) {
            mdStrBuilder.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
        }
		String mdStr = mdStrBuilder.toString();

		// TODO add right predicates here
		// store file-based/implicit metadata
		FilePojo filePojo = new FilePojo();
		filePojo.setMd5(mdStr);
		filePojo.setFileLocation(f.getAbsolutePath()); // TODO not a good "solution"
		filePojo.setId(uri.toString());
		filePojo.setFileRetrievalURI(uri);
		filePojo.setFileSize(f.length());

		// these are only available if this is an upload field and not just a
		// form field
		oldG.getObjectMapper().addObject(filePojo);
		
		return filePojo;
	}
	
	/**
	 * Posts an empty file
	 * TODO This is meant to be used for placeholders, so that a service can
	 * register a file and put it at the location later.
	 * @return
	 */
	@POST
	@Path("empty")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response postEmptyFile(
			@FormDataParam("meta") FormDataBodyPart metaPart
			) {
		ByteArrayInputStream fakeInStream = new ByteArrayInputStream("".getBytes());
		// The model for this resource
		GrafeoImpl g = new GrafeoImpl();
		// Identifier for this resource (used in URI generation and for filename)
		String uniqueStr = createUniqueStr();
		// name of the new resource
		String uriStr = SERVICE_URI + "/" + uniqueStr;
		URI uri;
		try {
			uri = new URI(uriStr);
			uriStr = uri.toString();
		} catch (URISyntaxException e) {
			return throwServiceError(e);
		}
		try {
			storeAndDescribeFile(fakeInStream, g, uri);
		} catch (IOException e) {
			return throwServiceError(e);
		}
		
		if (metaPart != null) {
			
			String metaStr = metaPart.getValueAs(String.class);
			
			// try to read the metadata
			try {
				if (metaStr.length() != 0) {
					g.readHeuristically(metaStr);
				}
			} catch (RuntimeException e) {
				return throwServiceError(e);
			}
			// rename top blank node to the newly minted URI if it exists
			if (g.findTopBlank() != null) {
				g.findTopBlank().rename(uri.toString());
			}			
			
		}
		
		// set the status of the file to waiting
		g.addTriple(uriStr, "dm2e:fileStatus", g.literal(FileStatus.WAITING.toString()));
		
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
	 * @param uriStr
	 * @return
	 */
	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response putFileAndOrMetadata(
			@FormDataParam("meta") FormDataBodyPart metaPart,
			@FormDataParam("meta") FormDataContentDisposition metaDisposition,
			@FormDataParam("file") FormDataBodyPart filePart,
			@FormDataParam("file") FormDataContentDisposition fileDisposition,
			@QueryParam("uri") String uriStr) {
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
		Grafeo oldG;
		try {
			oldG = getGrafeoForUriWithContentNegotiation(uri);
		} catch (Exception e) {
			return throwServiceError("Could not read from URI: " + e);
		}
		// check if the uri is known
		if (null == oldG)
			return throwServiceError("Could not find metadata about URI");
		
		if (null != metaPart) {
			// build a model from the input
			GrafeoImpl newG = new GrafeoImpl(metaPart.getValueAs(InputStream.class));
			if (newG.getModel().isEmpty()) {
				return throwServiceError("Couldn't parse input model.");
			}
	
			// if metadata: delete graph, create new one
			
			// rename the top blank node to uri
			if (newG.findTopBlank() != null) {
				newG.findTopBlank().rename(uriStr);
			}			
			
			// create SPARQL update query
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.graph(uriStr)
				.delete(oldG.getNTriples())
				.insert(newG.getNTriples())
				.endpoint(STORAGE_ENDPOINT_STATEMENTS)
				.build();
			
			log.info("About to replace: " + sparul.toString());
			
			// save the data
			sparul.execute();
			
			oldG = newG;
			
		} // end of metadata replacement
		
		// TODO if file data: store file at location
		if (null != filePart) {
			InputStream fileInStream = filePart.getValueAs(InputStream.class);
			try {
				storeAndDescribeFile(fileInStream, oldG, uri);
			} catch (IOException e) {
				return throwServiceError("Couldn't write out file "+e);
			}
			
		}
		
		return Response.ok(getResponseEntity(oldG)).build();
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
        log.info("A new file is to be stored here.");
		// The model for this resource
		GrafeoImpl g = new GrafeoImpl();
		// Identifier for this resource (used in URI generation and for filename)
		String uniqueStr = createUniqueStr();
		// name of the new resource
		String uriStr = SERVICE_URI + "/" + uniqueStr;
		log.info("Its URI will be: " + uriStr);
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

        log.info("Everything seems to be sane so far...");
		// metadata is present
		if (metaPart != null) {
			
			String metaStr = metaPart.getValueAs(String.class);
			
			// try to read the metadata
			try {
				if (metaStr.length() != 0) {
					g.readHeuristically(metaStr);
				}
			} catch (RuntimeException e) {
				return throwServiceError(e);
			}
			// rename top blank node to the newly minted URI if it exists
			if (g.findTopBlank() != null) {
				g.findTopBlank().rename(uri.toString());
			}			
			
			// if the file part is null, make sure that a
			// dm2e:file_retrieval_uri is provided in meta
			FilePojo f = g.getObjectMapper().getObject(FilePojo.class, uri);
			if (filePart == null && null == f.getFileRetrievalURI()) {
				return throwServiceError("If no 'file' is set, omnom:fileRetrievalURI is REQUIRED in 'meta'.");
			}
		}

        log.info("Metadata is processed.");

		// There **is** a file to be processed
		if (filePart != null) { 
			try {
                log.info("We start to read the file...");
				InputStream fileInStream = filePart.getValueAs(InputStream.class);
				// store and describe file
				FilePojo filePojo = storeAndDescribeFile(fileInStream, g, uri);
				if (!filePart.isSimple()) {
					// TODO this is wrong most of the time
					filePojo.setMediaType(filePart.getMediaType().toString());
					filePojo.setOriginalName(fileDisposition.getFileName());
				}
				// it's stored, set to AVAILABLE
				filePojo.setFileStatus(FileStatus.AVAILABLE.toString());
				g.getObjectMapper().addObject(filePojo);
			} catch (IOException e) {
                log.severe("An exception occured during file reading: " + e);
				return throwServiceError(e);
			}
		}

        log.info("File is hopefully stored.");

        log.info("Final RDF to be stored for this file: " + g.getTurtle());
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
	
	/**
	 * Retrieve metadata/file data by passing a 'uri' parameter
	 * 
	 * @param uriStr
	 * @return
	 */
	@GET
    @Path("byURI")
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
		FilePojo filePojo = g.getObjectMapper().getObject(FilePojo.class, g.resource(uri));
		Grafeo outG = new GrafeoImpl();
		outG.getObjectMapper().addObject(filePojo);
		// TODO we need a link to the "get" ws where the file can be retrieved
		// from because the internal information is not really useful
		return getResponse(outG);
	}

	/**
	 * Returns the file. If the file is not stored by the file storage the
	 * request is redirected. Otherwise the internal file is returned directly.
	 * 
	 * @param uriObject
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
		if ( ! uri.startsWith(SERVICE_URI)) {
			// redirect
			try {
				return Response.temporaryRedirect(new URI(uri)).build();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
//		String path;
//		try {
//			path = g.firstMatchingObject(uri, PROP_DM2E_FILE_LOCATION).toString();
//		} catch (NullPointerException e) {
//			return throwServiceError("File location of this file is unknown.");
//		}
//		GLiteral contentTypeNode = (GLiteral) g.firstMatchingObject(uri, "dct:format");
//		GLiteral originalNameNode = (GLiteral) g.firstMatchingObject(uri, "dm2e:original_name");
//		String contentType = contentTypeNode != null ? contentTypeNode.toString() : "text/plain";
//		String originalName = originalNameNode != null ? originalNameNode.toString() : "rdf_file_info";
		
		FilePojo filePojo = g.getObjectMapper().getObject(FilePojo.class, uri);
		
		FileInputStream fis;
		try {
			log.info(filePojo.getFileLocation());
			fis = new FileInputStream(filePojo.getFileLocation());
		} catch (FileNotFoundException e) {
			log.info(e.toString());
			return Response.status(404).entity(
					"File '" + filePojo.getFileLocation() + "' not found on the server. " + e.toString()).build();
		}
		return Response.ok(fis).header("Content-Type", filePojo.getMediaType()).header("Content-Disposition",
				"attachment; filename=" + filePojo.getOriginalName()).build();
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

	/**
	 * Replace statements about a file with new statements.
	 * @param uriStr
	 * @param bodyInputStream
	 * @return
	 */
	@PATCH
	public Response updateStatementByUri(
			@QueryParam("uri") String uriStr,
			InputStream bodyInputStream) {
		
		// Check if the data is of a RDF content type
		if (DM2E_MediaType.noRdfRequest(headers)) {
			return throwServiceError("The request must be of a RDF type", 406);
		}
		
		URI uri;
		String deleteClause = "", insertClause = "";
		
		// validate URI
		try {
			uri = getUriForString(uriStr);
			if (uri==null) {
				throw new NullPointerException();
			}
		} catch (URISyntaxException | NullPointerException e) {
			return throwServiceError("The URI is not valid", 400);
		}
		
		// find the original metadata
		try {
			Grafeo g = getGrafeoForUriWithContentNegotiation(uriStr);
			if (g==null) {
				throw new NullPointerException();
			}
		} catch (IOException | NullPointerException | URISyntaxException e) {
			return throwServiceError("Couldn't find graph for URI.");
		}
		
		// build a model from the input
		GrafeoImpl patchModel = new GrafeoImpl(bodyInputStream);
		if (patchModel.getModel().isEmpty()) {
			return throwServiceError("Couldn't parse input model.");
		}

		// rename the top blank node to uri
		if (patchModel.findTopBlank() != null) {
			patchModel.findTopBlank().rename(uriStr);
		}			
		
		// create SPARQL DELETE clause
		StmtIterator iter = patchModel.getModel().listStatements();
		int i = 0;
		while (iter.hasNext()) {
			Statement stmt = iter.next();
			deleteClause += String.format("<%s> <%s> ?var%d .\n", 
					stmt.getSubject().toString(),
					stmt.getPredicate().toString(),
					i);
		}
		
		// create SPARQL INSERT clause
		insertClause = patchModel.getNTriples();
		
		// create SPARQL update query
		SparqlUpdate sparul = new SparqlUpdate.Builder()
			.graph(uriStr)
			.delete(deleteClause)
			.insert(insertClause)
			.endpoint(STORAGE_ENDPOINT_STATEMENTS)
			.build();
		
		log.info(sparul.toString());
		
		// save the data
		sparul.execute();
		
		// return the updated version to the client
		return getFileMetaDataByUri(uri);
	}
}
