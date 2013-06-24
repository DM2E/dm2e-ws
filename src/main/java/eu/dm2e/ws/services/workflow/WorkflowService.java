package eu.dm2e.ws.services.workflow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowConfigPojo;
import eu.dm2e.ws.api.WorkflowJobPojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlUpdate;
import eu.dm2e.ws.services.AbstractAsynchronousRDFService;
import eu.dm2e.ws.services.WorkerExecutorSingleton;

@Path("/workflow")
public class WorkflowService extends AbstractAsynchronousRDFService {

	/**
	 * The WorkflowJob object for the worker part of the service (to be used in
	 * the run() method)
	 */
	private WorkflowJobPojo wfPojo;

	public WorkflowJobPojo getWorkflowJobPojo() {
		return this.wfPojo;
	};

	public void setWorkflowJobPojo(WorkflowJobPojo wfPojo) {
		this.wfPojo = wfPojo;
	};

	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = new WebservicePojo();
		ws.setLabel("Workflow Service");
		return ws;
	}

	@Override
	public Response putConfigToService(String workflowBlueprintURI) {
		/*
		 * Resolve configURI to WebserviceConfigPojo
		 */
		WorkflowConfigPojo wfConf = new WorkflowConfigPojo();
		try {
			wfConf.loadFromURI(workflowBlueprintURI, 1);
		} catch (Exception e) {
			return throwServiceError(e);
		}

		/*
		 * Validate the configuration
		 */
		try {
			wfConf.validate();
		} catch (Exception e) {
			return throwServiceError(e);
		}

		/*
		 * Build WorkflowJobPojo
		 */
		WorkflowJobPojo job = new WorkflowJobPojo();
		job.setWorkflowConfig(wfConf);
		job.addLogEntry("WorkflowJobPojo constructed by WorkflowService", "TRACE");
		job.publishToService();

		/*
		 * Let the asynchronous worker handle the job
		 */
		try {
			WorkflowService instance = getClass().newInstance();
			Method method = getClass().getMethod("setWorkflowJobPojo", WorkflowJobPojo.class);
			method.invoke(instance, job);
			log.info("Job is before instantiation :" + job);
			WorkerExecutorSingleton.INSTANCE.handleJob(instance);
		} catch (NoSuchMethodException e) {
			return throwServiceError(e);
		} catch (InvocationTargetException e) {
			return throwServiceError(e);
		} catch (InstantiationException e) {
			return throwServiceError(e);
		} catch (IllegalAccessException e) {
			return throwServiceError(e);
		} catch (Exception e) {
			return throwServiceError(e);

		}

		/*
		 * Return JobPojo
		 */
		return Response.status(202).location(job.getIdAsURI()).entity(
				getResponseEntity(job.getGrafeo())).build();
	}

	@GET
	@Path("{id}")
	public Response getWorkflow() {

		URI wfUri = getRequestUriWithoutQuery();
		GrafeoImpl g = new GrafeoImpl();
		g.readFromEndpoint(NS.ENDPOINT_SELECT, wfUri);
		return getResponse(g);
	}

	@Override
	public Response postGrafeo(Grafeo g) {
		GResource wfRes = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW);
		log.fine("Workflow before Posting: " + g.getTerseTurtle());
		log.info("Number of positions: " + g.listStatements(null, "omnom:hasPosition", null).size());

		if (null == wfRes) {
			return throwServiceError(NS.OMNOM.CLASS_WORKFLOW, ErrorMsg.NO_TOP_BLANK_NODE);
		}
		String wfUri = getRequestUriWithoutQuery() + "/" + createUniqueStr();
		wfRes.rename(wfUri);

		log.info("Skolemnizing parameters.");
		{
			for (GResource paramRes : g.findByClass(NS.OMNOM.CLASS_PARAMETER)) {
				String paramLabel = paramRes.get(NS.RDFS.PROP_LABEL).getTypedValue(String.class);
				log.info("Adding parameter: " + paramLabel);
				if (paramRes.isAnon()) {
					paramRes.rename(wfUri + "/param/" + paramLabel);
				}
			}
		}

//		log.info("Skolemnizing Position Proxies.");
//		{
//			int positionItemIndex = 0;
//			for (GResource positionItemRes : g.findByClass(NS.CO.CLASS_ITEM)) {
//				positionItemIndex++;
//				positionItemRes.rename(wfUri + "/position-item/" + positionItemIndex);
//			}
//		}
		log.info("Skolemizing Positions.");
		{
			int positionIndex = 0;
			for (GResource positionRes : g.findByClass(NS.OMNOM.CLASS_WORKFLOW_POSITION)) {
				positionIndex++;
				log.info("Adding position # " + positionIndex);
				if (positionRes.isAnon()) {
					positionRes.rename(wfUri + "/position/" + positionIndex);
				}
			}
		}
