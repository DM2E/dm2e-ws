package eu.dm2e.ws.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:Workflow")
public class WorkflowPojo extends WebservicePojo {
	
    @RDFId(prefix="http://data.dm2e.eu/data/workflow/")
    private String id;
	
    @RDFProperty("omnom:hasPosition")
	private List<WorkflowPositionPojo> positions = new ArrayList<WorkflowPositionPojo>();
    
    @RDFProperty("omnom:hasParameterSlot")
    private Set<ParameterSlotPojo> parameterSlots = new HashSet<ParameterSlotPojo>();
	
	@Override
	public String toString() {
		StringBuilder outstr = new StringBuilder();
		outstr.append("Workflow: [");
//		outstr.append("{");
//		for (ParameterPojo param : this.parameters) {
//			outstr.append(param.getTitle());
//			outstr.append(": ");
//			outstr.append(param.getParameterValue());
//		}
//		outstr.append("}{");
		for (WorkflowPositionPojo slot : getPositions()) {
			outstr.append(slot.getWebService().getId());
			outstr.append(" => ");
		}
		outstr.append("]");
		return outstr.toString();
	}
    
    /*********************
     * GETTERS/SETTERS
     ********************/

	// inherited
//	public String getId() { return id; }
//	public void setId(String id) { this.id = id; }

	public List<WorkflowPositionPojo> getPositions() { return positions; }
	public void setPositions(List<WorkflowPositionPojo> positions) { this.positions = positions; }

	public Set<ParameterSlotPojo> getParameterSlots() { return parameterSlots; }
	public void setParameterSlots(Set<ParameterSlotPojo> parameterSlots) { this.parameterSlots = parameterSlots; }

}
