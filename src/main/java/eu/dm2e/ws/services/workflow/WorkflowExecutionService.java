package eu.dm2e.ws.services.workflow;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.grafeo.util.LogbackMarkers;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.ParameterConnectorPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
import eu.dm2e.ws.services.AbstractAsynchronousRDFService;
import eu.dm2e.ws.services.WorkerExecutorSingleton;

/**
 * Service for the creation and execution of workflows
 */
@Path("/exec/workflow")
public class WorkflowExecutionService extends AbstractAsynchronousRDFService {

	public static String PARAM_POLL_INTERVAL = "pollInterval";
	public static String PARAM_JOB_TIMEOUT = "jobTimeout";
    public static String PARAM_COMPLETE_LOG = "completeLog";
    public static String PARAM_WORKFLOW = "workflow";



    /**
     * The WorkflowJob object for the worker part of the service (to be used in
     * the run() method)
     */
    private JobPojo jobPojo;
    public JobPojo getJobPojo() { return jobPojo; }
    public void setJobPojo(JobPojo jobPojo) { this.jobPojo = jobPojo; }


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
	 * PUT /{id}
	 *
	 *  (non-Javadoc)
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
		try {
			wfConf.validate();
		} catch (Exception e) {
			return throwServiceError(e);
		}

		/*
		 * Build WorkflowJobPojo
		 */
		JobPojo newJobPojo = new JobPojo();
		newJobPojo.setCreated(DateTime.now());
		newJobPojo.setWebService(webservicePojo);
		newJobPojo.setWebserviceConfig(wfConf);
        newJobPojo.setHumanReadableLabel();



		log.info("JobPojo for workflow constructed by WorkflowExecutionService: {}", newJobPojo);
		newJobPojo.addLogEntry("JobPojo for workflow constructed by WorkflowExecutionService", "TRACE");
		try {
			newJobPojo.publishToService(client.getJobWebTarget());
		} catch (Exception e1) {
			return throwServiceError(e1);
		}

		/*
		 * Let the asynchronous worker handle the job
		 */
		log.info("Workflow job is before instantiation :" + newJobPojo);
			WorkflowExecutionService instance = new WorkflowExecutionService();
            instance.setJobPojo(newJobPojo);
			WorkerExecutorSingleton.INSTANCE.handleJob(instance);

