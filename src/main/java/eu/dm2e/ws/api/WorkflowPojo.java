
package eu.dm2e.ws.api;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.utils.DotUtils;
import eu.dm2e.ws.NS;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pojo representing a workflow
 */
@Namespaces({
	"omnom", "http://onto.dm2e.eu/omnom/",
	"rdfs", "http://www.w3.org/2000/01/rdf-schema#"
	})
@RDFClass(NS.OMNOM.CLASS_WORKFLOW)
public class WorkflowPojo extends AbstractPersistentPojo<WorkflowPojo> implements IValidatable {
	
	@Override
	public int getMaximumJsonDepth() { return 3; }
	
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

    public ParameterPojo addInputParameter(String paramName) {
    	return this.addParameterByName(paramName, false);
    }

    public ParameterPojo addOutputParameter(String paramName) {
    	return this.addParameterByName(paramName, true);
    }

    public ParameterPojo getParamByName(String needle) {
    	Set<ParameterPojo> allParams = new HashSet<>();
    	allParams.addAll(inputParams);
    	allParams.addAll(outputParams);
    	for (ParameterPojo param : allParams) {
    		if (param.matchesParameterName(needle)) {
    			return param;
    		}
    	}
        log.warn("No parameter found for needle: " + needle);
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
	    	log.debug("Checking " + conn.getToWorkflow() + ":" + conn.getToParam() + " in " + conn);
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

    public Set<ParameterConnectorPojo> getConnectorFromWorkflowInputParam(ParameterPojo param) {
        return getConnectorFromWorkflowInputParam(param.getId());
    }
    public Set<ParameterConnectorPojo> getConnectorFromWorkflowInputParam(String needle) {
        Set<ParameterConnectorPojo> retConn = new HashSet<>();
        for (ParameterConnectorPojo conn : this.getParameterConnectors()) {
            if (! conn.hasFromWorkflow())
                continue;
            log.debug("Checking " + conn.getFromWorkflow() + ":" + conn.getFromParam() + " in " + conn);
            if (conn.getFromParam().hasId()
                    &&
                    conn.getFromWorkflow().getId().equals(this.getId())
                    &&
                    conn.getFromParam().matchesParameterName(needle)) {
                retConn.add(conn);
                // break;
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
				&&  ((
    				        conn.getToPosition().hasId()
                            &&
                            conn.getToPosition().getId().equals(pos.getId())
                            ) || (
                            conn.getToPosition().equals(pos) // Added, as otherwise a connection can only
                            // be detected after publishing.
                ))
                &&
    				conn.getToParam().hasId()
				&&
	    			conn.getToParam().matchesParameterName(needle)) {
    			return conn;
    		}


        }
    	log.debug("No connector for position " + pos + " and param " + needle);
    	return null;
    }

    public Set<ParameterConnectorPojo> getConnectorFromPositionAndParam(WorkflowPositionPojo pos, ParameterPojo param) {
        return getConnectorFromPositionAndParam(pos, param.getId());
    }
    public Set<ParameterConnectorPojo> getConnectorFromPositionAndParam(WorkflowPositionPojo pos, String needle) {
        Set<ParameterConnectorPojo> retConn = new HashSet<>();
        for (ParameterConnectorPojo conn : this.getParameterConnectors()) {
            if (
                    null != needle
                            &&
                            conn.hasFromPosition()
                            &&  ((
                            conn.getFromPosition().hasId()
                                    &&
                                    conn.getFromPosition().getId().equals(pos.getId())
                    ) || (
                            conn.getFromPosition().equals(pos) // Added, as otherwise a connection can only
                            // be detected after publishing.
                    ))
                            &&
                            conn.getFromParam().hasId()
                            &&
                            conn.getFromParam().matchesParameterName(needle)) {
                retConn.add(conn);
            }


        }
        return retConn;
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
    	conn.setToParam(toPos.getWebservice().getParamByName(toParamName));
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
    	conn.setFromParam(fromPos.getWebservice().getParamByName(fromParamName));
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
    	conn.setFromParam(fromPos.getWebservice().getParamByName(fromParamName));
    	conn.setToPosition(toPos);
    	conn.setToParam(toPos.getWebservice().getParamByName(toParamName));
    	conn.validate();
    	this.getParameterConnectors().add(conn);
    	return conn;
    	
    }

    public WorkflowPositionPojo addPosition(WebservicePojo ws) {
        return addPosition("", ws);
    }

    public WorkflowPositionPojo addPosition(String label, WebservicePojo ws) {
        WorkflowPositionPojo pos = new WorkflowPositionPojo();
        pos.setWebservice(ws);
        pos.setWorkflow(this);
        pos.setLabel(label + "/" + ws.getLabel());
        addPosition(pos);
        return pos;
    }




    /**
	 * Things to make sure:
	 * * list is not empty
	 * * webservice configs contain a priori assignments (which they should not when run in a workflow)
	 * TODO
	 * 
	 *
	 * @see eu.dm2e.ws.api.IValidatable#validate()
	 */
	@Override
	public ValidationReport validate() {
        ValidationReport res = new ValidationReport(this);

//		if (this.getPositions().isEmpty()) {
//			throw new RuntimeException("No positions in the workflow.");
//		}
		//
		// b)
		//
		for (ParameterConnectorPojo conn : this.getParameterConnectors()) {
			if (conn.hasFromWorkflow() 
					&&
				null == this.getParamByName(conn.getFromParam().getLabel())) {
                res.add(new ValidationMessage(this,1,conn + " references parameter " + conn.getToParam() + " which is not defined by " + this));
			}
			if (conn.hasToWorkflow() 
					&&
				null == this.getParamByName(conn.getToParam().getLabel())) {
                res.add(new ValidationMessage(this,2,conn + " references parameter " + conn.getToParam() + " which is not defined by " + this));
			}
		}
		
		for (WorkflowPositionPojo pos : this.getPositions()) {
			res.addAll(pos.validate());
		}
        return res;
	}
    
    
    /*********************
     * GETTERS/SETTERS
     ********************/

    @RDFProperty(value = NS.OMNOM.PROP_INPUT_PARAM, serializeAsURI = false)
    private Set<ParameterPojo> inputParams = new HashSet<>();
	public Set<ParameterPojo> getInputParams() { return inputParams; }
	public void setInputParams(Set<ParameterPojo> inputParams) { this.inputParams = inputParams; }
	
    @RDFProperty(value = NS.OMNOM.PROP_OUTPUT_PARAM, serializeAsURI = false)
    private Set<ParameterPojo> outputParams = new HashSet<>();
	public Set<ParameterPojo> getOutputParams() { return outputParams; }
	public void setOutputParams(Set<ParameterPojo> outputParams) { this.outputParams = outputParams; }
    
    @RDFProperty(value = NS.OMNOM.PROP_PARAMETER_CONNECTOR, serializeAsURI = false)
    private Set<ParameterConnectorPojo> parameterConnectors = new HashSet<>();
	public Set<ParameterConnectorPojo> getParameterConnectors() { return parameterConnectors; }
	public void setParameterConnectors(Set<ParameterConnectorPojo> parameterConnectors) { this.parameterConnectors = parameterConnectors; }

	@RDFProperty(value = NS.OMNOM.PROP_WORKFLOW_POSITION, serializeAsURI = false, itemPrefix = "position-item")
    private List<WorkflowPositionPojo> positions = new ArrayList<>();
	public List<WorkflowPositionPojo> getPositions() { return positions; }
	public void setPositions(List<WorkflowPositionPojo> positions) { this.positions = positions; }
	public void addPosition(WorkflowPositionPojo it) { this.getPositions().add(it); }

	@RDFProperty(NS.DCTERMS.PROP_CREATOR)
	private UserPojo creator;
	public UserPojo getCreator() { return creator; }
	public void setCreator(UserPojo creator) { this.creator = creator; }
	
	@RDFProperty(NS.DCTERMS.PROP_MODIFIED)
	private DateTime modified = DateTime.now();
	public DateTime getModified() { return modified; }
	public void setModified(DateTime modified) { this.modified = modified; }

    @RDFProperty(value = NS.OMNOM.PROP_EXEC_WEBSERVICE, serializeAsURI = true)
    private Set<WebservicePojo> webservices = new HashSet<>();
    public Set<WebservicePojo> getWebservices() { return webservices; }
    public void setWebservices(Set<WebservicePojo> webservices) { this.webservices = webservices; }
    public void addWebservice(WebservicePojo it) { this.getWebservices().add(it); }
    public WebservicePojo getWebservice() {
        for (WebservicePojo ws:webservices) {
            return ws.refresh(0,true);
        }
        return null;
    }



    /**
     * Creates and connects all unconnected
     * webservice params to the workflow
      */
    public void autowire(boolean requiredOnly) {
       for (WorkflowPositionPojo pos: getPositions()) {
           if (pos.getWebservice()==null) continue;
           log.debug("Checking pos " + pos.getLabelorURI() + " for autowiring...");
           for (ParameterPojo param:pos.getWebservice().getInputParams()) {
               log.debug("   Checking param " + param.getLabelorURI() + " for autowiring...");
               if (requiredOnly && !param.getIsRequired()) {
                   log.debug("   Parameter not required, skipping.");
                   continue;
               }
               if (getConnectorToPositionAndParam(pos, param)==null) {
                    log.debug("   Autowiring parameter " + param.getNeedle());
                    ParameterPojo wp = addInputParameter(param.getNeedle());
                    wp.setIsRequired(param.getIsRequired());
                    wp.setDefaultValue(param.getDefaultValue());
                    wp.setComment(param.getComment());
                    wp.setParameterType(param.getParameterType());
                    wp.setHasIterations(param.getHasIterations());
                    addConnectorFromWorkflowToPosition(wp.getLabelorURI(),pos,param.getId());
                 }
           }
           for (ParameterPojo param:pos.getWebservice().getOutputParams()) {
               log.debug("   Checking param " + param.getLabelorURI() + " for autowiring...");
               if (getConnectorFromPositionAndParam(pos, param).isEmpty()) {
                   log.debug("   Autowiring parameter " + param.getNeedle());
                   ParameterPojo wp = addOutputParameter(param.getNeedle());
                   log.debug("WP: " + wp.getId());
                   log.debug("Param: " + param.getId());
                   wp.setIsRequired(param.getIsRequired());
                   wp.setDefaultValue(param.getDefaultValue());
                   wp.setComment(param.getComment());
                   wp.setParameterType(param.getParameterType());
                   wp.setHasIterations(param.getHasIterations());
                   log.debug("Iterating parameter? " + param.getHasIterations() + " Workflow param: " + wp.getHasIterations());
                   addConnectorFromPositionToWorkflow(pos,param.getId(),wp.getLabelorURI());
               }
           }
       }
       log.debug("Autowiring finished.");
    }

    public void autowire() {
      autowire(false);
    }

    public String getDotIdIn() {
        int h = getId()!=null?getId().hashCode():hashCode();
        if (h<0) h = h*-1;
        return "" + h + "1";
    }
    public String getDotIdOut() {
        int h = getId()!=null?getId().hashCode():hashCode();
        if (h<0) h = h*-1;
        return "" + h + "2";
    }

    private transient String[] colors = {"black", "blue3", "brown2", "burlywood2", "cadetblue2", "chartreuse2", "chocolate2", "cyan4", "darkorange", "dodgerblue4", "darkslategray4", "firebrick4"};
    public String getDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("subgraph cluster_").append(getDotIdIn()).append(" {\n");
        sb.append("clusterNode_").append(getDotIdIn()).append(" [label=\"\", fixedsize=\"false\", width=0, height=0, shape=none];\n");
        sb.append("color=blue;\n");
        sb.append("style=solid;\n");
        String label = getLabelorURI();
        if (label!=null) label.replaceAll("\"","\\\"");
        else label="NO LABEL OR URI SET";
        sb.append("label=\"WORKFLOW: ").append(label).append("\";\n");
        sb.append("   ").append("node [shape=none];\n");
        sb.append("   ").append("rankdir=LR;\n");
        sb.append(getWorkflowPositionDot());
        for (WorkflowPositionPojo pos:getPositions()) {
            sb.append(pos.getDot());
        }
        int color=0;
        for (ParameterConnectorPojo con:getParameterConnectors()) {
            sb.append(con.getDot(colors[color]));
            if (++color==colors.length) color=0;
        }
        sb.append("}\n");
        return sb.toString();

    }

