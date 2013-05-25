package eu.dm2e.ws.services.xslt;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractRDFService;

@Path("/service/xslt")
public class XsltService extends AbstractRDFService {

	private Logger log = Logger.getLogger(getClass().getName());

	// private static final String NS_XSLT_SERVICE =
	// Config.getString("dm2e.service.xslt.namespace");
	// private static final String PROPERTY_HAS_WEB_SERVICE_CONFIG = NS.DM2E +
	// "hasWebServiceConfig";

//	private static final String URI_JOB_SERVICE = Config.getString("dm2e.service.job.base_uri");
//	private static final String URI_CONFIG_SERVICE = Config
//		.getString("dm2e.service.config.base_uri");

	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();

		ParameterPojo xsltInParam = ws.addInputParameter("xsltInParam");
		xsltInParam.setTitle("XSLT input");
		xsltInParam.setIsRequired(true);

		ParameterPojo xmlInParam = ws.addInputParameter("xmlInParam");
		xmlInParam.setTitle("XML input");
		xmlInParam.setIsRequired(true);

		ParameterPojo xmlOutParam = ws.addOutputParameter("xmlOutParam");
		xmlOutParam.setTitle("XML output");
		
		ParameterPojo fileServiceParam = ws.addInputParameter("fileServiceParam");
		fileServiceParam.setIsRequired(false);
		
		return ws;
	}

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	public Response putTransformation(String configURI) {

//		try {
//			validateServiceInput(configURI);
//		} catch (Exception e) {
//			return throwServiceError(e);
//		}
		/*	
		 * Resolve configURI to WebserviceConfigPojo
		 */
		WebserviceConfigPojo wsConf = resolveWebSerivceConfigPojo(configURI);

//		WebResource jobResource = Client.create().resource(URI_JOB_SERVICE);

		// create the job
		log.info("Creating the job");
		JobPojo job = new JobPojo();
		// TODO the job probably doesn't even need a webservice reference since it's in the conf already
		job.setWebService(wsConf.getWebservice());
		job.setWebserviceConfig(wsConf);
		job.publish();
//		JobPojo jobPojo = new JobPojo();
//		jobPojo.setWebService(getWebServicePojo());
//		GrafeoImpl g = new GrafeoImpl();
//		configPojo = 
		// TODO web service config pojo must be creatable from URI
//		jobPojo.setWebserviceConfig(configURI);
//		g.addObject(jobPojo);

//		log.info(g.getTurtle());
//		ClientResponse jobResponse = jobResource.accept("text/turtle").post(ClientResponse.class,
//				g.getNTriples());
//		log.info(jobResponse.toString());
//		log.info(jobResponse.getEntity(String.class).toString());
//		URI jobUri = jobResponse.getLocation();

		// post the job to the worker
		log.info("Posting the job to the worker queue");
		XsltExecutorService.INSTANCE.handleJob(job);
		return Response
				.ok()
				.entity(getResponseEntity(job.getGrafeo()))
				.location(job.getIdAsURI())
				.build();

		// return location of the job
//		jobPojo.setId(jobUri.toString());
//		GrafeoImpl g2 = new GrafeoImpl();
//		g2.addObject(jobPojo);
//		return Response.created(jobUri).entity(getResponseEntity(g2)).build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	public Response postTransformation(@Context UriInfo uriInfo, String rdfString) {

		WebserviceConfigPojo conf = new WebserviceConfigPojo().constructFromRdfString(rdfString);
		conf.publish();
//		return Response.ok(conf.getTurtle()).build();
		return this.putTransformation(conf.getId());
//		WebResource configResource = Client.create().resource(URI_CONFIG_SERVICE);
//		log.severe(URI_CONFIG_SERVICE);
//
//		// post the config
//		log.info("Persisting config.");
//		ClientResponse configResponse = configResource.accept("text/turtle").post(
//				ClientResponse.class, body);
//		URI configUri = configResponse.getLocation();
//
//		return putTransformation(configUri.toString());
	}
}
