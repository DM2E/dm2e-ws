package eu.dm2e.ws.services.job;

import java.net.URI;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
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
	
	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.setLabel("Job Service");
//		ws.setId("http://localhost:9998/job");
		return ws;
	}
	
	@Override
	public Response getJob(Grafeo g, GResource uriStr) {
//		JobPojo jobPojo = g.getObjectMapper().getObject(JobPojo.class, uriStr);
//		if (expectsJsonResponse()) {
//			return Response.ok().entity(jobPojo.toJson()).build();
//		} else if (expectsRdfResponse()) {
//			return Response.ok().entity(jobPojo.getGrafeo()).build();
//		} else {
//            return Response.notAcceptable(supportedVariants).build();
//        }
        try {
            return Response.ok().entity(getResponseEntity(g)).build();
        } catch (NullPointerException e) {
            return Response.notAcceptable(supportedVariants).build();
        }
	}

	@Override
	public Response postJob(Grafeo outputGrafeo, GResource jobRes) {
		
		log.debug("Putting job to endpoint.");
		
		outputGrafeo.putToEndpoint(NS.ENDPOINT_UPDATE, jobRes.getUri());
		
		return Response.created(URI.create(jobRes.getUri())).entity(getResponseEntity(outputGrafeo)).build();
	}
	
	@Override
	public Response putJob(Grafeo outputGrafeo, GResource jobRes) {
		
		log.debug("Putting job to endpoint.");
		
		outputGrafeo.putToEndpoint(Config.ENDPOINT_UPDATE, jobRes.getUri());
		
		return Response.created(URI.create(jobRes.getUri())).entity(getResponseEntity(outputGrafeo)).build();
	}
	
}
