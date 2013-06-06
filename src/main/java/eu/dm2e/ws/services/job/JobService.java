package eu.dm2e.ws.services.job;

import java.io.File;
import java.net.URI;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
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
		
		log.fine("Skolemnizing");
		inputGrafeo.skolemnizeUUID(uriStr, NS.OMNOM.PROP_OUTPUT_ASSIGNMENT, "assignment");
		inputGrafeo.skolemnizeUUID(uriStr, NS.OMNOM.PROP_LOG_ENTRY, "log");
		
		log.warning("Instantiating Job POJO " + uriStr);
		JobPojo jobPojo = inputGrafeo.getObjectMapper().getObject(JobPojo.class, uriStr);
		jobPojo.setId(uriStr);
		
		Grafeo outputGrafeo = new GrafeoImpl();
		outputGrafeo.getObjectMapper().addObject(jobPojo);
		outputGrafeo.writeToEndpoint(NS.ENDPOINT_STATEMENTS, uriStr);
		return Response.created(URI.create(uriStr)).entity(getResponseEntity(jobPojo.getGrafeo())).build();
	}
	
	@Override
	public Response putJob(Grafeo inputGrafeo, String uriStr) {
		log.fine("Skolemnizing");
		GResource blank = inputGrafeo.findTopBlank("omnom:Job");
		if (blank != null) {
			blank.rename(uriStr);
		}
		inputGrafeo.skolemnizeUUID(uriStr, NS.OMNOM.PROP_LOG_ENTRY, "log");
		inputGrafeo.skolemnizeUUID(uriStr, NS.OMNOM.PROP_OUTPUT_ASSIGNMENT, "assignment");
		
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
	
	@POST
	@Consumes(MediaType.WILDCARD)
	@Path("/{id}/assignment")
	public Response postAssignment(File bodyAsFile) {
		Grafeo inputGrafeo;
		try {
			inputGrafeo = new GrafeoImpl(bodyAsFile);
		} catch (Exception e) {
			return throwServiceError(ErrorMsg.BAD_RDF);
		}
		GResource blank = inputGrafeo.findTopBlank("omnom:ParameterAssignment");
		if (blank == null) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		String jobUri = getRequestUriWithoutQuery().toString().replaceAll("/assignment$", "");
		String assUri = getRequestUriWithoutQuery() + "/" + UUID.randomUUID().toString();;
		ParameterAssignmentPojo ass = inputGrafeo.getObjectMapper().getObject(ParameterAssignmentPojo.class, blank);
		ass.setId(assUri);
		
		Grafeo outputGrafeo = new GrafeoImpl();
		outputGrafeo.getObjectMapper().addObject(ass);
		outputGrafeo.addTriple(jobUri, NS.OMNOM.PROP_OUTPUT_ASSIGNMENT, assUri);
		SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete("?s ?p ?o.")
				.insert(outputGrafeo.getNTriples())
				.graph(jobUri)
				.endpoint(Config.ENDPOINT_UPDATE)
				.build();
		sparul.execute();
		return Response.created(URI.create(assUri)).entity(getResponseEntity(ass.getGrafeo())).build();
	}
    @GET
    @Path("{id}/assignment/{assId}")
    public Response getAssignment( @PathParam("id") String id, @PathParam("assId") String assId) {
        log.info("Output Assignment " + assId + " of job requested: " + uriInfo.getRequestUri());
        String uri = getRequestUriWithoutQuery().toString().replaceAll("/assignment/[^/]*", "");

        return Response.status(303).location(URI.create(uri)).build();
    }
}
