package eu.dm2e.ws.services.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * @author Robert Meusel
 * 
 */
@Path("/file")
public class FileService extends AbstractRDFService {

	@POST
	@Path("upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(final FormDataMultiPart formDataMultiPart) {

		// we assume that a valid Input can consists out of
		// - Only meta data (submitted in the field "meta")
		// - meta data (submitted in the field "meta") and a file (submitted in
		// the field "file")
		// - only a file (submitted in the field "file")

		if (formDataMultiPart == null) {
			// TODO throw exception
		}
		Grafeo g = null;

		final FormDataBodyPart metaPart = formDataMultiPart.getField("meta");
		if (metaPart != null && metaPart.isSimple()) {
			g = new GrafeoImpl(metaPart.getValue(), null);
		} 
		if (g == null){
			g = new GrafeoImpl();
		}
		String uri = "";
		if (g.findTopBlank() == null){
			uri = "http://data.dm2e.eu/file/fromWS/" + new Date().getTime();
		}else{
			uri = g.findTopBlank().getUri();
		}
				
		final FormDataBodyPart filePart = formDataMultiPart.getField("file");
		if (filePart != null && !filePart.isSimple()) {
			// TODO think about where to store
			File f = new File(String.valueOf(new Date().getTime()));
			try {
				IOUtils.copy(filePart.getValueAs(InputStream.class),
						new FileOutputStream(f));
				//TODO add right predicates here
				g.addTriple(uri, "", filePart.getMediaType().toString());
				g.addTriple(uri, "", filePart.getContentDisposition().getFileName());
				g.addTriple(uri, "", f.getAbsolutePath());
				
			} catch (Exception e) {
				// TODO return something senseful
				throw new RuntimeException("Could not save file.");
			}
		}else{
			// we need to check if the path for the file is set
		}
		g.writeToEndpoint(
				"http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest/statements",
				uri);
		return getResponse(g);
	}

}
