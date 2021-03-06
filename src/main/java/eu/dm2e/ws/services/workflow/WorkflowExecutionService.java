package eu.dm2e.ws.services.workflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import sun.nio.cs.ext.EUC_CN;
import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.grafeo.util.LogbackMarkers;
import eu.dm2e.utils.Misc;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.ParameterConnectorPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.ValidationReport;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
import eu.dm2e.ws.services.AbstractAsynchronousRDFService;
import eu.dm2e.ws.services.WorkerExecutorSingleton;

/**
 * Service for the execution of workflows
 */
@Path("/exec/workflow")
public class WorkflowExecutionService extends AbstractAsynchronousRDFService {
	
	private class TimingReport implements Comparable<TimingReport>{
		String phase;
		String status;
		long startTime;
		long endTime;
		long elapsed;
		int iteration;
		public String getPhase() { return phase; }
		public String getStatus() { return status; }
		public void setStatus(String status) { this.status = status; }
		public long getStartTime() { return startTime; }
		public void setStartTime() { this.setStartTime(System.currentTimeMillis()); }
		public void setStartTime(long startTime) { this.startTime = startTime; }
		public long getEndTime() { return endTime; }
		public void end() { this.setEndTime(System.currentTimeMillis()); }
		public void setEndTime(long endTime) { 
			this.endTime = endTime; 
			this.setElapsed(this.getEndTime() - this.getStartTime());
		}
		public long getElapsed() { return elapsed; }
		public void setElapsed(long elapsed) { this.elapsed = elapsed; }
		public int getIteration() { return iteration; }
		public void setIteration(int iteration) { this.iteration = iteration; }
		@Override
		public int compareTo(TimingReport o) { return new Long(this.startTime).compareTo(new Long(o.getStartTime())); }
		@Override
		public boolean equals(Object obj) {
			if (obj.getClass().isInstance(TimingReport.class)) {
				TimingReport job = (TimingReport) obj;
				return this.phase.equals(job.getPhase());
			}
			return false;
		}
		public TimingReport(JobPojo job) { this.phase = job.getId();  this.setStartTime();}
		public TimingReport(String id) { this.phase = id ; this.setStartTime(); }
		public TimingReport() { this.setStartTime(); }
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.getPhase());
			sb.append("\n\tSTART: ");
			sb.append(new DateTime(this.getStartTime()).toString());
			sb.append("ms\n\tEND: ");
			sb.append(new DateTime(this.getEndTime()).toString());
			sb.append("ms\n\tTOTAL: ");
			sb.append(this.getElapsed());
			sb.append("ms\n\tSTATUS: ");
			sb.append(this.getStatus());
			sb.append("\n");
			return sb.toString();
		}
	}
	
	private class TimingCollection extends TreeMap<String,TimingReport>{
		String phase;
		String status;
		long startTime;
		long endTime;
		long elapsed;
		public void start(JobPojo job) { this.put(job.toString(), new TimingReport(job.toString())); }
		public void start(String string) { this.put(string, new TimingReport(string)); }
		public void fail(String string) { this.get(string).end(); this.get(string).setStatus("FAIL"); }
		public void fail(JobPojo job) { this.fail(job.toString());}
		public void ok(String string) { this.get(string).end(); this.get(string).setStatus("OK"); }
		public void ok(JobPojo job) { this.ok(job.toString()); }
		public TimingCollection(JobPojo workflowJob) {
			this.phase = workflowJob.getId();
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("WORKFLOW REPORT\n");
			sb.append("---------------\n");
			ArrayList<TimingReport> timingReports = new ArrayList<TimingReport>(this.values());
			Collections.sort(timingReports);
			for (TimingReport timing : timingReports) {
				sb.append(timing.toString());
			}
			return sb.toString();
		}
	}

    public static String PARAM_POLL_INTERVAL = "pollInterval";
    public static String PARAM_JOB_TIMEOUT = "jobTimeout";
    public static String PARAM_COMPLETE_LOG = "completeLog";
    public static String PARAM_WORKFLOW = "workflow";


    /**
     * The WorkflowJob object for the worker part of the service (to be used in
     * the run() method)
     */
    private JobPojo workflowJob;

    public JobPojo getWorkflowJob() {
        return workflowJob;
    }

    public void setWorkflowJob(JobPojo workflowJob) {
        this.workflowJob = workflowJob;
    }


    @Override
    public WebservicePojo getWebServicePojo() {
        WebservicePojo ws = new WebservicePojo();
        ws.setLabel("General Workflow Execution Service.");
        ws.setComment("This service can not be called directly. " +
                "Instead, it provides services for workflows. " +
                "Just add the ID of the worflow as path parameter.");
		return ws;
	}


    /**
     * GET /{id}/job/{jobId}		Accept: *		Content-Type: RDF
     */
    @GET
    @Path("/{id}/job/{resourceId}")
    @Produces({
            DM2E_MediaType.APPLICATION_RDF_TRIPLES,
            DM2E_MediaType.APPLICATION_RDF_XML,
            DM2E_MediaType.APPLICATION_X_TURTLE,
            DM2E_MediaType.TEXT_PLAIN,
            DM2E_MediaType.TEXT_RDF_N3,
            DM2E_MediaType.TEXT_TURTLE
    })
    public Response getJobRDFHandler(@PathParam("resourceId") String resourceId) {
        log.debug("Job requested as RDF: " + resourceId);
        JobPojo job = WorkerExecutorSingleton.INSTANCE.getJob(resourceId);
        if (job==null) {
            log.debug("Job not found: " + resourceId);
            return Response.status(404).build();
        }
        return Response.ok().entity(getResponseEntity(job.getGrafeo())).build();
    }

    /**
     * GET /{id}/job/{jobId}	CT: JSON
     * @param resourceId the job Id
     */
    @GET
    @Path("/{id}/job/{resourceId}")
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response getJobJSONHandler(@PathParam("resourceId") String resourceId) {
        log.debug("Job requested as JSON: " + resourceId);
        JobPojo job = WorkerExecutorSingleton.INSTANCE.getJob(resourceId);
        if (job==null) {
            log.debug("Job not found: " + resourceId);
            return Response.status(404).build();
        }
        return Response.ok().entity(job).build();
    }

    /**
     * GET /{id}/job/{jobId}/status			Accept: *		Content-Type: TEXT
     * Get the job status as a string.
     * @param resourceId the job ID
     */
    @GET
    @Path("/{id}/job/{resourceId}/status")
    public Response getJobStatus(@PathParam("resourceId") String resourceId) {
        log.debug("Job status requested: " + resourceId);
        JobPojo job = WorkerExecutorSingleton.INSTANCE.getJob(resourceId);
        if (job==null) {
            log.debug("Job not found: " + resourceId);
            return Response.status(404).build();
        }
        return Response.ok(job.getJobStatus()).build();
    }

	/**
	 * GET /{id}/job/{resourceId}/log			Accept: *		Content-Type: TEXT_LOG
	 * @param minLevelStr the minimum log level
	 * @param maxLevelStr the maximum log level
	 */
	@GET
	@Path("{id}/job/{resourceId}/log")
	@Produces(DM2E_MediaType.TEXT_X_LOG)
	public Response listLogEntriesAsLogFile(
			@PathParam("resourceId") String resourceId,
			@QueryParam("minLevel") String minLevelStr,
			@QueryParam("maxLevel") String maxLevelStr) {
        log.debug("Job log requested: " + resourceId);
        JobPojo job = WorkerExecutorSingleton.INSTANCE.getJob(resourceId);
        if (job==null) {
        	log.debug("Job not found: " + resourceId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
		return Response.ok().entity(job.toLogString(minLevelStr, maxLevelStr)).build();
	}

    /**
     * PUT /{id}
     * <p/>
     * (non-Javadoc)
     *
     * @see eu.dm2e.ws.services.AbstractAsynchronousRDFService#putConfigToService(String)
     */
    @PUT
    @Path("/{id}")
    @Consumes({DM2E_MediaType.TEXT_PLAIN})
    @Produces({
            DM2E_MediaType.APPLICATION_RDF_TRIPLES,
            DM2E_MediaType.APPLICATION_RDF_XML,
            DM2E_MediaType.APPLICATION_X_TURTLE,
//		 DM2E_MediaType.TEXT_PLAIN,
            DM2E_MediaType.TEXT_RDF_N3,
            DM2E_MediaType.TEXT_TURTLE,
            MediaType.APPLICATION_JSON
    })
    public Response putConfigToService(String webserviceConfigURI) {

        URI workflowExecutionUri = getRequestUriWithoutQuery();
        URI workflowUri = popPathFromBeginning(workflowExecutionUri, "exec");
        /*
        * Build workflow and service
        */
        WorkflowPojo workflowPojo = new WorkflowPojo();
        try {
            workflowPojo.loadFromURI(workflowUri);
        } catch (Exception e2) {
            return throwServiceError(e2);
        }

        WebservicePojo webservicePojo = new WebservicePojo();
        try {
            webservicePojo.loadFromURI(workflowExecutionUri);
            log.debug("Web Service to be executed: " + webservicePojo.getTerseTurtle());
        } catch (Exception e2) {
            return throwServiceError(e2);
        }


        /*
           * Resolve configURI to WebserviceConfigPojo
           */
        log.warn("Loading webservice config wfConf" + webserviceConfigURI);
        WebserviceConfigPojo wfConf = new WebserviceConfigPojo();
        try {
            wfConf.loadFromURI(webserviceConfigURI, 1);
        } catch (Exception e) {
            return throwServiceError(e);
        }

        /*
           * Validate the configuration
           */
        log.warn("Validating webservice config");
        ValidationReport res = wfConf.validate();
        if (!res.valid()) return throwServiceError(res.toString());

        /*
        * Validate the workflow (connectedness...)
        */
        log.warn("Validating workflow");
        res = workflowPojo.validate();
        if (!res.valid()) return throwServiceError(res.toString());

        /*
           * Build WorkflowJobPojo
           */
        JobPojo newJobPojo = new JobPojo();
        String uuid = UUID.randomUUID().toString();
        newJobPojo.setCreated(DateTime.now());
        newJobPojo.setWebService(webservicePojo);
        newJobPojo.setWebserviceConfig(wfConf);
        newJobPojo.setHumanReadableLabel();
        //newJobPojo.setParentJob(newJobPojo);
        // Temporary ID handling, the job is persisted with a job service URI,
        // but as long as the temporary ID is set, job service redirects here.
        newJobPojo.setTemporaryID(URI.create(workflowExecutionUri.toString() + "/job/" + uuid));
        String id = newJobPojo.publishToService(Config.get(ConfigProp.JOB_BASEURI));
        newJobPojo.setId(id);

        WorkerExecutorSingleton.INSTANCE.addJobPojo(uuid,newJobPojo);

        log.info("JobPojo for workflow constructed by WorkflowExecutionService: {}", newJobPojo);
        newJobPojo.addLogEntry("JobPojo for workflow constructed by WorkflowExecutionService", "TRACE");
        try {
            // newJobPojo.publishToService(client.getJobWebTarget());
        } catch (Exception e1) {
            return throwServiceError(e1);
        }

        /*
           * Let the asynchronous worker handle the job
           */
        log.info("Workflow job is before instantiation :" + newJobPojo);
        WorkflowExecutionService instance = new WorkflowExecutionService();
        instance.setWorkflowJob(newJobPojo);

        WorkerExecutorSingleton.INSTANCE.handleJob(uuid,instance);

        /*
           * Return JobPojo
           */
        return Response.status(202).location(newJobPojo.getIdAsURI()).entity(newJobPojo).build();
    }

    Map<String, WebservicePojo> serviceDescriptions = new HashMap<>();

    /**
     * Get a web service Pojo for a workflow pojo
     * @param wf the {@link WorkflowPojo} 
     * @return the {@link WebservicePojo}
     */
    public WebservicePojo getWebServicePojo(WorkflowPojo wf) {
        WebservicePojo ws = new WebservicePojo();
        String base = uriInfo.getBaseUri().toString();
        String path = this.getClass().getAnnotation(Path.class).value();
        if (base.endsWith("/") && path.startsWith("/")) base = base.substring(0, base.length() - 1);
        ws.setId(base + path + "/" + lastPathElement(wf.getId()));
        ws.setImplementationID(this.getClass().getCanonicalName());
        ws.setLabel("Service for WF: " + wf.getLabel());
        for (ParameterPojo p : wf.getInputParams()) {
            ParameterPojo sp = ws.addInputParameter(lastPathElement(p.getId()));
            sp.setIsRequired(p.getIsRequired());
            sp.setComment(p.getComment());
            sp.setDefaultValue(p.getDefaultValue());
            sp.setParameterType(p.getParameterType());
            sp.setWebservice(ws);
            sp.setLabel(p.getLabel());
        }
        ParameterPojo workflowParam = ws.addInputParameter(PARAM_WORKFLOW);
        workflowParam.setDefaultValue(wf.getId());
        workflowParam.setIsRequired(false);
        workflowParam.setLabel("The workflow connected to this service: " + wf.getLabelorURI());
        workflowParam.setComment("Do not set or change this parameter value!");
        for (ParameterPojo p : wf.getOutputParams()) {
            ParameterPojo sp = ws.addOutputParameter(lastPathElement(p.getId()));
            sp.setIsRequired(p.getIsRequired());
            sp.setComment(p.getComment());
            sp.setDefaultValue(p.getDefaultValue());
            sp.setParameterType(p.getParameterType());
            sp.setWebservice(ws);
            sp.setHasIterations(p.getHasIterations());
            sp.setLabel(p.getLabel());
        }
        return ws;
    }

    /**
     * GET /{id}
     *
     */
    @GET
    @Path("{id}")
    public Response getExecutionServiceBase() {
        URI uri = appendPath(uriInfo.getRequestUri(), "describe");
        return Response.seeOther(uri).build();
    }

    /**
     * GET /{id}/describe
     *
     */
    @GET
    @Path("{id}/describe")
    public Response getServiceDescription(@PathParam("id") String id) {
        if (serviceDescriptions.containsKey(id)) return Response.ok().entity(serviceDescriptions.get(id)).build();


        URI workflowExecutionUri = popPath();
        URI workflowUri = popPathFromBeginning(workflowExecutionUri, "exec");

        WorkflowPojo workflowPojo = new WorkflowPojo();
        log.trace("Loading workflow from " + workflowUri);
        try {
            workflowPojo.loadFromURI(workflowUri);
        } catch (Exception e2) {
            return throwServiceError(e2);
        }
        WebservicePojo wsDesc = this.getWebServicePojo(workflowPojo);
        log.trace(LogbackMarkers.DATA_DUMP, wsDesc.getTerseTurtle());
        serviceDescriptions.put(id, wsDesc);
        return Response.ok().entity(wsDesc).build();
    }

    @GET
    @Path("{id}/param/{paramId}")
    public Response getParamDescription() {
        String baseURIstr = getRequestUriWithoutQuery().toString();
        baseURIstr = baseURIstr.replaceAll("/param/[^/]+$", "");
        URI baseURI;
        try {
            baseURI = new URI(baseURIstr);
        } catch (URISyntaxException e) {
//			throw(e);
            return throwServiceError(e);
        }
        return Response.seeOther(baseURI).build();
    }

    /**
     * GET {id}/blankConfig
     *
     */
    @GET
    @Path("{id}/blankConfig")
    public Response getEmptyConfigForWorkflow() {
	log.warn("About to create blankConfig()");
        URI workflowExecutionUri = popPath();
        URI workflowUri = popPathFromBeginning(workflowExecutionUri, "exec");
        GrafeoImpl g = new GrafeoImpl();
        g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), workflowUri);
	log.warn("blankConfig(): Read Wf from endpoint: " + workflowUri);
	log.warn("Done " + workflowUri);
        if (g.isEmpty()) {
		log.warn("Workflow not found: " + workflowUri);
            return Response.status(404).entity("Workflow not found : " + workflowUri.toString()).build();
        }
        WebservicePojo ws = new WebservicePojo();
	log.warn("blankConfig(): Read WfExec from endpoint: " +workflowExecutionUri);
        ws.loadFromURI(workflowExecutionUri);
        WebserviceConfigPojo wfconf = new WebserviceConfigPojo();
        wfconf.setWebservice(ws);
        for (ParameterPojo inputParam : ws.getInputParams()) {
            ParameterAssignmentPojo ass = wfconf.addParameterAssignment(inputParam.getId(), inputParam.getDefaultValue() == null ? "" : inputParam.getDefaultValue());
		log.debug("Assignment: " + ass);
            ass.setLabel(inputParam.getLabel());
        }
//		for (ParameterPojo inputParam : wf.getOutputParams()) {
//			wfconf.addParameterAssignment(inputParam.getId(), "BLANK");
//		}
        return Response.ok().entity(wfconf).build();
    }


    @Override
    public Response postGrafeo(Grafeo g) {
        WebTarget webResource = client.getConfigWebTarget();
        Response resp = webResource.request().post(g.getNTriplesEntity());
        if (null == resp.getLocation()) {
            log.error("Invalid RDF string posted as configuration.");
            return throwServiceError(resp.readEntity(String.class));
        }
        return this.putConfigToService(resp.getLocation().toString());
    }


    public boolean checkPositionInputComplete(WorkflowPositionPojo pos, WebserviceConfigPojo config) {
        for (ParameterPojo param : config.getWebservice().getInputParams()) {
        	log.trace(pos.getTerseTurtle());
            if (pos.getWorkflow().getConnectorToPositionAndParam(pos, param) != null && config.getParameterAssignmentForParam(param) == null) {
                log.debug("Config not ready at position " + pos.getId() + ", no value for " + param.getLabelorURI());
                return false;
            }
            if (param.getIsRequired() && config.getParameterAssignmentForParam(param) == null) {
                log.debug("Really?");
                return false; // Somewhat redundant, a required param should have a connection...
            }
        }
        log.info("Position " + pos.getLabelorURI() + " is ready to run!");
        return true;
    }

    public JobPojo runPosition(WorkflowPositionPojo pos, WebserviceConfigPojo wsconf, TimingCollection TIMEREPORT) {
        /*
				 * Publish the WebserviceConfig, so it becomes stable
				 */
        wsconf.resetId();
        wsconf.setExecutesPosition(pos);
        wsconf.setWasGeneratedBy(workflowJob);
        
        {
        	String _uid = UUID.randomUUID().toString();
        	TIMEREPORT.start(_uid + " wsconf.publishToService " + pos);
        	wsconf.publishToService(client.getConfigWebTarget());
        	TIMEREPORT.ok(_uid + " wsconf.publishToService " + pos);
        }


        /*Set<WebserviceConfigPojo> startedJobs = parentJob.getWebserviceConfig().getStartedJobs();
        startedJobs.add(wsconf);
        System.out.println(wsconf.getId());
        parentJob.getWebserviceConfig().setStartedJobs(startedJobs);
        System.out.println("domiii: "+ Misc.output(parentJob.getWebserviceConfig().getStartedJobs()));         */

        if (null == wsconf.getId()) {
            throw new RuntimeException("Could not publish webservice config " + wsconf);
        }

        /*
         * Run the webservice
         */
        Response resp = null;
        {
        	String _uid = UUID.randomUUID().toString();
        	TIMEREPORT.start(_uid + " start werbservice " + pos);
        	resp = client.target(wsconf.getWebservice().getId())
        			.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
        			.put(Entity.text(wsconf.getId()));
        	if (202 != resp.getStatus() || null == resp.getLocation()) {
        		//					job.debug(wsconf.getTerseTurtle());
        		TIMEREPORT.fail(_uid + " start werbservice " + pos);
        		throw new RuntimeException("Request to start web service " + wsconf.getWebservice() + " with config " + wsconf + "failed: " + resp.getStatus() + " parentJob " + workflowJob.getLabelorURI());
        	}
        	TIMEREPORT.ok(_uid + " start werbservice " + pos);
        }


        /*
                   * start the job
                   */
        // long timePassed = 0;
        JobPojo webserviceJob = new JobPojo(resp.getLocation());
        workflowJob.getStartedJobs().add(webserviceJob);

        // persist change of the workflow job
        positionsToRun.remove(pos.getId());
        log.debug("Initializing job maps, new job: " + webserviceJob);
        log.debug("THIS is an integer: " + new Integer(0));
        log.debug("Consumed iterations before: " + Misc.output(consumedIterations));
        consumedIterations.put(webserviceJob.getId(), new Integer(0));
        log.debug("Consumed iterations after: " + Misc.output(consumedIterations));
        // NOT HERE due to concurrent modification when we iterate over this map
        // runningJobs.put(webserviceJob.getId(),webserviceJob);
        job2Position.put(webserviceJob.getId(), pos);
        position2Job.put(pos.getId(), webserviceJob);
        return webserviceJob;
    }


    private void propagateAvailableAssignments(WebserviceConfigPojo newConfig, ParameterConnectorPojo conn, WorkflowPojo workflow, WebserviceConfigPojo workflowConfig) {
        // iterate over all input params of the connected position
        for (ParameterPojo param : conn.getToPosition().getWebservice().getInputParams()) {
            ParameterConnectorPojo backConn = workflow.getConnectorToPositionAndParam(conn.getToPosition(), param);
            if (newConfig.getParameterAssignmentForParam(param)!=null) {
                log.debug("Parameter assigned, skipping");
                continue;
            }
            // if there is no connection, we have nothing to do
            if (backConn == null) {
                log.debug("No connection for param: " + param.getLabelorURI());
                continue;
            }
            log.debug("Checking param for reassignment: " + param.getLabelorURI() + "(backConn: " + backConn.getLabelorURI() + ")");
            // if the parameter is iterating, we get new results, nothing to do
            if (backConn.getFromParam().getHasIterations()) {
                log.debug("Iterating parameter, skipping");
                continue;
            }
            // if the connection connects to the workflow, get the assignment or the default value
            if (backConn.hasFromWorkflow()) {
                ParameterAssignmentPojo a = workflowConfig.getParameterAssignmentForParam(backConn.getFromParam());
                String v = a != null ? a.getParameterValue() : backConn.getFromParam().getDefaultValue();
                if (v == null) {
                    log.debug("No value found.");
                    continue;
                }
                log.debug("Reassigning value from workflow param " + backConn.getFromParam().getLabelorURI());
                newConfig.addParameterAssignment(param.getId(), v);
            // same for other positions
            } else if (backConn.hasFromPosition()) {
                ParameterAssignmentPojo a = position2Job.get(backConn.getFromPosition()).getInputParameterAssignmentForParam(backConn.getFromParam());
                if (a == null) continue;
                log.debug("Reassigning value from position " + backConn.getFromPosition().getLabelorURI() + "/ param " + backConn.getFromParam().getLabelorURI());
                newConfig.addParameterAssignment(param.getId(), a.getParameterValue());
            } else {
                throw new RuntimeException("MUST NOT BE!");
            }
        }
    }

    private Map<String,ParameterPojo> wf2ws = new HashMap<>();
    // TODO: This parameter propagating is ugly and eror-prone. we need a better concept...
    private void propagateWorkflowParametersToWebservice(WorkflowPojo workflow, WebserviceConfigPojo workflowConfig, JobPojo job) {
        for (ParameterPojo wfParam : workflow.getInputParams()) {
            // Only propagate once.
            if (workflowConfig.getWebservice().getParamByName(wfParam.getId())!=null) continue;
            ParameterPojo param = workflowConfig.getWebservice().getParamByName(wfParam.getNeedle());
            wf2ws.put(wfParam.getId(),param);
            String value = workflowConfig.getParameterValueByName(param.getNeedle());
            workflowConfig.getWebservice().getInputParams().add(wfParam);
            if (value != null) {
                workflowConfig.addParameterAssignment(wfParam.getId(), value);
                log.debug("Propagated input param " + wfParam.getNeedle() + " to workflow config.");
            }
        }
        for (ParameterPojo wfParam : workflow.getOutputParams()) {
            // Only propagate once.
            if (job.getWebService().getParamByName(wfParam.getId())!=null) continue;
            log.debug("Propagated output param " + wfParam.getId() + " to job.");
            ParameterPojo param = job.getWebService().getParamByName(wfParam.getNeedle());
            if (param!=null) {
                log.debug("Corresponding WS param: " + param.getId());
                wf2ws.put(wfParam.getId(),param);
            } else {
                // TODO: completeLog is not available in webservice, why? See below
                log.warn("No corresponding WS param found for: " + wfParam.getId());
            }
            job.getWebService().getOutputParams().add(wfParam);
            log.debug(Misc.output(job.getWebService().getOutputParams()));
        }
    }


    private void propagateWorkflowOutputAssignmentsToWebservice(ParameterAssignmentPojo ass, JobPojo job) {
        if (ass==null) {
            log.debug("No assignment to propagate. Skipping.");
            return;
        }
        log.debug("WFPARAM->WSPARAM: WFPARAM=" + ass.getForParam().getId());
        ParameterPojo wsParam = wf2ws.get(ass.getForParam().getId());
        if (wsParam==null) {
            // TODO: completeLog is not available in webservice, why? See above
            log.warn("Skipping, no parameter mapping found for: " + ass.getForParam().getId());
            return;
        }
        log.debug("WSPARAM="+wsParam.getId());
        try {
            job.addOutputParameterAssignment(wsParam.getId(),ass.getParameterValue());
        } catch (Throwable t) {
            log.warn("Could not add assignment for parameter: " + wsParam.getId(), t);
        }
    }

    private Map<String, WebserviceConfigPojo> positionsToRun = new HashMap<>();  // key is position
    private Map<String, JobPojo> runningJobs = new HashMap<>(); // key is job
    private Map<String, WorkflowPositionPojo> job2Position = new HashMap<>(); // key is job
    private Map<String, Integer> consumedIterations = new HashMap<>(); // key is job
    private Map<String, JobPojo> finishedJobs = new HashMap<>(); // key is job
    private Map<String, JobPojo> position2Job = new HashMap<>(); // key is position
    // an iterating workflow is a workflow that has an iterating output parameter, i.e.,
    // one of the iterating parameters of an iterating service is connected to
    // the output parameters of the workflow. we set this to true if and when this happens.
    private boolean iteratingWorkflow = false;

    /**
     * @see Runnable#run()
     */
    @Override
    public void run() {
        log.info("Workflow workflowJob run() for " + workflowJob.getWebService());
        WebserviceConfigPojo workflowConfig = workflowJob.getWebserviceConfig();
        WorkflowPojo workflow = new WorkflowPojo();
        TimingCollection TIMEREPORT = new TimingCollection(workflowJob);
        TIMEREPORT.start("LOAD_WORKFLOW");
        workflow.loadFromURI(workflowJob.getWebService().getParamByName(PARAM_WORKFLOW).getDefaultValue(), 1);
        for (WorkflowPositionPojo pos : workflow.getPositions()) {
        	pos.setWorkflow(workflow);
        }
        TIMEREPORT.ok("LOAD_WORKFLOW");

        try {
            try {
            	TIMEREPORT.start("WORKFLOW_SANITY_CHECK");
                workflowJob.getId().toString();
                workflow.getId().toString();
                workflowConfig.getId().toString();
                log.info("Workflow in run before validation: " + workflow.getTerseTurtle());
            	TIMEREPORT.ok("WORKFLOW_SANITY_CHECK");
            } catch (NullPointerException e) {
            	TIMEREPORT.fail("WORKFLOW_SANITY_CHECK");
                throw e;
            }

            log.info("Job used in run(): " + workflowJob);

            /*
                * Validate
                */
            try {
            	TIMEREPORT.start("VALIDATE_WORKFLOW_CONFIG");
                workflowConfig.validate();
            	TIMEREPORT.ok("VALIDATE_WORKFLOW_CONFIG");
            } catch (Throwable t) {
            	TIMEREPORT.fail("VALIDATE_WORKFLOW_CONFIG");
                throw (t);
            }

            // The workflow parameters are used in the following, but the webservice has different parameters. Ugly, ugly...
            TIMEREPORT.start("PROPAGATE_WORKFLOW_TO_CONFIG");
            propagateWorkflowParametersToWebservice(workflow, workflowConfig, workflowJob);
            TIMEREPORT.ok("PROPAGATE_WORKFLOW_TO_CONFIG");


            /*
                * Get global meta parameters (pollinterval, jobtimeout ...)
                */
            long pollInterval = Long.parseLong(workflow.getParamByName(PARAM_POLL_INTERVAL).getDefaultValue());
            if (null != workflowConfig.getParameterAssignmentForParam(PARAM_POLL_INTERVAL)) {
                pollInterval = Long.parseLong(workflowConfig.getParameterAssignmentForParam(PARAM_POLL_INTERVAL).getParameterValue());
            }
            long jobTimeoutInterval = Long.parseLong(workflow.getParamByName(PARAM_JOB_TIMEOUT).getDefaultValue());
            if (null != workflowConfig.getParameterAssignmentForParam(PARAM_JOB_TIMEOUT)) {
                jobTimeoutInterval = Long.parseLong(workflowConfig.getParameterAssignmentForParam(PARAM_JOB_TIMEOUT).getParameterValue());
            }

            /*
                *
                */
            workflowJob.setStarted();


            /*
                * New scheduling and routing algorithm
                */

            // First: propagate all workflow input parameters to the connected positions.
            log.info("Checking connections from workflow inputs...");
            TIMEREPORT.start("PROPAGATE_PARAMS: " + workflowJob);
            for (ParameterPojo param : workflow.getInputParams()) {
                log.debug("Checking WF param: " + param.getLabelorURI());
                ParameterAssignmentPojo ass = workflowConfig.getParameterAssignmentForParam(param);
                // if the parameter has no assignment, propagate its default value,
                String value = ass != null ? ass.getParameterValue() : param.getDefaultValue();
                log.debug("PARAM:" + param.getLabelorURI() + " VALUE: " + value + " #CONNS: " + workflow.getConnectorFromWorkflowInputParam(param).size());
                for (ParameterConnectorPojo conn : workflow.getConnectorFromWorkflowInputParam(param)) {
                    // it could be that an input parameter is directly connected to an output parameter
                	// The GUI doesn't allow that though. kb Mon Nov 25 00:47:36 CET 2013
                    if (conn.hasToWorkflow()) {
                        if (workflowJob.getOutputParameterAssignmentForParam(conn.getToParam()) != null) {
                        	TIMEREPORT.fail("PROPAGATE_PARAMS");
                            throw new RuntimeException("Multiple assignments for workflow outputs not yet supported!");
                        }
                        workflowJob.addOutputParameterAssignment(conn.getToParam().getId(), value);
                    }
                    WorkflowPositionPojo target = conn.getToPosition();
                    // all connected positions are put to our map of positions that should run at some point
                    if (!positionsToRun.containsKey(target.getId()))
                        positionsToRun.put(target.getId(), target.getWebservice().createConfig());
                    WebserviceConfigPojo config = positionsToRun.get(target.getId());
                    config.addParameterAssignment(conn.getToParam().getId(), value);
                    // whenever a new assignment is propagated, we test if the position ha all connected inputs to run
                    if (checkPositionInputComplete(target, config)) {
                    	String _uid = UUID.randomUUID().toString();
                    	TIMEREPORT.start(_uid + " runPosition() " + target);
                        JobPojo newJob = runPosition(target, config, TIMEREPORT);
                    	TIMEREPORT.ok(_uid + " runPosition() " + target);
                        // Running a position means to remove it from the positionsToRun and add its workflowJob to the runningJobs
                        runningJobs.put(newJob.getId(), newJob);
                    }
                }
            }
            TIMEREPORT.ok("PROPAGATE_PARAMS: " + workflowJob);
            // workflowJob.publishToService();
            for (JobPojo runningJob : runningJobs.values()) {
            	TIMEREPORT.start(runningJob);
            }

            // Next: Constantly iterate over all running jobs and propagate new results to positions until no further jobs are running
            log.info("Start polling... Interval: " + pollInterval);
            long tick = 0;
            while (!positionsToRun.isEmpty() || !runningJobs.isEmpty()) {
            	tick += 1;
                workflowJob.trace("Sleeping for " + pollInterval + "ms, waiting for jobs to finish.");
                try {
                	TIMEREPORT.start("SLEEP_" + tick + ": " + workflowJob);
                    Thread.sleep(pollInterval);
                	TIMEREPORT.ok("SLEEP_" + tick + ": " + workflowJob);
                } catch (Exception e) {
                	TIMEREPORT.fail("SLEEP_" + tick + ": " + workflowJob);
                    throw new RuntimeException(e);
                }

                log.debug("Checking all running jobs.");
                log.debug("Running Jobs: " + Misc.output(runningJobs));
                log.debug("Positions to run: " + Misc.output(positionsToRun));
                if (runningJobs.isEmpty() && !positionsToRun.isEmpty()) {
                    // As it is said, this should not happen. After the first propagations,
                    // at least one position must run or the system would not change any more.
                    // When nothing runs, nothing can change any more, so either we are finished
                    // or we have a problem.
                    // TODO: not terminating workflows are not yet detected properly. ALso a pretest for cycles or unreachable positions would be nice.
                    throw new RuntimeException("Should not happen!");
                }

                Map<String, JobPojo> tmpRunningJobs = new HashMap<>();
                Iterator it = runningJobs.keySet().iterator();
                // iterate over all running jobs. new jobs are stored in the tmp map and added after the loop to prevent a concurrent modification of the map.
                while (it.hasNext()) {
                    String key = (String) it.next();
                    JobPojo webserviceJob = runningJobs.get(key);
                    TIMEREPORT.start("RUN: " + webserviceJob);
                    TIMEREPORT.start("LOAD: " + webserviceJob);
                    webserviceJob.refresh(0, true);
                    TIMEREPORT.ok("LOAD: " + webserviceJob);
                    /*
                    timePassed += pollInterval;
                    if (timePassed > jobTimeoutInterval*1000) {
                    throw new RuntimeException("Job " + webserviceJob + " took more than " + jobTimeoutInterval + "s too long to finish :(");
                    }
                    log.info("JOB STATUS: " +webserviceJob.getTerseTurtle());
                    */
                    log.debug("Consumed Iterations: " + Misc.output(consumedIterations));
                    log.debug("Latest Result: " + webserviceJob.getLatestResult());
                    // when the workflowJob is finished, failed OR it is iterating and new results are available, then we have to propagate the results
                    if (!webserviceJob.isStillRunning() || webserviceJob.getLatestResult() > consumedIterations.get(webserviceJob.getId())) {
                        log.info("Finished workflowJob or new iteration");

                        // Finished or Failed: Clean up, i.e., remove from running and put to finished.
                        if (!webserviceJob.isStillRunning()) {
                            log.info("Job finished, cleaning up.");
                            it.remove();
                            TIMEREPORT.ok("RUN: " + webserviceJob);
                            finishedJobs.put(webserviceJob.getId(), webserviceJob);
                            workflowJob.addFinishedJob(webserviceJob);
                            // workflowJob.publishToService();
                        }

                        // Failed: throw exception
                        if (webserviceJob.isFailed()) {
                            log.error("JOB FAILED");
                            TIMEREPORT.fail("RUN: " + webserviceJob);
                            throw new RuntimeException("Job " + webserviceJob + " of Webservice " + job2Position.get(webserviceJob.getId()).getWebservice() + "failed, hence workflow " + workflow + "failed. :(");
                        // Finished or iterating: propagate new results
                        } else {

                            workflowJob.info("Job " + webserviceJob + " of Webservice " + job2Position.get(webserviceJob.getId()).getWebservice() + "finished or iterated successfully, propagating to next position.");

                            // determine the results that are not yet processed
                            int alreadySeen = consumedIterations.get(webserviceJob.getId());
                            int available = webserviceJob.getLatestResult();
                            // iterate over all iterations that are not yet processed
                            for (int i = alreadySeen + 1; i <= available; i++) {
                                log.info("Loading new results from (not inclusive) " + alreadySeen + " to  " + available);
                                // iterate over all iterating outputparameters
                                for (ParameterAssignmentPojo ass : webserviceJob.getOutputParameterAssignments(i)) {
                                    log.debug("Output param: " + ass.getForParam().getLabelorURI());
                                    String value = ass.getParameterValue();
                                    // Iterate over all connections from this output parameter
                                    for (ParameterConnectorPojo conn : workflow.getConnectorFromPositionAndParam(job2Position.get(webserviceJob.getId()), ass.getForParam())) {
                                        // if it is a workflow parameter, propagate the assignment
                                        if (conn.hasToWorkflow()) {
                                            // The propagated params are lost whenever the pojos are reloaded... as I said: ugly...
                                            propagateWorkflowParametersToWebservice(workflow, workflowConfig, workflowJob);
                                            log.debug("All output params: " + Misc.output(workflowJob.getWebService().getOutputParams()));
                                            ParameterAssignmentPojo newAss = workflowJob.addOutputParameterAssignment(conn.getToParam().getId(), value);
                                            propagateWorkflowOutputAssignmentsToWebservice(newAss, workflowJob);
                                            iteratingWorkflow = true;
                                            if (!conn.getToParam().getHasIterations()) throw new RuntimeException("Shame on you, this output parameter must be iterating, too! BTW, this=" + conn.getToParam().getId());
                                        } else {
                                            WorkflowPositionPojo target = conn.getToPosition();
                                            WebserviceConfigPojo config = null;
                                            if (!positionsToRun.containsKey(target.getId())) {
                                                log.debug("Creating new config for next position: " + target.getLabelorURI());
                                                positionsToRun.put(target.getId(), target.getWebservice().createConfig());
                                                config = positionsToRun.get(target.getId());
                                                // If there are other input parameters than iterating ones, we have to reassign them to the new config
                                                // This is necessary as parameter assignments are pushed when they are available, not pulled when they are consumed
                                                propagateAvailableAssignments(config,conn,workflow,workflowConfig);
                                            } else {
                                                config = positionsToRun.get(target.getId());
                                            }
                                            // propaget the new results to the next positions
                                            config.addParameterAssignment(conn.getToParam().getId(), value);
                                            // if the position is ready, run it
                                            if (checkPositionInputComplete(target, config)) {
                                                JobPojo newJob = runPosition(target, config, TIMEREPORT);
                                                TIMEREPORT.start("RUN: " + newJob);
                                                tmpRunningJobs.put(newJob.getId(), newJob);
                                            }
                                        }
                                    }
                                }
                                // with every iteration, the workflow also iterates, if an iterating parameter
                                // is connected to an iterating output parameter of the workflow.
                                if (iteratingWorkflow) workflowJob.iterate();

                            }
                            consumedIterations.put(webserviceJob.getId(), available);
                            // During an iteration, only iterating parameters are propagated. When finished, propagate also non-iterating parameters, if available
                            if (webserviceJob.isFinished()) {
                            	TIMEREPORT.ok("RUN: " + webserviceJob);
                                log.info("Job is finished, propagating results...");
                                for (ParameterAssignmentPojo ass : webserviceJob.getNonIteratingOutputParameterAssignments()) {
                                    log.debug("Param: " + ass.getForParam().getLabelorURI());
                                    String value = ass.getParameterValue();
                                    for (ParameterConnectorPojo conn : workflow.getConnectorFromPositionAndParam(job2Position.get(webserviceJob.getId()),ass.getForParam())) {
                                        if (conn.hasToWorkflow()) {
                                            if (workflowJob.getOutputParameterAssignmentForParam(conn.getToParam()) != null) {
                                                // TODO: Here we need a conceptional solution...
                                                log.error("Multiple assignments of a non-iterating workflow output!");
                                            }
                                            workflowJob.addOutputParameterAssignment(conn.getToParam().getId(), value);
                                        } else {
                                            WorkflowPositionPojo target = conn.getToPosition();
                                            WebserviceConfigPojo config = null;
                                            if (!positionsToRun.containsKey(target.getId())) {
                                                positionsToRun.put(target.getId(), target.getWebservice().createConfig());
                                                config = positionsToRun.get(target.getId());
                                                // If there are other input parameters than iterating ones, we have to reassign them to the new config
                                                // This is necessary as parameter assignments are pushed when they are available, not pulled when they are consumed
                                                config.addParameterAssignment(conn.getToParam().getId(), value);
                                                propagateAvailableAssignments(config,conn,workflow,workflowConfig);
                                            } else {
                                                config = positionsToRun.get(target.getId());
                                                config.addParameterAssignment(conn.getToParam().getId(), value);
                                            }


                                            if (checkPositionInputComplete(target, config)) {
                                                JobPojo newJob = runPosition(target, config, TIMEREPORT);
                                                TIMEREPORT.start("RUN: " + newJob);
                                                tmpRunningJobs.put(newJob.getId(), newJob);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Add new running jobs
                runningJobs.putAll(tmpRunningJobs);
                tmpRunningJobs.clear();
            }

            // Handling non-iterating workflow outputs and propagating them to the service... ugly...
            for (ParameterPojo param:workflow.getOutputParams()) {
                    if (param.getHasIterations()) continue;
                    propagateWorkflowOutputAssignmentsToWebservice(workflowJob.getOutputParameterAssignmentForParam(param), workflowJob);
            }

            workflowJob.setFinished();
        } catch (Throwable t) {
            log.error("Workflow " + workflowJob + " FAILED: " + t + "\n" + ExceptionUtils.getStackTrace(t));
            workflowJob.fatal("Workflow " + workflowJob + " FAILED: " + t + "\n" + ExceptionUtils.getStackTrace(t));
            workflowJob.setFailed();
            // TODO why can't I throw this here but in AbstractTransformationService??
            throw new RuntimeException(t);
        } finally {

            // output one giant log containing everything for debugging
//               JobPojo dummyJob = new JobPojo();
//               Set<JobPojo> allLoggingJobs = new HashSet<>();
//               allLoggingJobs.addAll(finishedJobs.values());
//               allLoggingJobs.add(workflowJob);
//               for (JobPojo j : allLoggingJobs) {
//                   for (LogEntryPojo logEntry : j.getLogEntries()) {
//                       LogEntryPojo newLogEntry = new LogEntryPojo();
//                       newLogEntry.setTimestamp(logEntry.getTimestamp());
//                       newLogEntry.setLevel(logEntry.getLevel());
//                       newLogEntry.setMessage( j + ": " + logEntry.getMessage());
//                       dummyJob.getLogEntries().add(newLogEntry);
//                   }
//               }
               String outputStr = TIMEREPORT.toString();
               outputStr += "\n";
               String uriStr = workflowJob.getId();
               String fileName = uriStr.replaceAll("[^A-Za-z0-9_]", "_");
               fileName = fileName.replaceAll("__+", "_");
               File f = new File(String.format(
            		   "%s/%s",
            		   Config.get(ConfigProp.FILE_STOREDIR),
            		   fileName));
               if (!f.getParentFile().exists()) {
            	   f.getParentFile().mkdirs();
               }
               log.info("Store file as: {}", f.getAbsolutePath());
               try {
            	   IOUtils.write(outputStr, new FileOutputStream(f));
               } catch (IOException e) {
            	   // TODO Auto-generated catch block
            	   e.printStackTrace();
            	   log.debug("" + e);
            	   // nothing we can do at this point anyway
               }
//               outputStr += dummyJob.toLogString();
//               workflowJob.addOutputParameterAssignment(PARAM_COMPLETE_LOG, "DIS");
//               workflowJob.publishToService();
//               client.getJobWebTarget()
//               		 .path("byURI")
//               		 .queryParam("uri", workflowJob.getId())
//               		 .request()
//               		 .put(workflowJob.getNTriplesEntity());
        }
    }
}
