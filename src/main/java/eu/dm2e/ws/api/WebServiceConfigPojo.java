package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:WebServiceConfig")
public class WebServiceConfigPojo {
	
	@RDFProperty("omnom:assignment")
	private Set<ParameterAssignmentPojo> parameterAssignments = new HashSet<ParameterAssignmentPojo>();
	
	@RDFProperty("omnom:hasWebService")
	private WebservicePojo webservice;

	public Set<ParameterAssignmentPojo> getParameterAssignments() { return parameterAssignments; }
	public void setParameterAssignments(Set<ParameterAssignmentPojo> parameterAssignments) { this.parameterAssignments = parameterAssignments; }

	public WebservicePojo getWebservice() { return webservice; }
	public void setWebservice(WebservicePojo webservice) { this.webservice = webservice; }
	
}
