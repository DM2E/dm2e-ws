package eu.dm2e.ws.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({
	"omnom", "http://onto.dm2e.eu/omnom/",
	"rdfs", "http://www.w3.org/2000/01/rdf-schema#"
	})
@RDFClass(NS.OMNOM.CLASS_WORKFLOW)
public class WorkflowPojo extends AbstractPersistentPojo<WorkflowPojo> implements IWebservice, IValidatable {
	
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
    public ParameterConnectorPojo connectorToPositionAndParam(WorkflowPositionPojo pos, ParameterPojo needle) {
    	for (ParameterConnectorPojo conn : this.getParameterConnectors()) {
    		if (
    				conn.getToPosition().hasId()
				&&
    				conn.getToParam().hasId()
				&&
    				conn.getToPosition().getId().equals(pos.getId())
				&&
    				conn.getToParam().getId().equals(needle)) {
    			return conn;
    		}
    	}
    	return null;
    }
    
    public ParameterConnectorPojo connectorToPositionAndParam(WorkflowPositionPojo pos, String needle) {
    	for (ParameterConnectorPojo conn : this.getParameterConnectors()) {
    		if (
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
    	return null;
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
    
    @RDFProperty(value = NS.OMNOM.PROP_PARAMETER_CONNECTOR, itemPrefix = "connector")
    private Set<ParameterConnectorPojo> parameterConnectors = new HashSet<>();
	public Set<ParameterConnectorPojo> getParameterConnectors() { return parameterConnectors; }
	public void setParameterConnectors(Set<ParameterConnectorPojo> parameterConnectors) { this.parameterConnectors = parameterConnectors; }

    @RDFProperty(value = NS.OMNOM.PROP_HAS_POSITION, itemPrefix="position-item" )
    private List<WorkflowPositionPojo> positions = new ArrayList<>();
	public List<WorkflowPositionPojo> getPositions() { return positions; }
	public void setPositions(List<WorkflowPositionPojo> positions) { this.positions = positions; }
	public void addPosition(WorkflowPositionPojo it) { this.getPositions().add(it); }
    

}
