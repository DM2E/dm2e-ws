package eu.dm2e.ws.api;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.model.JobStatusConstants;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass("omnom:Job")
public class JobPojo {
	
    @RDFId
    private String id;
    
    @RDFProperty("omnom:status")
    private String status = JobStatusConstants.NOT_STARTED.toString();
    
    @RDFProperty("omnom:hasWebService")
    private WebservicePojo webService;
    
//    @RDFProperty("omnom:parameterAssignment")
//    private Set<ParameterAssignmentPojo> parameterAssignments = new HashSet<ParameterAssignmentPojo>();
    

    // TODO is this even necessary when we have parameters
    @RDFProperty("omnom:hasWebServiceConfig")
    private WebServiceConfigPojo webServiceConfig;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public WebservicePojo getWebService() { return webService; }
	public void setWebService(WebservicePojo webService) { this.webService = webService; }
	public void setWebService(String webService) {
		this.webService = new WebservicePojo();
		this.webService.setId(webService);
	}
	public WebServiceConfigPojo getWebServiceConfig() {
		return webServiceConfig;
	}
	public void setWebServiceConfig(WebServiceConfigPojo webServiceConfig) {
		this.webServiceConfig = webServiceConfig;
	}
	
//	public URI getWebServiceConfig() { return webServiceConfig; }
//	public void setWebServiceConfig(URI webServiceConfig) { this.webServiceConfig = webServiceConfig; }
//	public void setWebServiceConfig(String webServiceConfig) {
//		try {
//			this.webServiceConfig = new URI(webServiceConfig);
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
