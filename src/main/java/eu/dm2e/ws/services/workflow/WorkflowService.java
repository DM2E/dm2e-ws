package eu.dm2e.ws.services.workflow;

import eu.dm2e.grafeo.GResource;
import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.grafeo.jena.SparqlUpdate;
import eu.dm2e.ws.*;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.services.AbstractRDFService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for the creation and execution of workflows
 */
@Path("/workflow")
public class WorkflowService extends AbstractRDFService {

	public static String PARAM_POLL_INTERVAL = "pollInterval";
	public static String PARAM_JOB_TIMEOUT = "jobTimeout";
	public static String PARAM_COMPLETE_LOG = "completeLog";

    @Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = new WebservicePojo();
		ws.setLabel("Workflow Storage Service");
		return ws;
	}


	/**
	 * GET /list
	 * @return
	 */
	@GET
	@Path("list")
	@Consumes({
		MediaType.APPLICATION_JSON
	})
	public Response getWorkflowList() {
		List<WorkflowPojo> wfList = new ArrayList<WorkflowPojo>();
        Grafeo g = new GrafeoImpl();
        g.readTriplesFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), null, NS.RDF.PROP_TYPE, g.resource(NS.OMNOM.CLASS_WORKFLOW));
        for (GResource wfUri : g.findByClass(NS.OMNOM.CLASS_WORKFLOW)) {
        	log.info("Workflow Resource: " + wfUri.getUri());
        	if (null == wfUri.getUri()) {
        		log.warn("There is a blank node workflow without an ID in the triplestore.");
        	}
        	// FIXME possibly very inefficient
        	g.load(wfUri.getUri());
        	WorkflowPojo wf = new WorkflowPojo();
        	wf.loadFromURI(wfUri.getUri());
        	wfList.add(wf);
        }

		return Response.ok(wfList).build();
	}



	/**
	 * GET /{id}
	 * @return
	 */
	@GET
	@Path("{id}")
	public Response getWorkflow() {
		URI wfUri = getRequestUriWithoutQuery();
		GrafeoImpl g = new GrafeoImpl();
		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), wfUri);
		if (g.isEmpty()) {
			return Response.status(404).build();
		}
		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);
		return Response.ok(wf).build();
	}



	/**
	 * PUT {workflowID} 	Accept: json
	 * @param wf
	 * @return
	 */
	@PUT
	@Path("{workflowID}")
	@Consumes({
		MediaType.APPLICATION_JSON
	})
	public Response putWorkflowAsJSON(WorkflowPojo wf) {
		return putGrafeo(wf.getGrafeo());
	}

	/**
	 * PUT {workflowID}		Accept: rdf
	 * TODO refactor
	 * @param rdfString
	 * @return
	 */
	@PUT
	@Path("{workflowID}")
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
	})
	public Response putRDF(String rdfString) {
		Grafeo g = null;
		try {
			g = new GrafeoImpl();
			g.readHeuristically(rdfString);
			assert(g != null);
		}
		catch (Exception e) {
			throwServiceError(e);
		}
		return this.putGrafeo(g);
	}

	public Response putGrafeo(Grafeo g) {
		final URI wfUri = getRequestUriWithoutQuery();
		final String wfUriStr = getRequestUriWithoutQuery().toString();

        // Link to the default webservice (WorkflowExecutionService)
        g.addTriple(g.resource(wfUri), NS.OMNOM.PROP_EXEC_WEBSERVICE, g.resource(pushPathFromBeginning(uriInfo, "exec")));

		// TODO FIXME What if the user changed the default parameters defined in post?

		log.info("Skolemizing parameters.");
		{
			g.skolemizeByLabel(wfUriStr, NS.OMNOM.PROP_INPUT_PARAM, "param");
			g.skolemizeByLabel(wfUriStr, NS.OMNOM.PROP_OUTPUT_PARAM, "param");
		}

		log.info("Skolemizing Positions.");
		{
			g.skolemizeSequential(wfUriStr, NS.OMNOM.PROP_WORKFLOW_POSITION, "position");
		}

		log.info("Skolemizing Connectors.");
		{
			g.skolemizeSequential(wfUriStr, NS.OMNOM.PROP_PARAMETER_CONNECTOR, "connector");
		}
//		log.info("Skolemnizing WebserviceConfigs");
//		{
//			for (GResource subj : g.findByClass(NS.OMNOM.CLASS_WORKFLOW_POSITION)) {
//				g.skolemizeUUID(subj.toString(), NS.OMNOM.PROP_WEBSERVICE_CONFIG, "webserviceconfig");
//			}
//		}

		log.info("Writing updated workflow to endpoint.");
		try {
			g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), wfUriStr);
		} catch (Exception e) {
			return throwServiceError(e);
		}
		log.info("DONE Writing updating workflow to endpoint: " + wfUriStr);

		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUriStr);
		return Response.ok(wf).location(wfUri).build();
	}

	/**
	 * POST /				Accept: json
	 * @param wf
	 * @return
	 */
	@POST
	@Consumes({
		MediaType.APPLICATION_JSON
	})
	public Response postWorkflowAsJSON(WorkflowPojo wf) {
//		WorkflowPojo wf = OmnomJsonSerializer.deserializeFromJSON(s, WorkflowPojo.class);
		log.error(wf.getTerseTurtle());
		return postGrafeo(wf.getGrafeo());
	}

    /**
     * Convenience method that accepts a configuration, publishes it
     * directly to the ConfigurationService and then calls the TransformationService
     * with the persistent URI.
     *
     * <del>Only to be used for development, not for production!</del>
     * This is necessary for workflows. [kb, Jul 15, 2013 12:03:08 AM]
     *
     * TODO test whether we can replace this with the MessageBodyWriter<Grafeo>
     *
     * @param rdfString
     * @return
     */
    @POST
    @Consumes({
            DM2E_MediaType.APPLICATION_RDF_TRIPLES,
            DM2E_MediaType.APPLICATION_RDF_XML,
            DM2E_MediaType.APPLICATION_X_TURTLE,
            DM2E_MediaType.TEXT_RDF_N3,
            DM2E_MediaType.TEXT_TURTLE
            // , MediaType.TEXT_HTML
            // , DM2E_MediaType.TEXT_PLAIN,
            // , MediaType.APPLICATION_JSON
    })
    public Response postRDF(String rdfString) {
        Grafeo g = null;
        try {
            g = new GrafeoImpl();
            g.readHeuristically(rdfString);
            assert(g != null);
        }
        catch (Exception e) {
            throwServiceError(e);
        }
        return this.postGrafeo(g);
    }

	public Response postGrafeo(Grafeo g) {
		GResource wfRes = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW);
		log.trace("Workflow before Posting: " + g.getTerseTurtle());
		log.info("Number of positions: " + g.listStatements(null, "omnom:hasPosition", null).size());

		if (null == wfRes) {
			return throwServiceError(NS.OMNOM.CLASS_WORKFLOW, ErrorMsg.NO_TOP_BLANK_NODE);
		}
		String uid =   createUniqueStr();
        String wfUri = getRequestUriWithoutQuery() + "/" + uid;
		wfRes.rename(wfUri);

        // Link to the default webservice (WorkflowExecutionService)

        g.addTriple(wfRes, NS.OMNOM.PROP_EXEC_WEBSERVICE, g.resource(pushPathFromBeginning(uriInfo,"exec") + "/" + uid));

		/*
		 * Add global workflow parameters
		 */
		log.info("Adding global workflow parameters");
		{
			{
				ParameterPojo pollTimeParam = new ParameterPojo();
				pollTimeParam.setLabel(PARAM_POLL_INTERVAL);
				pollTimeParam.setComment("Time to wait between polls for job status, in milliseconds. [Default: 2000ms]");
				pollTimeParam.setDefaultValue("" + 2 * 1000);
				GResource pollTimeParamRes = g.getObjectMapper().addObject(pollTimeParam);
				g.addTriple(wfRes, NS.OMNOM.PROP_INPUT_PARAM, pollTimeParamRes);
			}
			{
				ParameterPojo maxJobWaitParam = new ParameterPojo();
				maxJobWaitParam.setLabel(PARAM_JOB_TIMEOUT);
				maxJobWaitParam.setComment("Maximum time to wait for a job to finish, in seconds. [Default: 120s]");
				maxJobWaitParam.setDefaultValue("" + 120);
				GResource maxJobWaitParamRes = g.getObjectMapper().addObject(maxJobWaitParam);
				g.addTriple(wfRes, NS.OMNOM.PROP_INPUT_PARAM, maxJobWaitParamRes);
			}
			{
				ParameterPojo completeLogParam = new ParameterPojo();
				completeLogParam.setLabel(PARAM_COMPLETE_LOG);
				completeLogParam.setComment("The complete log file of the workflow job and its webservice jobs.");
				GResource completeLogParamRes = g.getObjectMapper().addObject(completeLogParam);
				g.addTriple(wfRes, NS.OMNOM.PROP_OUTPUT_PARAM, completeLogParamRes);
			}
		}

		log.info("Skolemnizing parameters.");
		{
			g.skolemizeByLabel(wfUri, NS.OMNOM.PROP_INPUT_PARAM, "param");
			g.skolemizeByLabel(wfUri, NS.OMNOM.PROP_OUTPUT_PARAM, "param");
		}

		log.info("Skolemizing Positions.");
		{
			g.skolemizeSequential(wfUri, NS.OMNOM.PROP_WORKFLOW_POSITION, "position");
		}

		log.info("Skolemnizing Connectors.");
		{
			g.skolemizeSequential(wfUri, NS.OMNOM.PROP_PARAMETER_CONNECTOR, "connector");
		}