    public String getFullDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append("G" + getDotIdIn()).append(" {\n");
        sb.append("   ").append("rankdir=LR;\n");
        sb.append(getDot());
        sb.append("}\n");
        return sb.toString();

    }

    private String getWorkflowPositionDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ").append(getDotIdIn()).append(" [");
        sb.append("label=<");

        List<String> labels = new ArrayList<>();
        List<String> ports = new ArrayList<>();
        List<String> rowLabels = new ArrayList<>();
        // rowLabels.add(DotUtils.getColumn("WORKFLOW"));
        for (ParameterPojo p : getInputParams()) {
            ports.add(p.getDotId());
            labels.add(DotUtils.xmlEscape(p.getLabel()));
        }
        rowLabels.add(DotUtils.getColumn(labels, ports));
        sb.append(DotUtils.getRow(rowLabels,null,"gray90"));

        sb.append(">");
        sb.append("];\n");

        sb.append("   ").append(getDotIdOut()).append(" [");
        sb.append("label=<");
        labels.clear();
        ports.clear();
        rowLabels.clear();
        for (ParameterPojo p : getOutputParams()) {
            ports.add(p.getDotId());
            labels.add(DotUtils.xmlEscape(p.getLabel()));
        }
        rowLabels.add(DotUtils.getColumn(labels, ports));
        // rowLabels.add(DotUtils.getColumn("WORKFLOW"));
        sb.append(DotUtils.getRow(rowLabels,null,"gray90"));
        sb.append(">");
        sb.append("];\n");

        return sb.toString();
    }
}
