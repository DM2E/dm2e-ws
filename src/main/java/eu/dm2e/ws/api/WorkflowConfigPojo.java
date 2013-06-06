package eu.dm2e.ws.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:Workflow")
public class WorkflowConfigPojo implements ConfigPojo {
	
	public static final String PROP_HAS_POSITION = "omnom:hasPosition";
	public static final String PROP_PARAMETER_SLOT = "omnom:parameterSlot";
	Logger log = Logger.getLogger(getClass().getName());
    
    // TODO this is not perfect, there could be several slots for the same position and the same parameter
    public ParameterSlotPojo getSlotForPositionIndexAndParam(int index, ParameterPojo param) {
    	WorkflowPositionPojo pos = this.getPositions().get(index);
    	Logger log = Logger.getLogger(getClass().getName());
    	if (null != pos) {
    		for (ParameterSlotPojo thisSlot : parameterSlots) {
//		    	log.info("" + thisSlot.getForPosition().getWebService());
//		    	log.info(""+ pos.g);
    			if (thisSlot.getForPosition() != pos) {
    				continue;
    			}
		    	log.info("" + thisSlot.getForParam().getId());
    			if (thisSlot.getForParam() != null
    					&& thisSlot.getForParam().getId().equals(param.getId())
    					&& thisSlot.getForPosition() == pos) {
    				return thisSlot;
    			}
    		}
    	}
    	return null;
    }
	
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
			outstr.append(slot.getWebServicConfige().getWebservice().getId());
			outstr.append(" => ");
		}
		outstr.append("]");
		return outstr.toString();
	}

	@Override
	public void validateConfig() {
		log.warning("validateConfig UNIMPLEMENTED HERE");
	}
    
    /*********************
     * GETTERS/SETTERS
     ********************/

	// inherited
    @RDFId()
    private String id;
	
    @RDFProperty(PROP_HAS_POSITION) private List<WorkflowPositionPojo> positions = new ArrayList<>();
	public List<WorkflowPositionPojo> getPositions() { return positions; }
	public void setPositions(List<WorkflowPositionPojo> positions) { this.positions = positions; }
    
    @RDFProperty(PROP_PARAMETER_SLOT) private Set<ParameterSlotPojo> parameterSlots = new HashSet<>();
	public Set<ParameterSlotPojo> getParameterSlots() { return parameterSlots; }
	public void setParameterSlots(Set<ParameterSlotPojo> parameterSlots) { this.parameterSlots = parameterSlots; }

	
}
