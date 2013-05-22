package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;

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
    
    @RDFProperty("omnom:hasLogEntry")
    private Set<LogEntryPojo> logEntries = new HashSet<LogEntryPojo>();
    
    @RDFProperty("omnom:hasOutputParam")
    private Set<ParameterAssignmentPojo> outputParameters= new HashSet<ParameterAssignmentPojo>();
    
    public void addLogEntry(LogEntryPojo entry) {
    	this.logEntries.add(entry);
    	// TODO update to triplestore
    }
    public void addLogEntry(String message, String level) {
    	LogEntryPojo entry = new LogEntryPojo();
    	entry.setMessage(message);
    	entry.setLevel(level);
    	this.logEntries.add(entry);
    	// TODO update to triplestore
    }
    
    public void addOutputParameterAssignment(ParameterAssignmentPojo ass) {
    	this.outputParameters.add(ass);
    	// TODO update to triplestore
    }
    public void addOutputParameterAssignment(String forParam, String value) {
    	ParameterAssignmentPojo ass = new ParameterAssignmentPojo();
    	// TODO ParameterPojo for forParam can be deduced by the job's web service
    	ass.setForParam(this.webService.getParamByName(forParam));
    	ass.setParameterValue(value);
    	// TODO update to triplestore
    }

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public WebservicePojo getWebService() { return webService; }
	public void setWebService(WebservicePojo webService) { this.webService = webService; }
//	public void setWebService(String webService) {
//		this.webService = new WebservicePojo();
//		this.webService.setId(webService);
//	}
	public WebServiceConfigPojo getWebServiceConfig() { return webServiceConfig; }
	public void setWebServiceConfig(WebServiceConfigPojo webServiceConfig) { this.webServiceConfig = webServiceConfig; }
	
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