//		log.info("Publishing blank web service configs.");
//		{
//			for (GResource wsConfRes : g.findByClass(NS.OMNOM.CLASS_WEBSERVICE_CONFIG)) {
//				WebserviceConfigPojo wsConf = g.getObjectMapper().getObject(
//						WebserviceConfigPojo.class, wsConfRes);
//				if (!wsConf.hasId()) {
//					String confLoc = wsConf.publishToService(client.getConfigWebResource());
//					wsConfRes.rename(confLoc);
//				}
//				// if (wsConf.isAnon()) {
//				// w
//				// }
//			}
//		}

		log.info("Skolemnizing Connectors.");
		{
			int connectorIndex = 0;
			for (GResource positionRes : g.findByClass(NS.OMNOM.CLASS_PARAMETER_CONNECTOR)) {
				connectorIndex++;
				log.info("Adding position # " + connectorIndex);
				if (positionRes.isAnon()) {
					positionRes.rename(wfUri + "/connector/" + connectorIndex);
				}
			}
		}

		log.info("Writing workflow to config.");
		g.emptyGraph(NS.ENDPOINT_UPDATE, wfUri);
		g.writeToEndpoint(NS.ENDPOINT_UPDATE, wfUri);
		log.info("Done Writing workflow to config: " + wfUri);

//		Assert.assertEquals(1, g.listStatements(null, "omnom:hasPosition", null).size());
		return Response.ok().entity(getResponseEntity(g)).location(URI.create(wfUri)).build();
	}

	@POST
	@Consumes({ DM2E_MediaType.APPLICATION_RDF_TRIPLES, DM2E_MediaType.APPLICATION_RDF_XML,
			DM2E_MediaType.APPLICATION_X_TURTLE, DM2E_MediaType.TEXT_PLAIN,
			DM2E_MediaType.TEXT_RDF_N3, DM2E_MediaType.TEXT_TURTLE })
	@Path("{workflowId}/position")
	public Response postWorkflowPosition(@PathParam("workflowId") String workflowId,
			String rdfString) {
		Grafeo g = new GrafeoImpl(rdfString, null);
		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_POSITION);
		if (null == blank) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		URI newUri = appendPath(createUniqueStr());
		URI workflowUri = popPath("position");
		blank.rename(newUri);
		// TODO rename blank sub-resources
		g.writeToEndpoint(Config.ENDPOINT_UPDATE, workflowUri);
		return Response.created(newUri).build();
	}

	@PUT
	@Path("{workflowId}/position/{posId}")
	public Response putWorkflowPosition(@PathParam("workflowId") String workflowId,
			@PathParam("posId") String posId,
			String rdfString) {
		URI posUri = getRequestUriWithoutQuery();
		URI workflowUri = popPath(popPath());
		Grafeo g = new GrafeoImpl(rdfString, null);
		// WorkflowPositionPojo pos =
		// g.getObjectMapper().getObject(WorkflowPositionPojo.class, posUri);
		// pos.getGrafeo().writeToEndpoint(Config.ENDPOINT_UPDATE, posId);
		SparqlUpdate sparul = new SparqlUpdate.Builder().delete(posUri + "?s ?o").insert(
				g.toString()).endpoint(Config.ENDPOINT_UPDATE).graph(workflowUri).build();
		sparul.execute();
		return getResponse(g);
	}

	@POST
	@Consumes({ DM2E_MediaType.APPLICATION_RDF_TRIPLES, DM2E_MediaType.APPLICATION_RDF_XML,
			DM2E_MediaType.APPLICATION_X_TURTLE, DM2E_MediaType.TEXT_PLAIN,
			DM2E_MediaType.TEXT_RDF_N3, DM2E_MediaType.TEXT_TURTLE })
	@Path("{workflowId}/connector")
	public Response postWorkflowConnector(@PathParam("workflowId") String workflowId,
			String rdfString) {
		Grafeo g = new GrafeoImpl(rdfString, null);
		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_PARAMETER_CONNECTOR);
		if (null == blank) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		URI newUri = appendPath(createUniqueStr());
		URI workflowUri = popPath("connector");
		blank.rename(newUri);
		// TODO rename blank sub-resources
		g.writeToEndpoint(Config.ENDPOINT_UPDATE, workflowUri);
		return Response.created(newUri).build();
	}

	@PUT
	@Path("{workflowId}/connection/{conId}")
	public Response putWorkflowConnector(@PathParam("workflowId") String workflowId,
			@PathParam("conId") String conId,
			String rdfString) {
		URI conUri = getRequestUriWithoutQuery();
		URI workflowUri = popPath(popPath());
		Grafeo g = new GrafeoImpl(rdfString, null);
		SparqlUpdate sparul = new SparqlUpdate.Builder().delete(conUri + "?s ?o").insert(
				g.toString()).endpoint(Config.ENDPOINT_UPDATE).graph(workflowUri).build();
		sparul.execute();
		return getResponse(g);
	}

	@Override
	public void run() {
		WorkflowJobPojo wf = this.getWorkflowJobPojo();
		try {
			wf.setStarted();
			// for (WorkflowPositionPojo pos : jobPojo.conf.)

			// TODO

			wf.setFailed();
		} catch (Exception e) {
			throw e;
		}
	}

}