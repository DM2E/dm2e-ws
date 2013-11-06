package eu.dm2e.ws.services.workflow;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.*;
import eu.dm2e.ws.services.AbstractAsynchronousRDFService;
import eu.dm2e.ws.services.WorkerExecutorSingleton;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
            log.debug("Web Service to be executed: "+ webservicePojo.getTerseTurtle());
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
        workflowParam.setLabel("The workflow connected to this service: " + wf.getLabel());
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
        try {
            workflowPojo.loadFromURI(workflowUri);
        } catch (Exception e2) {
            return throwServiceError(e2);
        }
        WebservicePojo wsDesc = this.getWebServicePojo(workflowPojo);
        log.trace(wsDesc.getTerseTurtle());
        return Response.ok().entity(wsDesc).build();
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
        Set<JobPojo> runningJobs = new HashSet<>();
        Map<String,JobPojo> finishedJobs = new HashMap<>();

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
			 * Iterate Positions
			 */
			for (WorkflowPositionPojo pos : workflow.getPositions()) {
				WebservicePojo ws = pos.getWebservice();
				job.addLogEntry("Re-loading webservice description", "TRACE");
				ws.loadFromURI(ws.getId());
				WebserviceConfigPojo wsconf = new WebserviceConfigPojo();
				wsconf.setWebservice(ws);
				wsconf.setWasGeneratedBy(job);

				/*
				 * Iterate Input Parameters of the Webservice at this position
				 */
				job.addLogEntry("About to iterate parameters", "TRACE");
				nextParam:
				for (ParameterPojo param : ws.getInputParams()) {
					job.trace("Generating assignment for param " + param);
//					job.addLogEntry("Current param: " + param, "TRACE");
					job.publishToService();
					log.trace("Current param: " + param);

					// if there is a connector to this parameter at this position
					ParameterConnectorPojo conn = workflow.getConnectorToPositionAndParam(pos, param);
					if (null == conn) continue nextParam;

					final ParameterAssignmentPojo ass;
					if (conn.hasFromWorkflow()) {
						// FIXME this is not working for whatever reason
						// if the connector is from the workflow, take the value assigned to the workflow parameter
						ass = workflowConfig.getParameterAssignmentForParam(conn.getFromParam());
					} else {
						// if the connector is from a previous position, take the value from the corresponding previous job
						ass = finishedJobs.get(conn.getFromPosition().getId()).getOutputParameterAssignmentForParam(conn.getFromParam());
						job.debug("Finished Jobs: " + finishedJobs.keySet());
						job.debug("This connector fromPosition: " + conn.getFromPosition());
					}
					if (ass == null) {
						job.debug(workflowConfig.getTerseTurtle());
						throw new RuntimeException("Couldn't get the assignment for param " + param);
					}
					wsconf.addParameterAssignment(param.getId(), ass.getParameterValue());
				}

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
				Response resp = client.target(ws.getId())
						.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
						.put(Entity.text(wsconf.getId()));
				if (202 != resp.getStatus() || null == resp.getLocation()) {
//					job.debug(wsconf.getTerseTurtle());
					throw new RuntimeException("Request to start web service " + ws + " with config " + wsconf + "failed: " + resp);
				}


				/*
				 * start the job
				 */
				long timePassed = 0;
				JobPojo webserviceJob = new JobPojo(resp.getLocation());

                // persist change of the workflow job
				runningJobs.add(webserviceJob);
				job.publishToService();
				do {
					webserviceJob.loadFromURI(webserviceJob.getId());
					job.trace("Sleeping for " + pollInterval + "ms, waiting for job " + webserviceJob + " to finish.");
					try {
						Thread.sleep(pollInterval);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					timePassed += pollInterval;
					if (timePassed > jobTimeoutInterval*1000) {
						throw new RuntimeException("Job " + webserviceJob + " took more than " + jobTimeoutInterval + "s too long to finish :(");
					}
					log.info("JOB STATUS: " +webserviceJob.getTerseTurtle());
					
				} while (webserviceJob.isStillRunning());

				// just clearing the runningjobs set will be a problem for parallel jobs
				runningJobs.remove(webserviceJob);

				finishedJobs.put(pos.getId(), webserviceJob);

				job.publishToService();
				if (webserviceJob.isFailed()) {
					throw new RuntimeException("Job " + webserviceJob + " of Webservice " + ws + "failed, hence workflow " + workflow + "failed. :(");
				}
				else if (webserviceJob.isFinished()) {
					job.info("Job " + webserviceJob + " of Webservice " + ws + "finished successfully, moving on to next position.");
					for (ParameterAssignmentPojo ass : webserviceJob.getOutputParameterAssignments()) {
						try {
							job.addOutputParameterAssignment(ass.getLabel(), ass.getParameterValue());
						} catch (Exception e) {
							// TODO FIXME HACK BAD BAD
						}
					}
				}
				job.publishToService();

			} // end position loop

			job.setFinished();
		} catch (Throwable t) {
			log.error("Workflow " + job +  " FAILED: " + t + "\n" + ExceptionUtils.getStackTrace(t));
			job.fatal("Workflow " + job + " FAILED: " + t + "\n" + ExceptionUtils.getStackTrace(t));
			job.setFailed();
			// TODO why can't I throw this here but in AbstractTransformationService??
			// throw t
		} finally {
			
			// output one giant log containing everything for debugging
			JobPojo dummyJob = new JobPojo();
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
		}
	}
}