//		log.info("Skolemnizing WebserviceConfigs");
//		{
//			for (GResource subj : g.findByClass(NS.OMNOM.CLASS_WORKFLOW_POSITION)) {
//				g.skolemizeUUID(subj.toString(), NS.OMNOM.PROP_WEBSERVICE_CONFIG, "webserviceconfig");
//			}
//		}

		log.info("Writing workflow to config.");
		try {
			g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), wfUri);
		} catch (Exception e) {
			return throwServiceError(e);
		}
		log.info("Done Writing workflow to config: " + wfUri);

		WorkflowPojo wf = g.getObjectMapper().getObject(WorkflowPojo.class, wfUri);

		try {
			wf.validate();
		} catch (Exception e) {
			return throwServiceError(e);
		}

		return Response.ok().entity(wf).location(URI.create(wfUri)).build();
	}
	@POST
	@Consumes({ DM2E_MediaType.APPLICATION_RDF_TRIPLES, DM2E_MediaType.APPLICATION_RDF_XML,
			DM2E_MediaType.APPLICATION_X_TURTLE, DM2E_MediaType.TEXT_PLAIN,
			DM2E_MediaType.TEXT_RDF_N3, DM2E_MediaType.TEXT_TURTLE })
	@Path("{workflowId}/position")
	public Response postWorkflowPosition(@PathParam("workflowId") String workflowId, String rdfString) {
		Grafeo g = new GrafeoImpl(rdfString, null);
		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_POSITION);
		if (null == blank) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		URI newUri = appendPath(createUniqueStr());
		URI workflowUri = popPath("position");
		blank.rename(newUri);
		// TODO rename blank sub-resources
		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), workflowUri);
		return Response.created(newUri).build();
	}
	@PUT
	@Path("{workflowId}/position/{posId}")
	public Response putWorkflowPosition(@PathParam("workflowId") String workflowId, @PathParam("posId") String posId, String rdfString) {
		URI posUri = getRequestUriWithoutQuery();
		URI workflowUri = popPath(popPath());
		Grafeo g = new GrafeoImpl(rdfString, null);
		SparqlUpdate sparul = new SparqlUpdate.Builder()
			.delete(posUri + "?s ?o")
			.insert(g)
			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
			.graph(workflowUri)
			.build();
		sparul.execute();
		return getResponse(g);
	}
	@GET
	@Path("{workflowId}/position/{posId}")
	@Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
	})
	public Response getWorkflowPosition(@PathParam("workflowId") String workflowId, @PathParam("posId") String posId) {
		URI workflowUri = popPath(popPath());
		return Response.seeOther(workflowUri).build();
	}
	@POST
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE })
	@Path("{workflowId}/connector")
	public Response postWorkflowConnector(@PathParam("workflowId") String workflowId, String rdfString) { Grafeo g = new GrafeoImpl(rdfString, null);
		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_PARAMETER_CONNECTOR);
		if (null == blank) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		URI newUri = appendPath(createUniqueStr());
		URI workflowUri = popPath("connector");
		blank.rename(newUri);
		// TODO rename blank sub-resources
		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), workflowUri);
		return Response.created(newUri).build();
	}
	@PUT
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE })
	@Path("{workflowId}/connector/{conId}")
	public Response putWorkflowConnector(@PathParam("workflowId") String workflowId, @PathParam("conId") String conId, String rdfString) {
		URI conUri = getRequestUriWithoutQuery();
		URI workflowUri = popPath(popPath());
		Grafeo g = new GrafeoImpl(rdfString, null);
		SparqlUpdate sparul = new SparqlUpdate.Builder()
			.delete(conUri + "?s ?o")
			.insert(g.toString())
			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
			.graph(workflowUri).build();
		sparul.execute();
		return getResponse(g);
	}
	@GET
	@Path("{workflowId}/connector/{conId}")
	@Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
	})
	public Response getWorkflowConnector( @PathParam("workflowId") String workflowId, @PathParam("conId") String conId) {
		URI workflowUri = popPath(popPath());
		return Response.seeOther(workflowUri).build();
	}
	@POST
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE })
	@Path("{workflowId}/param")
	public Response postWorkflowParameter(@PathParam("workflowId") String workflowId, String rdfString) {
		Grafeo g = new GrafeoImpl(rdfString, null);
		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_PARAMETER);
		if (null == blank) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		URI newUri = appendPath(createUniqueStr());
		URI workflowUri = popPath();
		blank.rename(newUri);
		// TODO rename blank sub-resources
		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), workflowUri);
		return Response.created(newUri).build();
	}
	@PUT
	@Path("{workflowId}/param/{paramId}")
	public Response putWorkflowParamaeter(@PathParam("workflowId") String workflowId, @PathParam("paramId") String paramId, String rdfString) {
		URI conUri = getRequestUriWithoutQuery();
		URI workflowUri = popPath(popPath());
		Grafeo g = new GrafeoImpl(rdfString, null);
		SparqlUpdate sparul = new SparqlUpdate.Builder()
			.delete(conUri + "?s ?o")
			.insert(g.toString())
			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
			.graph(workflowUri).build();
		sparul.execute();
		return getResponse(g);
	}
	@GET
	@Path("{workflowId}/param/{paramId}")
	@Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
	})
	public Response getWorkflowParameter( @PathParam("workflowId") String workflowId, @PathParam("paramId") String paramId) {
		URI workflowUri = popPath(popPath());
		return Response.seeOther(workflowUri).build();
	}


}
