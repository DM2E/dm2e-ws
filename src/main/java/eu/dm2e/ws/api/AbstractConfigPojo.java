package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

public abstract class AbstractConfigPojo<T> extends AbstractPersistentPojo<T> implements IValidatable {
	
	public abstract ParameterPojo getParamByName(String needle);
	
	public ParameterAssignmentPojo getParameterAssignmentForParam(String paramName) {
		log.info("Access to param assignment by name: " + paramName);
        for (ParameterAssignmentPojo ass : this.getParameterAssignments()) {
        	log.fine("Parameter being checked " + ass.getForParam());
        	if (ass.getForParam().matchesParameterName(paramName)) {
				log.info("GOTCHA : " + ass.getParameterValue()); 
                return ass;
			}
		}
        log.info("No assignment for " + paramName + " in config " + this);
		return null;
	}
	
	public void addParameterAssignment(String paramName, String paramValue) {
		log.info("adding parameter assignment");
		ParameterPojo param = this.getParamByName(paramName);
		if (null == param) {
			throw new RuntimeException("Webservice contains no such parameter: " + paramName);
		}
		
		ParameterAssignmentPojo ass = new ParameterAssignmentPojo();
		ass.setLabel(paramName);
		ass.setForParam(param);
		ass.setParameterValue(paramValue);
		this.getParameterAssignments().add(ass);
	}
	
	public void addParameterAssignment(ParameterAssignmentPojo ass) {
		this.getParameterAssignments().add(ass);
	}
	
    public String getParameterValueByName(String needle) {
    	ParameterAssignmentPojo ass = this.getParameterAssignmentForParam(needle);
    	if (null != ass) {
    		return ass.getParameterValue();
    	}
        log.info("No value found for: " + needle);
    	return null;
    }
    

    /*********************
     * GETTERS/SETTERS
     ********************/
	
	@RDFProperty(NS.OMNOM.PROP_ASSIGNMENT)
	private Set<ParameterAssignmentPojo> parameterAssignments = new HashSet<>();
	public Set<ParameterAssignmentPojo> getParameterAssignments() { return parameterAssignments; }
	public void setParameterAssignments(Set<ParameterAssignmentPojo> parameterAssignments) { this.parameterAssignments = parameterAssignments; }

}
