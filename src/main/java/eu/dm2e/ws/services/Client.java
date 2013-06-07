package eu.dm2e.ws.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.Grafeo;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public class Client {
	
	Logger log = Logger.getLogger(getClass().getName());

    private com.sun.jersey.api.client.Client jerseyClient = null;
//    private Logger log = Logger.getLogger(getClass().getName());
    
    public ClientResponse putPojoToService(SerializablePojo pojo, String wr) {
    	return this.putPojoToService(pojo, this.resource(wr));
    }
    public ClientResponse putPojoToService(SerializablePojo pojo, WebResource wr) {
    	if (null == pojo.getId()) {
    		throw new RuntimeException("Can't PUT a Pojo without id.");
    	}
	    return wr
	    		.type(DM2E_MediaType.TEXT_PLAIN)
				.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
				.entity(pojo.getId())
				.put(ClientResponse.class);	
    }
    
    public ClientResponse postPojoToService(SerializablePojo pojo, String wr) {
    	return this.postPojoToService(pojo, this.resource(wr));
    }
    public ClientResponse postPojoToService(SerializablePojo pojo, WebResource wr) {
		    return wr.type(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
				.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
				.entity(pojo.getNTriples())
				.post(ClientResponse.class);	
    }
    
    public String publishPojo(SerializablePojo pojo, WebResource configWR) {
		ClientResponse resp;
		String method = "POST";
		if (null == pojo.getId()) {
			log.warning(method + "ing pojo to service " + configWR.getURI() + ": " + pojo.getTurtle());
			resp = this.postPojoToService(pojo, configWR);
		} else {
			method = "PUT";
			log.info(method + "ing pojo to service " + configWR.getURI());
			if (pojo.getId().startsWith(configWR.getURI().toString())) {
				 resp = resource(pojo.getId())
					.type(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
//					.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.entity(pojo.getNTriples())
					.put(ClientResponse.class);
			} else {
				throw new NotImplementedException("Putting a config to a non-local web service isn't implemented yet.");
			}
		}
		if (resp.getStatus() >= 400) {
			throw new RuntimeException(method + ": Failed to publish pojo <"
					+ pojo.getId()
					+ "> :"
					+ resp.getStatus() 
					+ ":" 
					+ resp.getEntity(String.class)
					);
		}
		if (null == resp.getLocation()) {
			throw new RuntimeException(configWR.toString() +  " did not return a location. Body was " + resp.getEntity(String.class));
		}
		pojo.setId(resp.getLocation().toString());
		return resp.getLocation().toString();
    }
    public String publishPojo(SerializablePojo pojo, String serviceURI) {
    	return this.publishPojo(pojo, this.resource(serviceURI));
    }
    public String publishPojoToJobService(SerializablePojo pojo) {
    	return this.publishPojo(pojo, this.getJobWebResource());
    }
    public String publishPojoToConfigService(SerializablePojo pojo) {
    	return this.publishPojo(pojo, this.getConfigWebResource());
    }
    
    public String publishFile(String file) {
    	return this.publishFile(file, (String)null);
    }
    public String publishFile(String is, Grafeo metadata) {
		return this.publishFile(is, metadata.getNTriples());
    }
    public String publishFile(String is, FilePojo metadata) {
		return this.publishFile(is, metadata.getNTriples());
    }
    
    public String publishFile(InputStream is) throws IOException {
		return this.publishFile(IOUtils.toString(is));
    }
    public String publishFile(InputStream is, String metadata) throws IOException {
		return this.publishFile(IOUtils.toString(is), metadata);
    }
    public String publishFile(InputStream is, Grafeo metadata) throws IOException {
		return this.publishFile(IOUtils.toString(is), metadata.getNTriples());
    }
    public String publishFile(InputStream is, FilePojo metadata) throws IOException {
		return this.publishFile(IOUtils.toString(is), metadata.getNTriples());
    }
    
    public String publishFile(File file) {
    	FormDataMultiPart fdmp = createFileFormDataMultiPart(null, file);
    	return this.publishFile(fdmp);
    }
    public String publishFile(File file, String metadata) {
    	FormDataMultiPart fdmp = createFileFormDataMultiPart(metadata, file);
    	return this.publishFile(fdmp);
    }
    public String publishFile(File file, Grafeo metadata) {
    	FormDataMultiPart fdmp = createFileFormDataMultiPart(metadata.getNTriples(), file);
    	return this.publishFile(fdmp);
    }
    public String publishFile(File file, FilePojo metadata) throws IOException {
    	FileInputStream fis = new FileInputStream(file);
		return this.publishFile(IOUtils.toString(fis), metadata.getNTriples());
    }
    
    public String publishFile(String file, String metadata) {
    	FormDataMultiPart fdmp = createFileFormDataMultiPart(metadata, file);
    	return this.publishFile(fdmp);
    }
    public String publishFile(FormDataMultiPart fdmp) {
        ClientResponse resp = getFileWebResource()
                .type(MediaType.MULTIPART_FORM_DATA)
                .accept(DM2E_MediaType.TEXT_TURTLE)
                .entity(fdmp)
                .post(ClientResponse.class);
        if (resp.getStatus() >= 400) {
            throw new RuntimeException("File storage failed with status " + resp.getStatus() + ": " + resp.getEntity(String.class));
        }
        if (null == resp.getLocation()) {
            throw new RuntimeException("File storage failed with status " + resp.getStatus() + ": " + resp.getEntity(String.class));
        }
        return resp.getLocation().toString();
    }
	public FormDataMultiPart createFileFormDataMultiPart(String metaStr, File content) {
		FormDataMultiPart fdmp = new FormDataMultiPart();
		if (null == metaStr) {
			metaStr = "";
		}
		FormDataBodyPart fdm_meta = new FormDataBodyPart(
				"meta", 
				metaStr,
				new MediaType("application", "rdf-triples"));
		fdmp.bodyPart(fdm_meta);
		if (null != content) {
			FormDataBodyPart fdm_file = new FormDataBodyPart(
				"file",
				content,
				MediaType.APPLICATION_OCTET_STREAM_TYPE);
			fdmp.bodyPart(fdm_file);
		}
		return fdmp;
	}

	public FormDataMultiPart createFileFormDataMultiPart(String metaStr, String content) {
		FormDataMultiPart fdmp = new FormDataMultiPart();
		if (null == metaStr) {
			metaStr = "";
		}
		FormDataBodyPart fdm_meta = new FormDataBodyPart(
				"meta", 
				metaStr,
				new MediaType("application", "rdf-triples"));
		fdmp.bodyPart(fdm_meta);
		if (null != content) {
			FormDataBodyPart fdm_file = new FormDataBodyPart(
				"file",
				content,
				MediaType.APPLICATION_OCTET_STREAM_TYPE);
			fdmp.bodyPart(fdm_file);
		}
		return fdmp;
	}
	public FormDataMultiPart createFileFormDataMultiPart(FilePojo meta, String content) {
		return createFileFormDataMultiPart(meta.getNTriples(), content);
	}
	public FormDataMultiPart createFileFormDataMultiPart(Grafeo meta, String content) {
		return createFileFormDataMultiPart(meta.getNTriples(), content);
	}
    
    public WebResource getConfigWebResource() {
    	return this.resource(Config.getString("dm2e.service.config.base_uri"));
    }
    public WebResource getFileWebResource() {
    	return this.resource(Config.getString("dm2e.service.file.base_uri"));
    }
    public WebResource getJobWebResource() {
    	return this.resource(Config.getString("dm2e.service.job.base_uri"));
    }
    
    public WebResource resource(String URI) {
    	return this.getJerseyClient().resource(URI);
    }
    public WebResource resource(URI URI) {
    	return this.resource(URI.toString());
    }

    /*******************
     * GETTERS/SETTERS
     ********************/
	public com.sun.jersey.api.client.Client getJerseyClient() {
    	if (null == this.jerseyClient) { 
    		 this.setJerseyClient(new com.sun.jersey.api.client.Client());
    	}
		return this.jerseyClient;
	}
	public void setJerseyClient(com.sun.jersey.api.client.Client jerseyClient) { this.jerseyClient = jerseyClient; }

}
