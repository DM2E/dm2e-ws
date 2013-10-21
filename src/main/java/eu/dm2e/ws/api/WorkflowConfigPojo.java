package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.services.workflow.WorkflowService;

/**
 * Pojo for a workflow configuration
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass(NS.OMNOM.CLASS_WORKFLOW_CONFIG)
public class WorkflowConfigPojo extends AbstractConfigPojo<WorkflowConfigPojo> {
	
	@Override
	public ParameterPojo getParamByName(String needle) {
		return this.getWorkflow().getParamByName(needle);
	}
    
	
	/**
	 * Validate a workflow config against a workflow.
	 * 
	 * Things to make sure:
	 * a) Every input parameter of the workflow must be covered by an assignment of the workflowconfig
	 * b) All Connecotrs from/to Workflows must reference existing parameters of the workflow
	 * c) Every required inputParameter of every position is going to be filled somehow
	 * d) Every output parameter of the workflow is going to be filled somehow
	 * 
	 * @see eu.dm2e.ws.api.IValidatable#validate()
	 */
	@Override
	public void validate() {
		WorkflowPojo workflow = this.getWorkflow();
		log.info("Validating " + this + " with " + workflow);
		log.info("Input params: " + workflow.getInputParams());
		log.info("Assignments: " + this.getParameterAssignments());
		
		//
		// a)
		//
		for (ParameterPojo param : workflow.getInputParams()) {
			log.info("" + param);
			ParameterAssignmentPojo ass = this.getParameterAssignmentForParam(param.getId());
			if (param.getIsRequired()) {
				if (null == ass) {
					throw new RuntimeException(param + " is not set by " + this); 
				}
			}
			
			if (null != ass && null == ass.getParameterValue()) {
				throw new RuntimeException(param + " has no value in " + this); 
			}
		}
		
		//
		// c)
		//
		// for every position
		for (WorkflowPositionPojo pos : workflow.getPositions()) {
//			wsconf = pos.getWebservice();
			WebservicePojo ws = pos.getWebservice();
			if (null == ws) {
				throw new AssertionError(pos + " has no webService.");
			}
			// for every input parameter of the webservice at this position
			for (ParameterPojo param : ws.getInputParams()) {
				if (param.getIsRequired()
						&&
					null == workflow.getConnectorToPositionAndParam(pos, param)) {
					throw new RuntimeException(param + " of " + ws + " in " + pos + "is not connected in the workflow " + workflow + ".");
				}
					
			}
		}
		
		//
		// d)
		//
		for (ParameterPojo param : workflow.getOutputParams()) {
			if (param.matchesParameterName(WorkflowService.PARAM_COMPLETE_LOG))
				continue;
			if (null == workflow.getConnectorToWorkflowOutputParam(param)) {
				throw new RuntimeException("No connector to output parameter " + param + "of workflow " + workflow);
			}
		}
	}
	
    /*********************
     * GETTERS/SETTERS
     ********************/
	
    @RDFProperty(value = NS.OMNOM.PROP_WORKFLOW, serializeAsURI = true )
    private WorkflowPojo workflow;
	public WorkflowPojo getWorkflow() { return workflow; }
	public void setWorkflow(WorkflowPojo wf) { this.workflow = wf; }


//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = super.hashCode();
//		result = prime * result + ((workflow == null) ? 0 : workflow.hashCode());
//		return result;
//	}
//
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) return true;
//		if (!super.equals(obj)) return false;
//		if (!(obj instanceof WorkflowConfigPojo)) return false;
//		WorkflowConfigPojo other = (WorkflowConfigPojo) obj;
//		if (workflow == null) {
//			if (other.workflow != null) return false;
//		} else if (!workflow.equals(other.workflow)) return false;
//		return true;
//	}


}
