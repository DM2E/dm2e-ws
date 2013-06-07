package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;


@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:ParameterSlot")
public class ParameterSlotPojo extends SerializablePojo {

	@RDFId
	private String id;
	
//	
//	@RDFProperty("omnom:outputForPosition")
//	private WorkflowPositionPojo outputForPosition;
	
	
	/******************
	 * GETTERS/SETTERS
	 *****************/

	@RDFProperty("omnom:forPosition")
	private WorkflowPositionPojo inputForPosition;
	public WorkflowPositionPojo getForPosition() { return inputForPosition; }
	public void setForPosition(WorkflowPositionPojo inputForPosition) { this.inputForPosition = inputForPosition; }

	@RDFProperty(NS.OMNOM.PROP_FOR_PARAM)
	private ParameterPojo forParam;
	public ParameterPojo getForParam() { return forParam; }
	public void setForParam(ParameterPojo forParam) { this.forParam = forParam; }

	@RDFProperty("omnom:connectedSlot")
	private ParameterSlotPojo connectedSlot;
	public ParameterSlotPojo getConnectedSlot() { return connectedSlot; }
	public void setConnectedSlot(ParameterSlotPojo connectedSlot) { this.connectedSlot = connectedSlot; }
}