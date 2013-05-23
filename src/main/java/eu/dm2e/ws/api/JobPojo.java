package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.model.JobStatusConstants;
import eu.dm2e.ws.model.LogLevel;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass("omnom:Job")
@RDFInstancePrefix("http://localhost:9998/job/")
public class JobPojo extends AbstractPersistentPojo<JobPojo>{
	
	Logger log = Logger.getLogger(getClass().getName());
	
    @RDFId
    private String id;
    
    @RDFProperty("omnom:status")
    private String status = JobStatusConstants.NOT_STARTED.toString();
    
    @RDFProperty("omnom:hasWebService")
    private WebservicePojo webService;

    @RDFProperty("omnom:hasWebServiceConfig")
    private WebServiceConfigPojo webServiceConfig;
    
    @RDFProperty("omnom:hasLogEntry")
    private Set<LogEntryPojo> logEntries = new HashSet<LogEntryPojo>();
    
    @RDFProperty("omnom:hasOutputParam")
    private Set<ParameterAssignmentPojo> outputParameters= new HashSet<ParameterAssignmentPojo>();
    
    
    /**
     * LOGGING
     */
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
    public void trace(String message) { log.info("Job " + getId() +": " + message); this.addLogEntry(message, LogLevel.TRACE.toString());}
    public void debug(String message) { log.info("Job " + getId() +": " + message); this.addLogEntry(message, LogLevel.DEBUG.toString());}
    public void info(String message)  { log.info("Job " + getId() +": " + message); this.addLogEntry(message, LogLevel.INFO.toString());}
    public void warn(String message)  { log.warning("Job " + getId() +": " + message); this.addLogEntry(message, LogLevel.WARN.toString());}
    public void fatal(String message) { log.severe("Job " + getId() +": " + message); this.addLogEntry(message, LogLevel.FATAL.toString());}
    
    /**
     * Output Parameters
     */
    public void addOutputParameterAssignment(ParameterAssignmentPojo ass) {
    	this.outputParameters.add(ass);
    	// TODO update to triplestore
    }
    public void addOutputParameterAssignment(String forParam, String value) {
    	ParameterAssignmentPojo ass = new ParameterAssignmentPojo();
    	// TODO ParameterPojo for forParam can be deduced by the job's web service
    	ass.setForParam(this.webService.getParamByName(forParam));
    	ass.setParameterValue(value);
    	this.outputParameters.add(ass);
    	this.publish();
    	// TODO update to triplestore
    }
    
    /**
     * Publish the job
     */
//    	// TODO implement publish to triplestore
//    public void publish() {
//    }

	/**
	 * Updating status
	 */
	public void setStatus(JobStatusConstants status) { this.status = status.toString(); }
	public void setStarted() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatusConstants.STARTED);
		this.setStatus(JobStatusConstants.STARTED.toString()); 
	}
	public void setFinished() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatusConstants.FINISHED);
		this.setStatus(JobStatusConstants.FINISHED.toString()); 
	}
	public void setFailed() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatusConstants.FAILED);
		this.setStatus(JobStatusConstants.FAILED.toString()); 
	}
	
	/*********************
	 * 
	 * GETTERS/SETTERS
	 * 
	 *********************/
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public WebservicePojo getWebService() { return webService; }
	public void setWebService(WebservicePojo webService) { this.webService = webService; }
	
	public WebServiceConfigPojo getWebServiceConfig() { return webServiceConfig; }
	public void setWebServiceConfig(WebServiceConfigPojo webServiceConfig) { this.webServiceConfig = webServiceConfig; }
	
	public Set<LogEntryPojo> getLogEntries() { return logEntries; }
	public void setLogEntries(Set<LogEntryPojo> logEntries) { this.logEntries = logEntries; }
	
	public Set<ParameterAssignmentPojo> getOutputParameters() { return outputParameters; }
	public void setOutputParameters(Set<ParameterAssignmentPojo> outputParameters) { this.outputParameters = outputParameters; }

}