		/*
		 * Return JobPojo
		 */
		return Response.status(202).location(newJobPojo.getIdAsURI()).entity(newJobPojo).build();
	}

    Map<String,WebservicePojo> serviceDescriptions = new HashMap<>();
    public  WebservicePojo getWebServicePojo(WorkflowPojo wf) {
        if (serviceDescriptions.containsKey(wf.getId())) return serviceDescriptions.get(wf.getId());
        WebservicePojo ws = new WebservicePojo();
        String base = uriInfo.getBaseUri().toString();
        String path = this.getClass().getAnnotation(Path.class).value();
        if (base.endsWith("/") && path.startsWith("/")) base = base.substring(0,base.length()-1);
        ws.setId(base + path + "/" + lastPathElement(wf.getId()));
        ws.setImplementationID(this.getClass().getCanonicalName());
        ws.setLabel("Service for WF: " + wf.getLabel());
        for (ParameterPojo p:wf.getInputParams()) {
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
        for (ParameterPojo p:wf.getOutputParams()) {
            ParameterPojo sp = ws.addOutputParameter(lastPathElement(p.getId()));
            sp.setIsRequired(p.getIsRequired());
            sp.setComment(p.getComment());
            sp.setDefaultValue(p.getDefaultValue());
            sp.setParameterType(p.getParameterType());
            sp.setWebservice(ws);
            sp.setLabel(p.getLabel());
        }
        serviceDescriptions.put(wf.getId(),ws);
        return ws;
    }

    /**
     * GET /{id}
     * @return
     */
    @GET
    @Path("{id}")
    public Response getExecutionServiceBase()  {
        URI uri = appendPath(uriInfo.getRequestUri(),"describe");
        return Response.seeOther(uri).build();
    }

    /**
	 * GET /{id}/describe
	 * @return
	 */
	@GET
	@Path("{id}/describe")
	public Response getServiceDescription() {
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
     * @return
     */
    @GET
    @Path("{id}/blankConfig")
    public Response getEmptyConfigForWorkflow() {
        URI workflowExecutionUri = popPath();
        URI workflowUri = popPathFromBeginning(workflowExecutionUri, "exec");
        GrafeoImpl g = new GrafeoImpl();
		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), workflowUri);
		if (g.isEmpty()) {
			return Response.status(404).build();
		}
        WebservicePojo ws = new WebservicePojo();
        ws.loadFromURI(workflowExecutionUri);
        WebserviceConfigPojo wfconf = new WebserviceConfigPojo();
		wfconf.setWebservice(ws);
		for (ParameterPojo inputParam : ws.getInputParams()) {
			ParameterAssignmentPojo ass = wfconf.addParameterAssignment(inputParam.getId(), inputParam.getDefaultValue());
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
        for (ParameterPojo param:config.getWebservice().getInputParams()) {
            if (pos.getWorkflow().getConnectorToPositionAndParam(pos,param)!=null && config.getParameterAssignmentForParam(param)==null)  {
                log.debug("Config not ready at position " + pos.getId() + ", no value for " + param.getLabelorURI());
                return false;
            }
            if (param.getIsRequired() && config.getParameterAssignmentForParam(param)==null) {
                log.debug("Really?");
                return false; // Somewhat redundant, a required param should have a connection...
            }
        }
        log.info("Position " + pos.getLabelorURI() + " is ready to run!");
        return true;
    }

    public JobPojo runPosition(WorkflowPositionPojo pos, WebserviceConfigPojo wsconf) {
        /*
				 * Publish the WebserviceConfig, so it becomes stable
				 */
        wsconf.resetId();
        wsconf.setExecutesPosition(pos);
        wsconf.publishToService(client.getConfigWebTarget());
        if (null == wsconf.getId()) {
            throw new RuntimeException("Could not publish webservice config " + wsconf);
        }

        /*
                   * Run the webservice
                   */
        Response resp = client.target(wsconf.getWebservice().getId())
                .request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
                .put(Entity.text(wsconf.getId()));
        if (202 != resp.getStatus() || null == resp.getLocation()) {
//					job.debug(wsconf.getTerseTurtle());
            throw new RuntimeException("Request to start web service " + wsconf.getWebservice() + " with config " + wsconf + "failed: " + resp);
        }


        /*
                   * start the job
                   */
        // long timePassed = 0;
        JobPojo webserviceJob = new JobPojo(resp.getLocation());

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



    private Map<String,WebserviceConfigPojo> positionsToRun = new HashMap<>();  // key is position
    private Map<String, JobPojo> runningJobs = new HashMap<>(); // key is job
    private Map<String, WorkflowPositionPojo> job2Position = new HashMap<>(); // key is job
    private Map<String, Integer> consumedIterations = new HashMap<>(); // key is job
    private Map<String, JobPojo> finishedJobs = new HashMap<>(); // key is job
    private Map<String, JobPojo> position2Job = new HashMap<>(); // key is position

    /**
	 * @see Runnable#run()
	 */
	@Override
	public void run() {
		JobPojo job = getJobPojo();
        log.info("Workflow job run() for " + job.getWebService());
        WebserviceConfigPojo workflowConfig = job.getWebserviceConfig();
		WorkflowPojo workflow = new WorkflowPojo();
        workflow.loadFromURI(job.getWebService().getParamByName(PARAM_WORKFLOW).getDefaultValue());

        try {
			try {
				job.getId().toString();
				workflow.getId().toString();
				workflowConfig.getId().toString();
				log.info("Workflow in run before validation: " + workflow.getTerseTurtle());
			} catch (NullPointerException e) {
				throw e;
			}

			log.info("Job used in run(): " + job);

			/*
			 * Validate
			 */
			try {
				workflowConfig.validate();
			} catch (Throwable t) {
				throw(t);
			}

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
			job.setStarted();




			/*
			 * New scheduling and routing algorithm
			 */
            log.info("Checking connections from workflow inputs...");
            for (ParameterPojo param:workflow.getInputParams()) {
                log.debug("Checking WF param: " + param.getLabelorURI());
                ParameterAssignmentPojo ass = workflowConfig.getParameterAssignmentForParam(param);
                 String value = ass!=null?ass.getParameterValue():param.getDefaultValue();
                log.debug("PARAM:" + param.getLabelorURI() + " VALUE: " + value + " #CONNS: " + workflow.getConnectorFromWorkflowInputParam(param).size());
                 for (ParameterConnectorPojo conn:workflow.getConnectorFromWorkflowInputParam(param)) {
                     if (conn.hasToWorkflow()) {
                         if (job.getOutputParameterAssignmentForParam(conn.getToParam())!=null) {
                             throw new RuntimeException("Multiple assignments for workflow outputs not yet supported!");
                         }
                         job.addOutputParameterAssignment(conn.getToParam().getId(),value);
                     }
                     WorkflowPositionPojo target = conn.getToPosition();
                     if (!positionsToRun.containsKey(target.getId())) positionsToRun.put(target.getId(),target.getWebservice().createConfig());
                     WebserviceConfigPojo config = positionsToRun.get(target.getId());
                     config.addParameterAssignment(conn.getToParam().getId(), value);
                     if (checkPositionInputComplete(target,config)) {
                             JobPojo newJob = runPosition(target,config);
                            runningJobs.put(newJob.getId(), newJob);

                     }
                 }

             }
            job.publishToService();

            log.info("Start polling... Interval: " + pollInterval);
            while (!positionsToRun.isEmpty() || !runningJobs.isEmpty()) {
                job.trace("Sleeping for " + pollInterval + "ms, waiting for jobs  to finish.");
                try {
                    Thread.sleep(pollInterval);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                log.debug("Checking all running jobs.");
                log.debug("Running Jobs: " + Misc.output(runningJobs));
                log.debug("Positions to run: " + Misc.output(positionsToRun));
                if (runningJobs.isEmpty() && !positionsToRun.isEmpty()) throw new RuntimeException("Should not happen!");

                Map<String,JobPojo> tmpRunningJobs = new HashMap<>();
                Iterator it = runningJobs.keySet().iterator();
                while (it.hasNext()) {
                        String key = (String) it.next();
                        JobPojo webserviceJob = runningJobs.get(key);
                        webserviceJob.refresh(0,true);
                        /*
                        timePassed += pollInterval;
                        if (timePassed > jobTimeoutInterval*1000) {
                            throw new RuntimeException("Job " + webserviceJob + " took more than " + jobTimeoutInterval + "s too long to finish :(");
                        }
                        log.info("JOB STATUS: " +webserviceJob.getTerseTurtle());
                         */
                    log.debug("Consumed Iterations: " + Misc.output(consumedIterations));
                    log.debug("Latest Result: " + webserviceJob.getLatestResult());
                    if (!webserviceJob.isStillRunning() || webserviceJob.getLatestResult()>consumedIterations.get(webserviceJob.getId())) {
                            log.info("Finished job or new iteration");

                    if (!webserviceJob.isStillRunning()) {
                        // just clearing the runningjobs set will be a problem for parallel jobs
                        log.info("Job finished, cleaning up.");
                        it.remove();
                        finishedJobs.put(webserviceJob.getId(), webserviceJob);
                        job.publishToService();
                    }

                    if (webserviceJob.isFailed()) {
                        log.error("JOB FAILED");
                        throw new RuntimeException("Job " + webserviceJob + " of Webservice " + job2Position.get(webserviceJob.getId()).getWebservice() + "failed, hence workflow " + workflow + "failed. :(");
                    }
                    else  {

                        job.info("Job " + webserviceJob + " of Webservice " + job2Position.get(webserviceJob.getId()).getWebservice() + "finished or iterated successfully, propagating to next position.");

                        int alreadySeen = consumedIterations.get(webserviceJob.getId());
                        int available = webserviceJob.getLatestResult();
                        for (int i = alreadySeen+1;i<=available;i++) {
                            log.info("Loading new results from (not inclusive) " + alreadySeen + " to  " + available);
                            for (ParameterAssignmentPojo ass : webserviceJob.getOutputParameterAssignments(i)) {
                                log.debug("Output param: " + ass.getForParam().getLabelorURI());
                                String value = ass.getParameterValue();
                                for (ParameterConnectorPojo conn:workflow.getConnectorFromPositionAndParam(job2Position.get(webserviceJob.getId()),ass.getForParam())) {
                                    if (conn.hasToWorkflow()) {
                                        job.addOutputParameterAssignment(conn.getToParam().getId(),value);
                                    } else {
                                        WorkflowPositionPojo target = conn.getToPosition();
                                        // DONE: If there are other input parameters than iterating ones, we have to reassign them to the new config
                                        WebserviceConfigPojo config = null;
                                        if (!positionsToRun.containsKey(target.getId())) {
                                            log.debug("Creating new config for next position: " + target.getLabelorURI());
                                            positionsToRun.put(target.getId(),target.getWebservice().createConfig());
                                            config = positionsToRun.get(target.getId());
                                            for (ParameterPojo param:conn.getToPosition().getWebservice().getInputParams()) {
                                                ParameterConnectorPojo backConn = workflow.getConnectorToPositionAndParam(conn.getToPosition(),param);
                                                log.debug("Checking param for reassignment: " + param.getLabelorURI() + "(backConn: " + backConn.getLabelorURI() + ")");
                                                if (backConn==null) {
                                                    log.debug("No connection");
                                                    continue;
                                                }
                                                if (backConn.getFromParam().getHasIterations()) {
                                                    log.debug("Iterating parameter, skipping");
                                                    continue;
                                                }
                                                if (backConn.hasFromWorkflow()) {
                                                    ParameterAssignmentPojo a = workflowConfig.getParameterAssignmentForParam(backConn.getFromParam());
                                                    String v = a!=null?a.getParameterValue():backConn.getFromParam().getDefaultValue();
                                                    if (v==null) {
                                                        log.debug("No value found.");
                                                        continue;
                                                    }
                                                    log.debug("Reassigning value from workflow param " + backConn.getFromParam().getLabelorURI());
                                                    config.addParameterAssignment(param.getId(),v);
                                                }  else if (backConn.hasFromPosition()) {
                                                    ParameterAssignmentPojo a = position2Job.get(backConn.getFromPosition()).getInputParameterAssignmentForParam(backConn.getFromParam());
                                                    if (a==null) continue;
                                                    log.debug("Reassigning value from position " + backConn.getFromPosition().getLabelorURI() + "/ param " + backConn.getFromParam().getLabelorURI());
                                                    config.addParameterAssignment(param.getId(),a.getParameterValue());
                                                }   else {
                                                    throw new RuntimeException("MUST NOT BE!");
                                                }
                                            }
                                        } else {
                                            config = positionsToRun.get(target.getId());
                                        }
                                        config.addParameterAssignment(conn.getToParam().getId(), value);
                                        if (checkPositionInputComplete(target,config)) {
                                            JobPojo newJob= runPosition(target,config);
                                            tmpRunningJobs.put(newJob.getId(), newJob);
                                        }
                                    }
                                }
                            }
                        }
                        consumedIterations.put(webserviceJob.getId(), available);
                        if (webserviceJob.isFinished()) {
                            for (ParameterAssignmentPojo ass : webserviceJob.getNonIteratingOutputParameterAssignments()) {

                                String value = ass.getParameterValue();
                                for (ParameterConnectorPojo conn:workflow.getConnectorFromWorkflowInputParam(ass.getForParam())) {
                                    if (conn.hasToWorkflow()) {
                                        if (job.getOutputParameterAssignmentForParam(conn.getToParam())!=null) {
                                            throw new RuntimeException("Multiple assignments for workflow outputs not yet supported!");
                                        }
                                        job.addOutputParameterAssignment(conn.getToParam().getId(),value);
                                    } else {
                                        WorkflowPositionPojo target = conn.getToPosition();
                                        if (!positionsToRun.containsKey(target.getId())) positionsToRun.put(target.getId(),target.getWebservice().createConfig());
                                        WebserviceConfigPojo config = positionsToRun.get(target.getId());
                                        config.addParameterAssignment(conn.getToParam().getId(), value);
                                        if (checkPositionInputComplete(target,config)) {
                                            JobPojo newJob= runPosition(target,config);
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




			job.setFinished();
		} catch (Throwable t) {
			log.error("Workflow " + job +  " FAILED: " + t + "\n" + ExceptionUtils.getStackTrace(t));
			job.fatal("Workflow " + job + " FAILED: " + t + "\n" + ExceptionUtils.getStackTrace(t));
			job.setFailed();
			// TODO why can't I throw this here but in AbstractTransformationService??
			throw new RuntimeException(t);
		} finally {
			
			// output one giant log containing everything for debugging
	/*		JobPojo dummyJob = new JobPojo();
			Set<JobPojo> allLoggingJobs = new HashSet<>();
			allLoggingJobs.addAll(finishedJobs.values());
			allLoggingJobs.add(job);
			for (JobPojo j : allLoggingJobs) {
				for (LogEntryPojo logEntry : j.getLogEntries()) {
					LogEntryPojo newLogEntry = new LogEntryPojo();
					newLogEntry.setTimestamp(logEntry.getTimestamp());
					newLogEntry.setLevel(logEntry.getLevel());
					newLogEntry.setMessage( j + ": " + logEntry.getMessage());
					dummyJob.getLogEntries().add(newLogEntry);
				}
			}
			job.addOutputParameterAssignment(PARAM_COMPLETE_LOG, dummyJob.toLogString());
			job.publishToService();
			*/
		}
	}
}
