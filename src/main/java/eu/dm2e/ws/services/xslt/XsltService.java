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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.AbstractRDFService;
import eu.dm2e.ws.worker.XsltExecutorService;

@Path("/service/xslt")
public class XsltService extends AbstractRDFService {
	
	private Logger log = Logger.getLogger(getClass().getName());
	
//	private static final String NS_XSLT_SERVICE = Config.getString("dm2e.service.xslt.namespace");
//	private static final String PROPERTY_HAS_WEB_SERVICE_CONFIG = NS.DM2E + "hasWebServiceConfig";
	
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
		JobPojo jobPojo = new JobPojo();
		GrafeoImpl g = new GrafeoImpl();
		jobPojo.setWebService(uriInfo.getRequestUri().toASCIIString());
		jobPojo.setWebServiceConfig(configURI);
		g.addObject(jobPojo);
		
		log.info(g.getNTriples());
		ClientResponse jobResponse = jobResource
			.accept("text/turtle")
			.post(ClientResponse.class, g.getNTriples());
		log.info(jobResponse.toString());
		log.info(jobResponse.getEntity(String.class).toString());
		URI jobUri = jobResponse.getLocation();
		
		// post the job to the worker
		log.info("Posting the job to the worker queue");
		XsltExecutorService.INSTANCE.handleJobUri(jobUri.toString());
		
		// return location of the job
		jobPojo.setId(jobUri.toString());
		g = new GrafeoImpl();
		g.addObject(jobPojo);
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
	