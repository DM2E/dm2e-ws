package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.NS;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;

/**
 * Pojo for a workflow job.
 */
@RDFClass(NS.OMNOM.CLASS_WORKFLOW_JOB)
public class WorkflowJobPojo extends AbstractJobPojo {
	
	@Override
	public ParameterPojo getOutputParamByName(String paramName) {
		if (null != this.getWorkflow())
			return this.getWorkflow().getParamByName(paramName);
		return null;
	}
	@Override
	public ParameterPojo getInputParamByName(String needle) {
		return this.getOutputParamByName(needle);
	}
	
	@Override
	public Set<ParameterAssignmentPojo> getInputParameterAssignments() {
		return this.getWorkflowConfig().getParameterAssignments();
	}
	
	/*******************************
	 * FIELDS / GETTERS / SETTERS
	 *******************************/

    @RDFProperty(value = NS.OMNOM.PROP_WORKFLOW, serializeAsURI = true)
    private WorkflowPojo workflow;
	public WorkflowPojo getWorkflow() { return workflow; }
	public void setWorkflow(WorkflowPojo webService) { this.workflow = webService; }
	
    @RDFProperty(value = NS.OMNOM.PROP_WORKFLOW_CONFIG, serializeAsURI = true)
    private WorkflowConfigPojo workflowConfig;
	public WorkflowConfigPojo getWorkflowConfig() { return workflowConfig; }
	public void setWorkflowConfig(WorkflowConfigPojo workflowConfig) { this.workflowConfig = workflowConfig; }
	
	@RDFProperty(value = NS.OMNOM.PROP_FINISHED_POSITION, serializeAsURI=true)
	private Set<WorkflowPositionPojo> finishedPositions = new HashSet<>();
	public Set<WorkflowPositionPojo> getFinishedPositions() { return finishedPositions; }
	public void setFinishedPositions(Set<WorkflowPositionPojo> finishedPositions) { this.finishedPositions = finishedPositions; }
	
	@RDFProperty(value = NS.OMNOM.PROP_FINISHED_JOB, serializeAsURI=true)
	private Set<JobPojo> finishedJobs = new HashSet<>();
	public Set<JobPojo> getFinishedJobs() { return finishedJobs; }
	public void setFinishedJobs(Set<JobPojo> finishedJobs) { this.finishedJobs = finishedJobs; }
	
	@RDFProperty(value = NS.OMNOM.PROP_RUNNING_JOB, serializeAsURI=true)
	private Set<JobPojo> runningJobs = new HashSet<>();
	public Set<JobPojo> getRunningJobs() { return runningJobs; }
	public void setRunningJobs(Set<JobPojo> runningJobs) { this.runningJobs = runningJobs; }
}
