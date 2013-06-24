package eu.dm2e.ws.services.job;

import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebservicePojo;
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
	public Response getJob(Grafeo g, String uriStr) {
        try {
            return Response.ok().entity(getResponseEntity(g)).build();
        } catch (NullPointerException e) {
            return Response.notAcceptable(supportedVariants).build();
        }
	}

	@Override
	public Response postJob(Grafeo inputGrafeo, String uriStr) {
		
		log.fine("Skolemizing");
		inputGrafeo.skolemizeUUID(uriStr, NS.OMNOM.PROP_ASSIGNMENT, "assignment");
		inputGrafeo.skolemizeUUID(uriStr, NS.OMNOM.PROP_LOG_ENTRY, "log");
		
		log.warning("Instantiating Job POJO " + uriStr);
		JobPojo jobPojo = null;
		try {
			jobPojo = inputGrafeo.getObjectMapper().getObject(JobPojo.class, uriStr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info(inputGrafeo.getTurtle());
		jobPojo.setId(uriStr);
		log.info(jobPojo.getTurtle());
		
		Grafeo outputGrafeo = new GrafeoImpl();
		outputGrafeo.getObjectMapper().addObject(jobPojo);
		outputGrafeo.writeToEndpoint(NS.ENDPOINT_UPDATE, uriStr);
		return Response.created(URI.create(uriStr)).entity(getResponseEntity(jobPojo.getGrafeo())).build();
	}
	
	@Override
	public Response putJob(Grafeo inputGrafeo, String uriStr) {
		log.fine("Skolemnizing");
		GResource blank = inputGrafeo.findTopBlank(NS.OMNOM.CLASS_JOB);
		if (blank != null) {
			blank.rename(uriStr);
		}
		inputGrafeo.skolemizeUUID(uriStr, NS.OMNOM.PROP_LOG_ENTRY, "log");
		inputGrafeo.skolemizeUUID(uriStr, NS.OMNOM.PROP_ASSIGNMENT, "assignment");
		
		log.warning("Instantiating Job POJO " + uriStr);
		JobPojo jobPojo = inputGrafeo.getObjectMapper().getObject(JobPojo.class, uriStr);
		jobPojo.setId(uriStr);
		
		log.fine("Building output.");
		Grafeo outputGrafeo = new GrafeoImpl();
		outputGrafeo.getObjectMapper().addObject(jobPojo);
		SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete("?s ?p ?o.")
				.insert(outputGrafeo.getNTriples())
				.graph(uriStr)
				.endpoint(Config.ENDPOINT_UPDATE)
				.build();
		sparul.execute();
		return Response.created(URI.create(uriStr)).entity(getResponseEntity(jobPojo.getGrafeo())).build();
	}
	
}
