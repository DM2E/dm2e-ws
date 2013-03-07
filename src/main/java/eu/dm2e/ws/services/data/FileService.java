package eu.dm2e.ws.services.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

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

	private static final String STORAGE_ENDPOINT = "http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest/statements";
	private static final String WS_ENDPOINT = "http://localhost:9998/file";

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
	@Path("upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(final FormDataMultiPart formDataMultiPart) {

		if (formDataMultiPart == null) {
			// TODO throw exception
		}
		Grafeo g = null;

		final FormDataBodyPart metaPart = formDataMultiPart.getField("meta");
		if (metaPart != null && metaPart.isSimple()) {
			g = new GrafeoImpl(metaPart.getValue(), null);
		}
		if (g == null) {
			g = new GrafeoImpl();
		}
		String uri = "";
		if (g.findTopBlank() == null) {
			uri = "http://data.dm2e.eu/file/fromWS/" + new Date().getTime();
		} else {
			uri = g.findTopBlank().getUri();
		}

		final FormDataBodyPart filePart = formDataMultiPart.getField("file");
		if (filePart != null && !filePart.isSimple()) {
			// TODO think about where to store
			File f = new File(String.valueOf(new Date().getTime()));
			try {
				IOUtils.copy(filePart.getValueAs(InputStream.class),
						new FileOutputStream(f));
				// TODO add right predicates here
				g.addTriple(uri, "dct:format", filePart.getMediaType()
						.toString());
				g.addTriple(uri, "http://dm2e.eu/terms/original_name", filePart
						.getContentDisposition().getFileName());
				g.addTriple(uri, "http://dm2e.eu/terms/file_retrival_uri",
						WS_ENDPOINT + "/get?uri=" + uri);
				g.addTriple(uri, "http://dm2e.eu/terms/file_location",
						f.getAbsolutePath());
			} catch (Exception e) {
				// TODO return something senseful
				throw new RuntimeException("Could not save file.");
			}
		} else {
			// we need to check if the path for the file is set
		}
		g.writeToEndpoint(STORAGE_ENDPOINT, uri);
		return getResponse(g);
	}

	/**
	 * Returns the metadata of a file identified by the given uri as RDF.
	 * 
	 * @param uri
	 *            the file identifier
	 * @return all stored meta-data about the file
	 */
	@GET
	@Path("get/meta")
	public Response getFileDataByUri(@QueryParam(value = "uri") String uri) {
		try {
			uri = URLDecoder.decode(uri, "utf8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("An exception occurred: " + e, e);
		}
		Grafeo g = new GrafeoImpl();
		g.readFromEndpoint(STORAGE_ENDPOINT, uri);
		// TODO we need a link to the "get" ws where the file can be retrieved
		// from because the internal information is not really useful
		return getResponse(g);
	}

	/**
	 * Returns the file. If the file is not stored by the file storage the
	 * request is redirected. Otherwise the internal file is returned directly.
	 * 
	 * @param uri the identifier of the file.
	 * @return the file or redirected to the location of the file.
	 */
	@GET
	@Path("get")
	public Response getFile(@QueryParam(value = "uri") String uri) {

		try {
			uri = URLDecoder.decode(uri, "utf8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("An exception occurred: " + e, e);
		}
		Grafeo g = new GrafeoImpl();
		g.readTriplesFromEndpoint(STORAGE_ENDPOINT, uri,
				"http://dm2e.eu/terms/file_location", null);
		// TODO get the path out of the
		String path = "";

		ResponseBuilder response = null;
		if (path.startsWith("http")) {
			// redirect
			try {
				response = Response.temporaryRedirect(new URI(path));
			} catch (URISyntaxException e) {
				throw new RuntimeException("An exception occurred: " + e, e);
			}
		} else {
			File f = new File(path);
			response = Response.ok((Object) f);
			response.header("Content-Disposition", "attachment");
		}
		return response.build();
	}

}
