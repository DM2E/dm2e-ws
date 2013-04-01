package eu.dm2e.ws.services.xslt;

import java.io.File;
import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.AbstractRDFService;
import eu.dm2e.ws.worker.XsltExecutorService;

@Path("/service/xslt")
public class XsltService extends AbstractRDFService {
	
	private Logger log = Logger.getLogger(getClass().getName());
	
//	private static final String NS_XSLT_SERVICE = Config.getString("dm2e.service.xslt.namespace");
	private static final String PROPERTY_HAS_WEB_SERVICE_CONFIG = NS.DM2E + "hasWebServiceConfig";
	
	// TODO shouldnot be hardwired
	private static final String URI_JOB_SERVICE = Config.getString("dm2e.service.job.base_uri");
	private static final String URI_CONFIG_SERVICE = Config.getString("dm2e.service.config.base_uri");
	
	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	public Response putTransformation(String configURI)  {
		
		WebResource jobResource = Client.create().resource(URI_JOB_SERVICE);	
		
		try{
			validateServiceInput(configURI);
		}
		catch(Exception e) {
			return throwServiceError(e);
		}
		
		// create the job
		log.info("Creating the job");
		GrafeoImpl g = new GrafeoImpl();
		Model m = g.getModel();
		Resource emptyResource = m.createResource();
		emptyResource.addProperty(m.createProperty(NS.RDF + "type"), m.createResource(NS.DM2E + "Job"));
		emptyResource.addLiteral(m.createProperty(NS.DM2E + "status"), "NOT_STARTED");
		emptyResource.addProperty(m.createProperty(NS.DM2E + "hasWebSerice"), m.createResource(uriInfo.getRequestUri().toASCIIString()));
		emptyResource.addProperty(m.createProperty(PROPERTY_HAS_WEB_SERVICE_CONFIG), m.createResource(configURI));
		ClientResponse jobResponse = jobResource
			.accept("text/turtle")
			.post(ClientResponse.class, g.getNTriples());
		URI jobUri = jobResponse.getLocation();
		g.findTopBlank().rename(jobUri.toString());
		
		
		// post the job to the worker
		log.info("Posting the job to the worker queue");
		XsltExecutorService.INSTANCE.handleJobUri(jobUri.toString());
		
		// return location of the job
		return Response.created(jobUri).entity(getResponseEntity(g)).build();
	}
	


	@POST
	@Consumes(MediaType.WILDCARD)
	public Response postTransformation(@Context UriInfo uriInfo, File body)  {
		
		WebResource configResource = Client.create().resource(URI_CONFIG_SERVICE);
		log.severe(URI_CONFIG_SERVICE);
		
		// post the config
		log.info("Persisting config.");
		ClientResponse configResponse = configResource
			.accept("text/turtle")
			.post(ClientResponse.class, body);
		URI configUri = configResponse.getLocation();
		
		return putTransformation(configUri.toString());
	}

}
	