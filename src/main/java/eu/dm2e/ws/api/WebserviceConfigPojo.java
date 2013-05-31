package eu.dm2e.ws.api;

import eu.dm2e.ws.grafeo.annotations.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:WebServiceConfig")
//@RDFInstancePrefix("http://data.dm2e.eu/config/")
@RDFInstancePrefix("http://localhost:9998/data/configurations/")
public class WebserviceConfigPojo extends AbstractPersistentPojo<WebserviceConfigPojo>{
	
	@RDFId
	private String id;
	
	@RDFProperty("omnom:assignment")
	private Set<ParameterAssignmentPojo> parameterAssignments = new HashSet<>();
	
	@RDFProperty("omnom:hasWebService")
	private WebservicePojo webservice;
	
	/*********************
	 * HELPERS
	 ********************/
	
	public ParameterAssignmentPojo getParameterAssignmentForParam(String paramName) {
		log.info("Access to param assignment by name: " + paramName);
        for (ParameterAssignmentPojo ass : this.getParameterAssignments()) {
			try { 
//				log.warning("" + ass.getForParam().getId());
				if (ass.getForParam().getId().matches(".*" + paramName + "$")
					||
					ass.getForParam().getLabel().equals(paramName)
					){
                    return ass;
				}
			} catch (NullPointerException e) {
            }
		}
		return null;
	}
	public void addParameterAssignment(ParameterAssignmentPojo ass) {
		this.getParameterAssignments().add(ass);
	}
	public void addParameterAssignment(String paramName, String paramValue) {
		log.info("adding parameter assignment");
		ParameterPojo param = this.getWebservice().getParamByName(paramName);
		if (null == param) {
			throw new RuntimeException("Webservice contains no such parameter: " + paramName);
		}
		ParameterAssignmentPojo ass = param.createAssignment(paramValue);
		ass.setId(this.getId() + "/assignment/" + UUID.randomUUID().toString());
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
	 * GETTERS SETTERS
	 ********************/
    
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public Set<ParameterAssignmentPojo> getParameterAssignments() { return parameterAssignments; }
	public void setParameterAssignments(Set<ParameterAssignmentPojo> parameterAssignments) { this.parameterAssignments = parameterAssignments; }

	public WebservicePojo getWebservice() { return webservice; }
	public void setWebservice(WebservicePojo webservice) { this.webservice = webservice; }

}
