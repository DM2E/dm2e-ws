package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass(NS.OMNOM.CLASS_WORKFLOW_JOB)
public class WorkflowJobPojo extends AbstractJobPojo {
	
	/*******************************
	 * FIELDS / GETTERS / SETTERS
	 *******************************/
	
    @RDFProperty(NS.OMNOM.PROP_WORKFLOW_CONFIG)
    private WorkflowConfigPojo workflowConfig;
	public WorkflowConfigPojo getWorkflowConfig() { return workflowConfig; }
	public void setWorkflowConfig(WorkflowConfigPojo workflowConfig) { this.workflowConfig = workflowConfig; }
	
    @RDFProperty(NS.OMNOM.PROP_ASSIGNMENT)
    Set<ParameterAssignmentPojo> outputParameters= new HashSet<>();
	public Set<ParameterAssignmentPojo> getOutputParameters() { return outputParameters; }
	public void setOutputParameters(Set<ParameterAssignmentPojo> outputParameters) { this.outputParameters = outputParameters; }

}
