package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass(NS.OMNOM.CLASS_WORKFLOW_CONFIG)
public class WorkflowConfigPojo extends AbstractPersistentPojo<WorkflowConfigPojo> implements IValidatable {
	
	public ParameterAssignmentPojo getParameterAssignmentForParam(String paramName) {
		log.info("Access to param assignment by name: " + paramName);
        for (ParameterAssignmentPojo ass : this.getParameterAssignments()) {
        	if (ass.getForParam().matchesParameterName(paramName)) {
				log.info("GOTCHA : " + ass.getParameterValue()); 
                return ass;
			}
		}
		return null;
	}
	
	public void addParameterAssignment(String paramName, String paramValue) {
		ParameterAssignmentPojo ass = new ParameterAssignmentPojo();
		ParameterPojo param = this.getWorkflow().getParamByName(paramName);
		ass.setLabel(paramName);
		ass.setForParam(param);
		ass.setParameterValue(paramValue);
		this.getParameterAssignments().add(ass);
	}
    
    // TODO this is not perfect, there could be several slots for the same position and the same parameter
//    public ParameterConnectorPojo getSlotForPositionIndexAndParam(int index, ParameterPojo param) {
//    	WorkflowPositionPojo pos = this.getWorkflowPojo().get(index);
//    	if (null != pos) {
////    		for (ParameterSlotPojo thisSlot : parameterSlots) {
//////		    	log.info("" + thisSlot.getForPosition().getWebService());
//////		    	log.info(""+ pos.g);
////    			if (thisSlot.getForPosition() != pos) {
////    				continue;
////    			}
////		    	log.info("" + thisSlot.getToParam().getId());
////    			if (thisSlot.getToParam() != null
////    					&& thisSlot.getToParam().getId().equals(param.getId())
////    					&& thisSlot.getForPosition() == pos) {
////    				return thisSlot;
////    			}
////    		}
//    	}
//    	return null;
//    }
	
	/**
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
		WorkflowPojo wf = this.getWorkflow();
		log.info("Validating " + this + " with " + wf);
		log.info("Input params" + wf.getInputParams());
		
		//
		// a)
		//
		for (ParameterPojo param : wf.getInputParams()) {
			ParameterAssignmentPojo ass = this.getParameterAssignmentForParam(param.getId());
			if (null == ass) {
				throw new RuntimeException(param + " is not set by " + this); 
			}
		}
		
		//
		// b)
		//
		for (ParameterConnectorPojo conn : wf.getParameterConnectors()) {
			if (conn.hasFromWorkflow() 
					&&
				null == wf.getParamByName(conn.getFromParam().getId())) {
				throw new AssertionError(conn + " references parameter " + conn.getToParam() + " which is not defined by " + wf);
			}
			if (conn.hasToWorkflow() 
					&&
				null == wf.getParamByName(conn.getToParam().getId())) {
				throw new AssertionError(conn + " references parameter " + conn.getToParam() + " which is not defined by " + wf);
			}
		}
		
		//
		// c)
		//
		// for every position
		for (WorkflowPositionPojo pos : wf.getPositions()) {
			WebserviceConfigPojo wsconf = pos.getWebserviceConfig();
			if (null == wsconf) {
				throw new AssertionError(pos + " has no WebServiceConfig");
			}
			WebservicePojo ws = wsconf.getWebservice();
			if (null == ws) {
				throw new AssertionError(wsconf + " of " + pos + " has no webService.");
			}
			// for every input parameter of the webservice at this position
			for (ParameterPojo param : ws.getInputParams()) {
				if (null != wsconf.getParameterAssignmentForParam(param.getId())) {
					throw new AssertionError(param + " is covered by the " + wsconf +". This belongs to the Workflow however.");
				}
				if (param.getIsRequired()
						&&
					null == wf.getConnectorToPositionAndParam(pos, param)) {
					throw new RuntimeException(param + " of " + ws + " in " + pos + "is not connected in the workflow " + wf + ".");
				}
					
			}
		}
		
		//
		// d)
		//
		for (ParameterPojo param : wf.getOutputParams()) {
			if (null == wf.getConnectorToWorkflowOutputParam(param)) {
				throw new RuntimeException("No connector to output parameter " + param + "of workflow " + wf);
			}
		}
	}
	
    /*********************
     * GETTERS/SETTERS
     ********************/
	
    @RDFProperty(value = NS.OMNOM.PROP_WORKFLOW )
    private WorkflowPojo workflow;
	public WorkflowPojo getWorkflow() { return workflow; }
	public void setWorkflow(WorkflowPojo wf) { this.workflow = wf; }
	
	@RDFProperty(NS.OMNOM.PROP_ASSIGNMENT)
	private Set<ParameterAssignmentPojo> parameterAssignments = new HashSet<>();
	public Set<ParameterAssignmentPojo> getParameterAssignments() { return parameterAssignments; }
	public void setParameterAssignments(Set<ParameterAssignmentPojo> parameterAssignments) { this.parameterAssignments = parameterAssignments; }


}
