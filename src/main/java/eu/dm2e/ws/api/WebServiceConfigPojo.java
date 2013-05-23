package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:WebServiceConfig")
//@RDFInstancePrefix("http://data.dm2e.eu/config/")
@RDFInstancePrefix("http://localhost:9998/data/configurations/")
public class WebServiceConfigPojo extends AbstractPersistentPojo<WebServiceConfigPojo>{
	
	@RDFId
	private String id;
	
	@RDFProperty("omnom:assignment")
	private Set<ParameterAssignmentPojo> parameterAssignments = new HashSet<ParameterAssignmentPojo>();
	
	@RDFProperty("omnom:hasWebService")
	private WebservicePojo webservice;
	
	/*********************
	 * HELPERS
	 ********************/
	
	public ParameterAssignmentPojo getParameterAssignmentForParam(String paramName) {
		for (ParameterAssignmentPojo ass : this.parameterAssignments) {
			try { 
//				log.warning("" + ass.getForParam().getId());
				if (ass.getForParam().getId().matches(".*" + paramName + "$")
					||
					ass.getForParam().getLabel().equals(paramName)
					){
					return ass;
				}
			} catch (NullPointerException e) {
				continue;
			}
		}
		return null;
	}
    public String getParameterValueByName(String needle) {
    	ParameterAssignmentPojo ass = this.getParameterAssignmentForParam(needle);
    	if (null != ass) {
    		return ass.getParameterValue();
    	}
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
