
package eu.dm2e.ws.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({
	"omnom", "http://onto.dm2e.eu/omnom/",
	"rdfs", "http://www.w3.org/2000/01/rdf-schema#"
	})
@RDFClass(NS.OMNOM.CLASS_WORKFLOW)
public class WorkflowPojo extends AbstractPersistentPojo<WorkflowPojo> implements IWebservice, IValidatable {
	
	@Override
	public Grafeo getGrafeo() {
		Grafeo g = super.getGrafeo();
		return g;
	};
	
	
    /*********************
     * HELPER FUNCTIONS
     * @return 
     *********************/
    private ParameterPojo addParameterByName(String paramName, boolean isOutput) {
    	ParameterPojo param = new ParameterPojo();
    	if (this.hasId()) {
	    	param.setId(this.getId() + "/param/" + paramName);
    	}	
    	param.setLabel(paramName);
    	param.setWorkflow(this);
    	Set<ParameterPojo> paramSet = (isOutput) ? this.outputParams : this.inputParams;
    	paramSet.add(param);
    	return param;
    }
    /**
	 * @see eu.dm2e.ws.api.IWebservicePojo#addInputParameter(java.lang.String)
	 */
    @Override
	public ParameterPojo addInputParameter(String paramName) {
    	return this.addParameterByName(paramName, false);
    }
    /**
	 * @see eu.dm2e.ws.api.IWebservicePojo#addOutputParameter(java.lang.String)
	 */
    @Override
	public ParameterPojo addOutputParameter(String paramName) {
    	return this.addParameterByName(paramName, true);
    }
    /**
	 * @see eu.dm2e.ws.api.IWebservicePojo#getParamByName(java.lang.String)
	 */
    @Override
	public ParameterPojo getParamByName(String needle) {
    	Set<ParameterPojo> allParams = new HashSet<>();
    	allParams.addAll(inputParams);
    	allParams.addAll(outputParams);
    	for (ParameterPojo param : allParams) {
    		if (param.matchesParameterName(needle)) {
    			return param;
    		}
    	}
        log.warning("No parameter found for needle: " + needle);
    	return null;
    }
    public ParameterConnectorPojo getConnectorToWorkflowOutputParam(ParameterPojo param) {
    	return getConnectorToWorkflowOutputParam(param.getId());
    }
    public ParameterConnectorPojo getConnectorToWorkflowOutputParam(String needle) {
    	ParameterConnectorPojo retConn = null;
    	for (ParameterConnectorPojo conn : this.getParameterConnectors()) {
    		if (! conn.hasToWorkflow())
    			continue;
	    	log.fine("Checking " + conn.getToWorkflow() + ":" + conn.getToParam() + " in " + conn);
    		if (conn.getToParam().hasId()
					&&
				conn.getToWorkflow().getId().equals(this.getId())
					&&
				conn.getToParam().matchesParameterName(needle)) {
    			retConn = conn;
    			break;
    		}
    	}
    	return retConn;
    }
    
    public ParameterConnectorPojo getConnectorToPositionAndParam(WorkflowPositionPojo pos, ParameterPojo param) {
    	return getConnectorToPositionAndParam(pos, param.getId());
    }
    public ParameterConnectorPojo getConnectorToPositionAndParam(WorkflowPositionPojo pos, String needle) {
    	for (ParameterConnectorPojo conn : this.getParameterConnectors()) {
    		if (
    				null != needle
    			&&
    				conn.hasToPosition()
				&&
    				conn.getToPosition().hasId()
				&&
    				conn.getToParam().hasId()
				&&
					conn.getToPosition().getId().equals(pos.getId())
				&&
	    			conn.getToParam().matchesParameterName(needle)) {
    			return conn;
    		}
    	}
    	log.warning("No connector for position " + pos + " and param " + needle);
    	return null;
    }
    
    
    /**
     * Add a connector from an input parameter of this workflow to a position/parameter pair
     * 
     * @param fromParamName 
     * @param toPos
     * @param toParamName
     * @return
     */
    public ParameterConnectorPojo addConnectorFromWorkflowToPosition(String fromParamName, WorkflowPositionPojo toPos, String toParamName) {
    	ParameterConnectorPojo conn = new ParameterConnectorPojo();
    	conn.setInWorkflow(this);
    	conn.setFromWorkflow(this);
    	conn.setFromParam(getParamByName(fromParamName));
    	conn.setToPosition(toPos);
    	conn.setToParam(toPos.getWebserviceConfig().getWebservice().getParamByName(toParamName));
    	conn.validate();
    	this.getParameterConnectors().add(conn);
    	return conn;
    }
    
