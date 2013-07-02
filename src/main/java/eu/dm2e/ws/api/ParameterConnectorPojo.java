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
	
	@RDFProperty(NS.OMNOM.PROP_IN_WORKFLOW)
	private WorkflowPojo inWorkflow;
	public WorkflowPojo getInWorkflow() { return inWorkflow; }
	public void setInWorkflow(WorkflowPojo wf) { this.inWorkflow = wf; }
	public boolean hasInWorkflow() { return this.inWorkflow != null; }
	
	@RDFProperty(NS.OMNOM.PROP_FROM_WORKFLOW)
	private WorkflowPojo fromWorkflow;
	public WorkflowPojo getFromWorkflow() { return fromWorkflow; }
	public void setFromWorkflow(WorkflowPojo wf) { this.fromWorkflow = wf; }
	public boolean hasFromWorkflow() { return this.fromWorkflow != null; }
	
	@RDFProperty(NS.OMNOM.PROP_TO_WORKFLOW)
	private WorkflowPojo toWorkflow;
	public WorkflowPojo getToWorkflow() { return toWorkflow; }
	public void setToWorkflow(WorkflowPojo wf) { this.toWorkflow = wf; }
	public boolean hasToWorkflow() { return this.toWorkflow != null; }
	
	@RDFProperty(NS.OMNOM.PROP_FROM_POSITION)
	private WorkflowPositionPojo fromPosition;
	public WorkflowPositionPojo getFromPosition() { return fromPosition; }
	public void setFromPosition(WorkflowPositionPojo fromPosition) { this.fromPosition = fromPosition; }
	public boolean hasFromPosition() { return this.fromPosition != null; }
	
	@RDFProperty(NS.OMNOM.PROP_TO_POSITION)
	private WorkflowPositionPojo toPosition;
	public WorkflowPositionPojo getToPosition() { return toPosition; }
	public void setToPosition(WorkflowPositionPojo toPosition) { this.toPosition = toPosition; }
	public boolean hasToPosition() { return this.toPosition != null; }

	@RDFProperty(NS.OMNOM.PROP_FROM_PARAM)
	private ParameterPojo fromParam;
	public ParameterPojo getFromParam() { return fromParam; }
	public void setFromParam(ParameterPojo fromParam) { this.fromParam = fromParam; }
	public boolean hasFromParam() { return this.fromParam != null; }
	
	@RDFProperty(NS.OMNOM.PROP_TO_PARAM)
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
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result + ((fromParam == null) ? 0 : fromParam.hashCode());
		result = prime * result + ((fromPosition == null) ? 0 : fromPosition.hashCode());
		result = prime * result + ((fromWorkflow == null) ? 0 : fromWorkflow.hashCode());
		result = prime * result + ((inWorkflow == null) ? 0 : inWorkflow.hashCode());
		result = prime * result + ((toParam == null) ? 0 : toParam.hashCode());
		result = prime * result + ((toPosition == null) ? 0 : toPosition.hashCode());
		result = prime * result + ((toWorkflow == null) ? 0 : toWorkflow.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof ParameterConnectorPojo)) return false;
		ParameterConnectorPojo other = (ParameterConnectorPojo) obj;
		if (fromParam == null) {
			if (other.fromParam != null) return false;
		} else if (!fromParam.equals(other.fromParam)) return false;
		if (fromPosition == null) {
			if (other.fromPosition != null) return false;
		} else if (!fromPosition.equals(other.fromPosition)) return false;
		if (fromWorkflow == null) {
			if (other.fromWorkflow != null) return false;
		} else if (!fromWorkflow.equals(other.fromWorkflow)) return false;
		if (inWorkflow == null) {
			if (other.inWorkflow != null) return false;
		} else if (!inWorkflow.equals(other.inWorkflow)) return false;
		if (toParam == null) {
			if (other.toParam != null) return false;
		} else if (!toParam.equals(other.toParam)) return false;
		if (toPosition == null) {
			if (other.toPosition != null) return false;
		} else if (!toPosition.equals(other.toPosition)) return false;
		if (toWorkflow == null) {
			if (other.toWorkflow != null) return false;
		} else if (!toWorkflow.equals(other.toWorkflow)) return false;
		return true;
	}
	
}