package eu.dm2e.ws.services.job;

import java.io.File;
import java.net.URI;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import eu.dm2e.ws.Config;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.AbstractJobPojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlUpdate;

//import java.util.ArrayList;
// TODO @GET /{id}/result with JSON

@Path("/job")
public class JobService extends AbstractJobService {
	
	private static String ENDPOINT_QUERY = Config.getString("dm2e.ws.sparql_endpoint");
	static String ENDPOINT_UPDATE = Config.getString("dm2e.ws.sparql_endpoint_statements");

	Logger log = Logger.getLogger(getClass().getName());
	
	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.setLabel("Job Service");
//		ws.setId("http://localhost:9998/job");
		return ws;
	}

    @Override
	@GET
    @Path("/{resourceID}")
    @Consumes(MediaType.WILDCARD)
    public Response getJob(@PathParam("resourceID") String resourceID) {
        log.info("Access to job: " + resourceID);
        String uriStr = uriInfo.getRequestUri().toString();
        Grafeo g = new GrafeoImpl();
        log.info("Reading job from endpoint " + ENDPOINT_QUERY);
        try {
            g.readFromEndpoint(ENDPOINT_QUERY, uriStr);
        } catch (Exception e1) {
            // if we couldn't read the job, try again once in a second
            try { Thread.sleep(1000); } catch (InterruptedException e) { }
            try { g.readFromEndpoint(ENDPOINT_QUERY, uriStr);
            } catch (Exception e) {
                return throwServiceError(e);
            }
        }
        AbstractJobPojo job = g.getObjectMapper().getObject(JobPojo.class, uriStr);
        String jobStatus = job.getStatus();
        log.info("Job status: " + jobStatus);
        try {
            return Response.ok().entity(getResponseEntity(job.getGrafeo())).build();
        } catch (NullPointerException e) {
            return Response.notAcceptable(supportedVariants).build();
        }
    }

	@Override
	@POST
	@Consumes(MediaType.WILDCARD)
	public Response postJob(File bodyAsFile) {
		log.info("Config posted.");
		// TODO use Exception to return proper HTTP response if input can not be
		// parsed as RDF
		Grafeo inputGrafeo;
		try {
			inputGrafeo = new GrafeoImpl(bodyAsFile);
		} catch (Exception e) {
			return throwServiceError(ErrorMsg.BAD_RDF);
		}
		GResource blank = inputGrafeo.findTopBlank("omnom:Job");
		if (blank == null) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		String uri = getWebServicePojo().getId() + "/" + UUID.randomUUID().toString();;
		
		log.fine("Skolemnizing");
		blank.rename(uri);
		inputGrafeo.skolemnizeUUID(uri, JobPojo.PROP_OUTPUT_ASSIGNMENT, "assignment");
		inputGrafeo.skolemnizeUUID(uri, JobPojo.PROP_LOG_ENTRY, "log");
		
		log.warning("Instantiating Job POJO " + uri);
		JobPojo jobPojo = inputGrafeo.getObjectMapper().getObject(JobPojo.class, uri);
		jobPojo.setId(uri);
		
		Grafeo outputGrafeo = new GrafeoImpl();
		outputGrafeo.getObjectMapper().addObject(jobPojo);
		outputGrafeo.writeToEndpoint(NS.ENDPOINT_STATEMENTS, uri);
		return Response.created(URI.create(uri)).entity(getResponseEntity(jobPojo.getGrafeo())).build();
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
		outputGrafeo.addTriple(jobUri, JobPojo.PROP_OUTPUT_ASSIGNMENT, assUri);
		SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete("?s ?p ?o.")
				.insert(outputGrafeo.getNTriples())
				.graph(jobUri)
				.endpoint(ENDPOINT_UPDATE)
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
	
	@Override
	@PUT
	@Path("/{resourceID}")
	@Consumes(MediaType.WILDCARD)
	public Response putJob(@PathParam("resourceID") String resourceID, File bodyAsFile) {
		log.info("Access to job: " + resourceID);
		String uriStr = uriInfo.getRequestUri().toString();
		Grafeo inputGrafeo;
		try {
			inputGrafeo = new GrafeoImpl(bodyAsFile);
		} catch (Exception e) {
			return throwServiceError(ErrorMsg.BAD_RDF);
		}
		
		log.fine("Skolemnizing");
		GResource blank = inputGrafeo.findTopBlank("omnom:Job");
		if (blank != null) {
			blank.rename(uriStr);
		}
		inputGrafeo.skolemnizeUUID(uriStr, JobPojo.PROP_LOG_ENTRY, "log");
		inputGrafeo.skolemnizeUUID(uriStr, JobPojo.PROP_OUTPUT_ASSIGNMENT, "assignment");
		
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
				.endpoint(ENDPOINT_UPDATE)
				.build();
		sparul.execute();
		return Response.created(URI.create(uriStr)).entity(getResponseEntity(jobPojo.getGrafeo())).build();
	}
}
