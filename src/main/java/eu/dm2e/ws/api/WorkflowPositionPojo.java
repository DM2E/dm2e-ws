package eu.dm2e.ws.api;

import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.grafeo.gom.SerializablePojo;
import eu.dm2e.utils.DotUtils;
import eu.dm2e.utils.UriUtils;
import eu.dm2e.ws.NS;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojo representing an instance of a webservice within a workflow
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass(NS.OMNOM.CLASS_WORKFLOW_POSITION)
public class WorkflowPositionPojo extends SerializablePojo<WorkflowPositionPojo> implements IValidatable{
	
	@Override
	public ValidationReport validate() {
        ValidationReport res = new ValidationReport(this);
		if (null == workflow)
			res.addMessage(this,1,this + " has no workflow");
		if (null == webservice)
            res.addMessage(this,2,this + " has no webservice");
        if (res.size()>0) return res;
        for (ParameterPojo param:getWebservice().getInputParams()) {
            if (param.getIsRequired()) {
                ParameterConnectorPojo conn = getWorkflow().getConnectorToPositionAndParam(this,param);
                if (conn==null) {
                    res.addMessage(this,3,this + " has no connection for required parameter " + param);
                }
            }
        }
//		else
//			webservice.validate();
        return res;
	}

    /*********************
     * CONSTRUCTORS
     ********************/
	public WorkflowPositionPojo() {
		// to make BeanUtils happy
	}
    

    
    /*********************
     * GETTERS/SETTERS
     ********************/

	/**
	 * TODO this shoudl be replace with a web service
	 *  Configuration of the workflow in this position as provided
	 */
	@RDFProperty( value = NS.OMNOM.PROP_WEBSERVICE, serializeAsURI = true)
	private WebservicePojo webservice;
	public WebservicePojo getWebservice() { return this.webservice; }
	public void setWebservice(WebservicePojo webService) { this.webservice = webService; }

	/**
	 * The workflow this position belongs to
	 */
	@RDFProperty( value = NS.OMNOM.PROP_IN_WORKFLOW, serializeAsURI = true)
	private WorkflowPojo workflow;
	public WorkflowPojo getWorkflow() { return workflow; }
	public void setWorkflow(WorkflowPojo workflow) { this.workflow = workflow; }


    public String getDotId() {
        int h = getId()!=null?getId().hashCode():hashCode();
        if (h<0) h = h*-1;
        return "" + h;

    }

    public String getDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ").append(getDotId()).append(" [");
        sb.append("label=<");
        List<String> labels = new ArrayList<>();
        List<String> ports = new ArrayList<>();
        List<String> rowLabels = new ArrayList<>();
        WorkflowPojo nestedWf = null;
        String nestedInput = null;
        for (ParameterPojo p : getWebservice().getInputParams()) {
            ports.add(p.getDotId());
            labels.add(DotUtils.xmlEscape(p.getLabel()));
            if (p.getId().endsWith("workflow")) {
                nestedWf = new WorkflowPojo();
                nestedWf.loadFromURI(p.getDefaultValue());
                nestedInput = p.getDotId();
            }
        }
        rowLabels.add(DotUtils.getColumn(labels, ports));
        if (getWebservice().getLabel() != null) {
        	rowLabels.add(DotUtils.getColumn(DotUtils.xmlEscape(getWebservice().getLabel())));
        } else {
        	rowLabels.add(DotUtils.getColumn(DotUtils.xmlEscape(UriUtils.lastUriSegment(getWebservice().getId()))));
        }
        labels.clear();
        ports.clear();
        for (ParameterPojo p : getWebservice().getOutputParams()) {
            ports.add(p.getDotId());
            labels.add(DotUtils.xmlEscape(p.getLabel()));
        }
        rowLabels.add(DotUtils.getColumn(labels, ports));
        sb.append(DotUtils.getRow(rowLabels,null,"gray90"));
        sb.append(">");
        sb.append("];\n");
        if (nestedWf!=null) {
            sb.append(nestedWf.getDot());
            sb.append(DotUtils.connect("clusterNode_"+nestedWf.getDotIdIn(),null,getDotId(),nestedInput,null));
        }

        return sb.toString();
    }
}
