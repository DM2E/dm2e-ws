package eu.dm2e.ws.services.job;

import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.AbstractJobPojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowJobPojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlUpdate;
import eu.dm2e.ws.services.AbstractJobService;

// TODO @GET /{id}/result with JSON

/**
 * NOTE: Don't use any Jersey annotations in the overridden methods. JSR-311 specifies
 * that a single annotation hides all inerited annotations.
 * 
 * @author kb
 *
 */
@Path("/job")
public class JobService extends AbstractJobService {
	
	Logger log = Logger.getLogger(getClass().getName());
	
	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.setLabel("Job Service");
//		ws.setId("http://localhost:9998/job");
		return ws;
	}
	
	@Override
	public Response getJob(Grafeo g, GResource uriStr) {
        try {
            return Response.ok().entity(getResponseEntity(g)).build();
        } catch (NullPointerException e) {
            return Response.notAcceptable(supportedVariants).build();
        }
	}

	@Override
	public Response postJob(Grafeo inputGrafeo, GResource jobRes) {
		
		log.fine("Skolemizing");
		inputGrafeo.skolemizeUUID(jobRes.getUri(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
		inputGrafeo.skolemizeUUID(jobRes.getUri(), NS.OMNOM.PROP_LOG_ENTRY, "log");
		
		log.warning("Instantiating " + jobRes);
		JobPojo jobPojo = null;
		Class<? extends AbstractJobPojo> clazz = jobRes.isa(NS.OMNOM.CLASS_JOB)
				? JobPojo.class
				: WorkflowJobPojo.class;
		try {
			jobPojo = inputGrafeo.getObjectMapper().getObject(clazz, jobRes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info(inputGrafeo.getTurtle());
		jobPojo.setId(jobRes.getUri());
		log.info(jobPojo.getTurtle());
		
		Grafeo outputGrafeo = new GrafeoImpl();
		outputGrafeo.getObjectMapper().addObject(jobPojo);
		outputGrafeo.writeToEndpoint(NS.ENDPOINT_UPDATE, jobRes.getUri());
		return Response.created(URI.create(jobRes.getUri())).entity(getResponseEntity(jobPojo.getGrafeo())).build();
	}
	
	@Override
	public Response putJob(Grafeo inputGrafeo, GResource jobRes) {
		log.fine("Skolemnizing");
		GResource blank = inputGrafeo.findTopBlank(NS.OMNOM.CLASS_JOB);
		if (blank != null) {
			blank.rename(jobRes);
		}
		inputGrafeo.skolemizeUUID(jobRes.getUri(), NS.OMNOM.PROP_LOG_ENTRY, "log");
		inputGrafeo.skolemizeUUID(jobRes.getUri(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
		
		log.warning("Instantiating " + jobRes);
		Class<? extends AbstractJobPojo> clazz = jobRes.isa(NS.OMNOM.CLASS_JOB)
				? JobPojo.class
				: WorkflowJobPojo.class;
		JobPojo jobPojo = inputGrafeo.getObjectMapper().getObject(clazz, jobRes);
		jobPojo.setId(jobRes.getUri());
		
		log.fine("Building output.");
		Grafeo outputGrafeo = new GrafeoImpl();
		outputGrafeo.getObjectMapper().addObject(jobPojo);
		SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete("?s ?p ?o.")
				.insert(outputGrafeo.getNTriples())
				.graph(jobRes.getUri())
				.endpoint(Config.ENDPOINT_UPDATE)
				.build();
		sparul.execute();
		return Response.created(URI.create(jobRes.getUri())).entity(getResponseEntity(jobPojo.getGrafeo())).build();
	}
	
}
