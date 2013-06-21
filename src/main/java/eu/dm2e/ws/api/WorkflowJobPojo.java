package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass(NS.OMNOM.CLASS_WORKFLOW_JOB)
public class WorkflowJobPojo extends AbstractJobPojo {
	
	
	/*******************************
	 * FIELDS / GETTERS / SETTERS
	 *******************************/
	
//	@RDFProperty(NS.OMNOM.PROP_CONNECTOR_ASSIGNMENT)
//	private Set<ParameterConnectorAssignmentPojo> connectorAssignments = new HashSet<>();
//	public Set<ParameterConnectorAssignmentPojo> getConnectorAssignments() { return connectorAssignments; }
//	public void setConnectorAssignments(Set<ParameterConnectorAssignmentPojo> connectorAssignments) { this.connectorAssignments = connectorAssignments; }
	
    @RDFProperty(NS.OMNOM.PROP_WORKFLOW_CONFIG)
    private WorkflowConfigPojo workflowConfig;
	public WorkflowConfigPojo getWorkflowConfig() { return workflowConfig; }
	public void setWorkflowConfig(WorkflowConfigPojo workflowConfig) { this.workflowConfig = workflowConfig; }

}
