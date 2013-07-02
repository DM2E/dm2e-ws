package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

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
	
	@RDFProperty(value = NS.OMNOM.FINISHED_POSITION, serializeAsURI=true)
	private Set<WorkflowPositionPojo> finishedPositions = new HashSet<>();
	public Set<WorkflowPositionPojo> getFinishedPositions() { return finishedPositions; }
	public void setFinishedPositions(Set<WorkflowPositionPojo> finishedPositions) { this.finishedPositions = finishedPositions; }
	
	@RDFProperty(value = NS.OMNOM.FINISHED_JOB, serializeAsURI=true)
	private Set<JobPojo> finishedJobs = new HashSet<>();
	public Set<JobPojo> getFinishedJobs() { return finishedJobs; }
	public void setFinishedJobs(Set<JobPojo> finishedJobs) { this.finishedJobs = finishedJobs; }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result + ((finishedJobs == null) ? 0 : finishedJobs.hashCode());
		result = prime * result + ((finishedPositions == null) ? 0 : finishedPositions.hashCode());
		result = prime * result + ((workflow == null) ? 0 : workflow.hashCode());
		result = prime * result + ((workflowConfig == null) ? 0 : workflowConfig.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof WorkflowJobPojo)) return false;
		WorkflowJobPojo other = (WorkflowJobPojo) obj;
		if (finishedJobs == null) {
			if (other.finishedJobs != null) return false;
		} else if (!finishedJobs.equals(other.finishedJobs)) return false;
		if (finishedPositions == null) {
			if (other.finishedPositions != null) return false;
		} else if (!finishedPositions.equals(other.finishedPositions)) return false;
		if (workflow == null) {
			if (other.workflow != null) return false;
		} else if (!workflow.equals(other.workflow)) return false;
		if (workflowConfig == null) {
			if (other.workflowConfig != null) return false;
		} else if (!workflowConfig.equals(other.workflowConfig)) return false;
		return true;
	}
}
