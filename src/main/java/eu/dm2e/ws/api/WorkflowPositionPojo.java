package eu.dm2e.ws.api;

import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.grafeo.gom.SerializablePojo;
import eu.dm2e.utils.DotUtils;
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
	public void validate() throws Exception {
		if (null == workflow)
			throw new AssertionError(this + " has no workflow");
		if (null == webservice)
			throw new AssertionError(this + " has no webservice");
//		else
//			webservice.validate();
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
        for (ParameterPojo p : getWebservice().getInputParams()) {
            ports.add(p.getDotId());
            labels.add(DotUtils.xmlEscape(p.getLabel()));
        }
        rowLabels.add(DotUtils.getColumn(labels, ports));
        rowLabels.add(DotUtils.getColumn(DotUtils.xmlEscape(getWebservice().getLabel())));
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
        return sb.toString();
    }
}
