package eu.dm2e.ws.services;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.gom.SerializablePojo;
import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.AbstractPersistentPojo;
import eu.dm2e.ws.api.FilePojo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Wrapper for a JAX-RS client with POJO posting/loading abilities and helpers for publishing files.
 */
public class Client {
	
	Logger log = LoggerFactory.getLogger(getClass().getName());

    private javax.ws.rs.client.Client jerseyClient = null;
//    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    public Response putPojoToService(SerializablePojo pojo, String wr) {
    	return this.putPojoToService(pojo, this.target(wr));
    }
    public Response putPojoToService(SerializablePojo pojo, WebTarget wr) {
    	if (null == pojo.getId()) {
    		throw new RuntimeException("Can't PUT a Pojo without id.");
    	}
//	    return wr
//	    		.type(DM2E_MediaType.TEXT_PLAIN)
//				.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
//				.entity(pojo.getId())
//				.put(Response.class);	
	    return wr
				.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
				.put(Entity.text(pojo.getId()));
    }
    
    public Response postPojoToService(SerializablePojo pojo, String wr) {
    	return this.postPojoToService(pojo, this.target(wr));
    }
    public Response postPojoToService(SerializablePojo pojo, WebTarget wr) {
//	    return wr.type(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
//			.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
//			.entity(pojo.getNTriples())
//			.post(Response.class);	
	    return wr
			.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
			.post(pojo.getNTriplesEntity());
    }
    
    public String publishPojo(AbstractPersistentPojo pojo, WebTarget serviceEndpoint) {
    	return publishPojo(pojo, serviceEndpoint, false);
    }
    
    public String publishPojo(AbstractPersistentPojo pojo, WebTarget serviceEndpoint, boolean reloadOnPublish) {
		Response resp;
		String method = "POST";
//		log.info("Size of NTRIPLES before post/put: " + pojo.getNTriples().length());
		log.debug(LogbackMarkers.DATA_DUMP, pojo.getTurtle());
		if ( ! pojo.hasId()) {
			log.info(method + "ing " + pojo + " to service " + serviceEndpoint.getUri());
			resp = this.postPojoToService(pojo, serviceEndpoint);
		} else {
			method = "PUT";
			log.info(method + "ing " + pojo + " to service " + serviceEndpoint.getUri());
			if (pojo.getId().startsWith(serviceEndpoint.getUri().toString())) {
				 resp = target(pojo.getId())
					.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
					.put(pojo.getNTriplesEntity());
//				 resp = target(pojo.getId())
//					.type(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
//					.accept(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
//					.entity(pojo.getNTriples())
//					.put(Response.class);
			} else {
				throw new NotImplementedException("Putting a config to a non-local web service currently is not implemented.");
			}
		}
		final String respStr = resp.readEntity(String.class);
		if (resp.getStatus() >= 400) {
			throw new RuntimeException(method + ": Failed to publish pojo <"
					+ pojo.getId()
					+ "> :"
					+ resp.getStatus() 
					+ ":" 
					+ respStr
					+ " (" + resp + ")"
					);
		}
//		log.info("Size of NTRIPLES after post/put: " + respStr.length());
		if (null == resp.getLocation()) {
			throw new RuntimeException(method +"ing " + pojo + " to " + serviceEndpoint.toString() +  " did not return a location. Body was " + respStr);
		}
		pojo.setId(resp.getLocation().toString());
		if (reloadOnPublish) {
			try {
				long timeStart = System.currentTimeMillis();
				pojo.loadFromURI(pojo.getId());
				long timeElapsed = System.currentTimeMillis() - timeStart;
				log.info(LogbackMarkers.TRACE_TIME, "Time spent: " + timeElapsed + "ms.");
			} catch (Exception e) {
				log.warn("Could not reload pojo." + e);
			}
		}
		return resp.getLocation().toString();
    }
    public String publishPojo(AbstractPersistentPojo pojo, String serviceURI) { return this.publishPojo(pojo, this.target(serviceURI), false); }
    public String publishPojoToJobService(AbstractPersistentPojo pojo) { return this.publishPojo(pojo, this.getJobWebTarget(), false); }
    public String publishPojoToConfigService(AbstractPersistentPojo pojo) { return this.publishPojo(pojo, this.getConfigWebTarget(), false); }
    
    public String publishFile(String file) { return this.publishFile(file, (String)null); }
    public String publishFile(String is, Grafeo metadata) { return this.publishFile(is, metadata.getNTriples()); }
    public String publishFile(String is, FilePojo metadata) { return this.publishFile(is, metadata.getNTriples()); }
    public String publishFile(InputStream is) throws IOException { return this.publishFile(IOUtils.toString(is)); }
    public String publishFile(InputStream is, String metadata) throws IOException { return this.publishFile(IOUtils.toString(is), metadata); }
    public String publishFile(InputStream is, Grafeo metadata) throws IOException { return this.publishFile(IOUtils.toString(is), metadata.getNTriples()); }
    public String publishFile(InputStream is, FilePojo metadata) throws IOException { return this.publishFile(IOUtils.toString(is), metadata.getNTriples()); }
    public String publishFile(File file) { FormDataMultiPart fdmp = createFileFormDataMultiPart(null, file); return this.publishFile(fdmp); }
    public String publishFile(File file, String metadata) { FormDataMultiPart fdmp = createFileFormDataMultiPart(metadata, file); return this.publishFile(fdmp); }
    public String publishFile(File file, Grafeo metadata) { FormDataMultiPart fdmp = createFileFormDataMultiPart(metadata.getNTriples(), file); return this.publishFile(fdmp); }
    public String publishFile(File file, FilePojo metadata) throws IOException { FileInputStream fis = new FileInputStream(file); return this.publishFile(IOUtils.toString(fis), metadata.getNTriples()); }
    public String publishFile(String file, String metadata) { FormDataMultiPart fdmp = createFileFormDataMultiPart(metadata, file); return this.publishFile(fdmp); }
    public String publishFile(FormDataMultiPart fdmp) {
//        Response resp = getFileWebTarget()
//                .type(MediaType.MULTIPART_FORM_DATA)
//                .accept(DM2E_MediaType.TEXT_TURTLE)
//                .entity(fdmp)
//                .post(Response.class);
    	Response resp = getFileWebTarget()
    			.request(DM2E_MediaType.TEXT_TURTLE)
    			.post(Entity.entity(fdmp, fdmp.getMediaType()));
        if (resp.getStatus() >= 400) {
            throw new RuntimeException("File storage failed with status " + resp.getStatus() + ": " + resp.readEntity(String.class));
        }
        if (null == resp.getLocation()) {
            throw new RuntimeException("File storage failed with status " + resp.getStatus() + ": " + resp.readEntity(String.class));
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
    
    public WebTarget getConfigWebTarget() { return this.target(Config.get(ConfigProp.CONFIG_BASEURI)); }
    public WebTarget getFileWebTarget() { return this.target(Config.get(ConfigProp.FILE_BASEURI)); }
    public WebTarget getJobWebTarget() { return this.target(Config.get(ConfigProp.JOB_BASEURI)); }
    public WebTarget getWorkflowWebTarget() { return this.target(Config.get(ConfigProp.WORKFLOW_BASEURI)); }
    
    public WebTarget target(String URI) {
    	return this.getJerseyClient().target(URI);
    }
    public WebTarget target(URI URI) {
    	return this.target(URI.toString());
    }
    
    public <U extends AbstractPersistentPojo>U loadPojoFromURI(Class<U> clazz, String id) {
    	U thePojo = null;
    	try {
			 thePojo = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			log.warn("Could not reload pojo "+id+":" + e);
			e.printStackTrace();
		}
    	if (null == thePojo) {
			log.warn("Could not reload pojo "+id+". Was null after instantiation.");
    		return null;
    	}
    	try {
			thePojo.loadFromURI(id);
		} catch (Exception e) {
			log.warn("Could not reload pojo "+id+":" + e);
			e.printStackTrace();
		}
		return thePojo;
    }

    /*******************
     * GETTERS/SETTERS
     ********************/
	public javax.ws.rs.client.Client getJerseyClient() {
    	if (null == this.jerseyClient) { 
    		javax.ws.rs.client.Client client = ClientBuilder.newClient();
    		client.register(MultiPartFeature.class);
    		this.setJerseyClient(client);
        }
		return this.jerseyClient;
	}
	public void setJerseyClient(javax.ws.rs.client.Client jerseyClient) { this.jerseyClient = jerseyClient; }

}
