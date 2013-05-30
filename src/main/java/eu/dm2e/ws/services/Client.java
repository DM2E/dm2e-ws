package eu.dm2e.ws.services;

import java.io.File;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.FilePojo;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public class Client {

    private com.sun.jersey.api.client.Client jerseyClient = new com.sun.jersey.api.client.Client();
    private Logger log = Logger.getLogger(getClass().getName());

    public String publishFile(File file, FilePojo meta) {
        WebResource fileResource = jerseyClient.resource(Config.getString("dm2e.service.file.base_uri"));
        FormDataMultiPart form = new FormDataMultiPart();

        // add file part
        MediaType fileType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
        FormDataBodyPart fileFDBP = new FormDataBodyPart("file", file, fileType);
        form.bodyPart(fileFDBP);

        // add metadata part
        FilePojo fileDesc = new FilePojo();
        // fileDesc.setGeneratorJob(jobPojo);
        fileDesc.setMediaType(fileType.toString());
        MediaType n3_type = MediaType.valueOf(DM2E_MediaType.TEXT_RDF_N3);
        FormDataBodyPart metaFDBP = new FormDataBodyPart("meta", fileDesc.getNTriples(), n3_type);
        form.bodyPart(metaFDBP);

        // build the response stub
        WebResource.Builder builder = fileResource
                .type(MediaType.MULTIPART_FORM_DATA)
                .accept(DM2E_MediaType.TEXT_TURTLE)
                .entity(form);

        // Post the file to the file service
        // jobPojo.info("Posting result to the file service.");
        ClientResponse resp = builder.post(ClientResponse.class);
        if (resp.getStatus() >= 400) {
            // jobPojo.fatal("File storage failed: " + resp.getEntity(String.class));
            // jobPojo.setFailed();
            throw new RuntimeException("File storage failed with status " + resp.getStatus() + ": " + resp.getEntity(String.class));
        }
        String fileLocation = resp.getLocation().toString();
        if (fileLocation == null) {
            // jobPojo.warn("File Service didn't respond with a Location header.");
            throw new RuntimeException("We didn't get a location header, ooumph! Status " + resp.getStatus() + ": " + resp.getEntity(String.class));
        }
        // jobPojo.info("File stored at: " + fileLocation);
        try {
            fileDesc.setFileRetrievalURI(fileLocation);
            fileDesc.publish();
        } catch(Exception e) {
            // jobPojo.fatal(e);
            throw new RuntimeException("Error publishing file metadata: " + e, e);
        }
        log.info("File stored, URI: " + fileLocation);
        return fileLocation;
    }
//    public HttpResponse postRDF(String postTo, Grafeo g, Map<String,String> headers) {
//    }

    /*******************
     * GETTERS/SETTERS
     ********************/
	public com.sun.jersey.api.client.Client getJerseyClient() { return jerseyClient; }
	public void setJerseyClient(com.sun.jersey.api.client.Client jerseyClient) { this.jerseyClient = jerseyClient; }
    
}
