package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;


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
	public void validate() {
		if (! hasInWorkflow()) {
			throw new RuntimeException("Every parameter connector must exist inside a workflow.");
		}
		if (! hasFromParam()) {
			throw new RuntimeException("Missing Param: Every Workflow Connector must source from a param (either Workflow or Position).");
		}
		if (! hasToParam()) {
			throw new RuntimeException("Missing Param: Every Workflow Connector must point to a param (either Workflow or Position).");
		}
		if (! hasToWorkflow() && ! hasToPosition()) {
			throw new RuntimeException("ParameterConnector must have a toPosition or a toWorkflow pair.");
		}
		if (! hasToWorkflow() && ! hasToPosition()) {
			throw new RuntimeException("ParameterConnector must have a fromPosition or a fromWorkflow pair.");
		}
	}
}