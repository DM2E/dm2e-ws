package eu.dm2e.ws.api;

import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.grafeo.gom.SerializablePojo;
import eu.dm2e.utils.DotUtils;
import eu.dm2e.ws.NS;


/** 
 * Pojo for a connector between two parameters.
 * <p>
 * Every ParameterConnectorPojo *MUST* have set:<ul>
 * <li>inWorkflow
 * <li>fromParam
 * <li>toParam
 * </ul>
 * Every ParameterConnectorPojo *MUST* have set exactly one of the following combinations:<ul>
 * <li>fromWorkflow and toPosition
 * <li>fromPosition and toPosition
 * <li>fromPosition and toWorkflow
 * </p>
 */
@Namespaces({"omnom", NS.OMNOM.BASE})
@RDFClass(NS.OMNOM.CLASS_PARAMETER_CONNECTOR)
public class ParameterConnectorPojo extends SerializablePojo<ParameterConnectorPojo> implements IValidatable{
	
	/******************
	 * GETTERS/SETTERS
	 *****************/
	
	@RDFProperty( value=NS.OMNOM.PROP_IN_WORKFLOW, serializeAsURI = true )
	private WorkflowPojo inWorkflow;
	public WorkflowPojo getInWorkflow() { return inWorkflow; }
	public void setInWorkflow(WorkflowPojo wf) { this.inWorkflow = wf; }
	public boolean hasInWorkflow() { return this.inWorkflow != null; }
	
	@RDFProperty( value=NS.OMNOM.PROP_FROM_WORKFLOW, serializeAsURI = true )
	private WorkflowPojo fromWorkflow;
	public WorkflowPojo getFromWorkflow() { return fromWorkflow; }
	public void setFromWorkflow(WorkflowPojo wf) { this.fromWorkflow = wf; }
	public boolean hasFromWorkflow() { return this.fromWorkflow != null; }
	
	@RDFProperty( value=NS.OMNOM.PROP_TO_WORKFLOW, serializeAsURI = true )
	private WorkflowPojo toWorkflow;
	public WorkflowPojo getToWorkflow() { return toWorkflow; }
	public void setToWorkflow(WorkflowPojo wf) { this.toWorkflow = wf; }
	public boolean hasToWorkflow() { return this.toWorkflow != null; }
	
	@RDFProperty( value=NS.OMNOM.PROP_FROM_POSITION, serializeAsURI = true )
	private WorkflowPositionPojo fromPosition;
	public WorkflowPositionPojo getFromPosition() { return fromPosition; }
	public void setFromPosition(WorkflowPositionPojo fromPosition) { this.fromPosition = fromPosition; }
	public boolean hasFromPosition() { return this.fromPosition != null; }
	
	@RDFProperty( value=NS.OMNOM.PROP_TO_POSITION, serializeAsURI = true )
	private WorkflowPositionPojo toPosition;
	public WorkflowPositionPojo getToPosition() { return toPosition; }
	public void setToPosition(WorkflowPositionPojo toPosition) { this.toPosition = toPosition; }
	public boolean hasToPosition() { return this.toPosition != null; }

	@RDFProperty( value=NS.OMNOM.PROP_FROM_PARAM, serializeAsURI = true )
	private ParameterPojo fromParam;
	public ParameterPojo getFromParam() { return fromParam; }
	public void setFromParam(ParameterPojo fromParam) { this.fromParam = fromParam; }
	public boolean hasFromParam() { return this.fromParam != null; }
	
	@RDFProperty( value=NS.OMNOM.PROP_TO_PARAM, serializeAsURI = true )
	private ParameterPojo toParam;
	public ParameterPojo getToParam() { return toParam; }
	public void setToParam(ParameterPojo toParam) { this.toParam = toParam; }
	public boolean hasToParam() { return this.toParam != null; }
	
//	@RDFProperty(NS.OMNOM.PROP_FROM_STRING)
//	private String fromString;
//	public String getFromString() { return fromString; }
//	public void setFromString(String string) { this.fromString = string; }
//	public boolean hasFromString() { return this.fromString == null; }
	
	// TODO create ErrorMsgs
	@Override
	public ValidationReport validate() {
        ValidationReport res = new ValidationReport(this);

        if (! hasInWorkflow()) {
            res.addMessage(this,1,"Every parameter connector must exist inside a workflow.");
		    return res;
        }
		if (! hasFromParam()) {
            res.addMessage(this,2,"Missing Param: Every Workflow Connector must source from a param (either Workflow or Position).");
		}
		if (! hasToParam()) {
            res.addMessage(this,3,"Missing Param: Every Workflow Connector must point to a param (either Workflow or Position).");
		}
        if (!res.valid()) return res;
		if (! hasToWorkflow() && ! hasToPosition() || hasToWorkflow() && hasToPosition()) {
            res.addMessage(this,4,"ParameterConnector must have either toPosition or toWorkflow set.");
		}
		if (! hasFromWorkflow() && ! hasFromPosition() || hasFromWorkflow() && hasFromPosition()) {
            res.addMessage(this,5,"ParameterConnector must have either fromPosition or fromWorkflow set.");
		}
        if (!res.valid()) return res;
        if (hasFromWorkflow() && getFromWorkflow().getParamByName(getFromParam().getId())==null) {
            res.addMessage(this,8,"Parameter " + getFromParam() + " does not exist in " + getFromWorkflow());
        }
        if (hasFromPosition() && getFromPosition().getWebservice().getParamByName(getFromParam().getId())==null) {
            res.addMessage(this,9,"Parameter " + getFromParam() + " does not exist in " + getFromPosition());
        }
        if (hasToWorkflow() && getToWorkflow().getParamByName(getToParam().getId())==null) {
            res.addMessage(this,10,"Parameter " + getToParam() + " does not exist in " + getFromWorkflow());
        }
        if (hasToPosition() && getToPosition().getWebservice().getParamByName(getToParam().getId())==null) {
            res.addMessage(this,11,"Parameter " + getToParam() + " does not exist in " + getFromPosition());
        }

        return res;
    }

    public String getDot(String color) {
        return DotUtils.connect(
                fromPosition!=null?fromPosition.getDotId():fromWorkflow.getDotIdIn(),
                fromParam.getDotId(),
                toPosition!=null?toPosition.getDotId():toWorkflow.getDotIdOut(),
                toParam.getDotId(),
                color);
        }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(hasFromWorkflow()?"WF":getFromPosition())
                .append("/").append(getFromParam())
                .append("->")
                .append(hasToWorkflow()?"WF":getToPosition())
                .append("/").append(getToParam());

        return sb.toString();
    }
}