    /**
     * Add a connector from a Position/Param pair to an output parameter of the workflow
     * 
     * @param fromPos
     * @param fromParamName
     * @param toParamName
     * @return
     */
    public ParameterConnectorPojo addConnectorFromPositionToWorkflow(WorkflowPositionPojo fromPos, String fromParamName, String toParamName) {
    	ParameterConnectorPojo conn = new ParameterConnectorPojo();
    	conn.setInWorkflow(this);
    	conn.setFromPosition(fromPos);
    	conn.setFromParam(fromPos.getWebserviceConfig().getWebservice().getParamByName(fromParamName));
    	conn.setToWorkflow(this);
    	conn.setToParam(getParamByName(toParamName));
    	conn.validate();
    	this.getParameterConnectors().add(conn);
    	return conn;
    }
    /**
     * Add a connection from a position/param pair to another position/param pair
     * @param fromPos
     * @param fromParamName
     * @param toPos
     * @param toParamName
     * @return
     */
    public ParameterConnectorPojo addConnectorFromPositionToPosition(WorkflowPositionPojo fromPos, String fromParamName, WorkflowPositionPojo toPos, String toParamName) {
    	ParameterConnectorPojo conn = new ParameterConnectorPojo();
    	conn.setInWorkflow(this);
    	conn.setFromPosition(fromPos);
    	conn.setFromParam(fromPos.getWebserviceConfig().getWebservice().getParamByName(fromParamName));
    	conn.setToPosition(toPos);
    	conn.setToParam(toPos.getWebserviceConfig().getWebservice().getParamByName(toParamName));
    	conn.validate();
    	this.getParameterConnectors().add(conn);
    	return conn;
    	
    }
    
	/**
	 * Things to make sure:
	 * * list is not empty
	 * * webservice configs contain a priori assignments (which they should not when run in a workflow)
	 * TODO
	 * 
	 * @see eu.dm2e.ws.api.IValidatable#validate()
	 */
	@Override
	public void validate() throws Exception {
		if (this.getPositions().isEmpty()) {
			throw new RuntimeException("No positions in the workflow.");
		}
	}
    
    
    /*********************
     * GETTERS/SETTERS
     ********************/

    @RDFProperty(NS.OMNOM.PROP_INPUT_PARAM)
    private Set<ParameterPojo> inputParams = new HashSet<>();
	@Override public Set<ParameterPojo> getInputParams() { return inputParams; }
	@Override public void setInputParams(Set<ParameterPojo> inputParams) { this.inputParams = inputParams; }
	
    @RDFProperty(NS.OMNOM.PROP_OUTPUT_PARAM)
    private Set<ParameterPojo> outputParams = new HashSet<>();
	@Override public Set<ParameterPojo> getOutputParams() { return outputParams; }
	@Override public void setOutputParams(Set<ParameterPojo> outputParams) { this.outputParams = outputParams; }
    
    @RDFProperty(NS.OMNOM.PROP_PARAMETER_CONNECTOR)
    private Set<ParameterConnectorPojo> parameterConnectors = new HashSet<>();
	public Set<ParameterConnectorPojo> getParameterConnectors() { return parameterConnectors; }
	public void setParameterConnectors(Set<ParameterConnectorPojo> parameterConnectors) { this.parameterConnectors = parameterConnectors; }

    @RDFProperty(value = NS.OMNOM.PROP_HAS_POSITION, itemPrefix="position-item" )
    private List<WorkflowPositionPojo> positions = new ArrayList<>();
	public List<WorkflowPositionPojo> getPositions() { return positions; }
	public void setPositions(List<WorkflowPositionPojo> positions) { this.positions = positions; }
	public void addPosition(WorkflowPositionPojo it) { this.getPositions().add(it); }
    

}
