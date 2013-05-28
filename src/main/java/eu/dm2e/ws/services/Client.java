package eu.dm2e.ws.services;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.grafeo.Grafeo;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 5/28/13
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class Client {

    com.sun.jersey.api.client.Client client = new com.sun.jersey.api.client.Client();
    Logger log = Logger.getLogger(getClass().getName());

    public String publishFile(File file, Grafeo meta) {
        WebResource webResource = client.resource(Config.getString("dm2e.service.file.base_uri"));

        InputStream stream = null;
        try {
            log.info("Publishing a file to " + webResource.getURI());
            log.info("File path: " + file.getPath());
            stream = new FileInputStream(file);
            FormDataMultiPart part = new FormDataMultiPart().field("file", stream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            ClientResponse response = webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, part);
            log.info("File stored at: " + response.getLocation().toString());
            return response.getLocation().toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }




    }
}
