package eu.dm2e.ws.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;

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
	
//	Logger log = Logger.getLogger(getClass().getName());
	
    @RDFId
    private String id;
    
    @RDFProperty("omnom:status")
    private String status = JobStatusConstants.NOT_STARTED.toString();
    
    // TODO the job probably doesn't even need a webservice reference since it's in the conf already
    @RDFProperty("omnom:hasWebService")
    private WebservicePojo webService;

    @RDFProperty("omnom:hasWebServiceConfig")
    private WebserviceConfigPojo webserviceConfig;
    
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
    	entry.setTimestamp(new Date());
    	this.logEntries.add(entry);
    	// TODO update to triplestore
    }
    public void trace(String message) { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.TRACE.toString()); this.publish();}
    public void debug(String message) { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.DEBUG.toString()); this.publish();}
    public void info(String message)  { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.INFO.toString());  this.publish();}
    public void warn(String message)  { log.warning("Job " + getId() +": " + message); this.addLogEntry(message, LogLevel.WARN.toString());  this.publish();}
    public void fatal(String message) { log.severe("Job " + getId() +": " + message);  this.addLogEntry(message, LogLevel.FATAL.toString()); this.publish();}
    
    public void trace(Exception e) { String msg = this.exceptionToString(e); this.trace(msg); }
    public void debug(Exception e) { String msg = this.exceptionToString(e); this.debug(msg); }
    public void fatal(Exception e) { String msg = this.exceptionToString(e); this.fatal(msg); }
    
    private String exceptionToString(Exception e) {
    	StringBuilder messageSB = new StringBuilder();
    	messageSB.append(ExceptionUtils.getStackTrace(e));
    	return messageSB.toString();
    }
    
    public Set<LogEntryPojo> getLogEntries(LogLevel minLevel, LogLevel maxLevel) {
		Set<LogEntryPojo> restrictedLogEntries = new HashSet<LogEntryPojo>();
		if (minLevel == null) {
			minLevel = LogLevel.TRACE;
		}
		if (maxLevel == null) {
			maxLevel = LogLevel.FATAL;
		}
		for (LogEntryPojo logEntry : this.getLogEntries()) {
			if (LogLevel.valueOf(logEntry.getLevel()).ordinal() >= minLevel.ordinal()
				&& LogLevel.valueOf(logEntry.getLevel()).ordinal() <= maxLevel.ordinal()) {
				restrictedLogEntries.add(logEntry);
			}
		}
		return restrictedLogEntries;
    }
    public Set<LogEntryPojo> getLogEntries(String minLevelStr, String maxLevelStr) {
    	LogLevel minLevel = null,
    			 maxLevel = null;
    	try {
			minLevel = LogLevel.valueOf(minLevelStr);
			maxLevel = LogLevel.valueOf(maxLevelStr);
		} catch (Exception e) {
			// TODO this isn't really a problem
		}
    	return getLogEntries(minLevel, maxLevel);
    }
    public Set<LogEntryPojo> getLogEntries(LogLevel minLevel) {
    	return getLogEntries(minLevel, null);
    }
    public Set<LogEntryPojo> getLogEntries(String minLevelStr) {
    	return getLogEntries(minLevelStr, null);
    }
    public List<LogEntryPojo> getLogEntriesSortedByDate(LogLevel minLevel, LogLevel maxLevel) {
    	List<LogEntryPojo> logList = new ArrayList<LogEntryPojo>(this.getLogEntries(minLevel, maxLevel));
    	Collections.sort(logList, new Comparator<LogEntryPojo>() {
			@Override
			public int compare(LogEntryPojo l1, LogEntryPojo l2) {
				return l1.getTimestamp().compareTo(l2.getTimestamp());
			}
    	});
    	return logList;
    }
    public List<LogEntryPojo> getLogEntriesSortedByDate(String minLevelStr, String maxLevelStr) {
    	LogLevel minLevel = null,
    			 maxLevel = null;
    	try {
			minLevel = LogLevel.valueOf(minLevelStr);
			maxLevel = LogLevel.valueOf(maxLevelStr);
		} catch (Exception e) {
			// TODO this isn't really a problem
		}
    	return getLogEntriesSortedByDate(minLevel, maxLevel);
    }
    
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
		this.publish();
	}
	public void setFinished() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatusConstants.FINISHED);
		this.setStatus(JobStatusConstants.FINISHED.toString()); 
		this.publish();
	}
	public void setFailed() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatusConstants.FAILED);
		this.setStatus(JobStatusConstants.FAILED.toString()); 
		this.publish();
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
	
	public WebserviceConfigPojo getWebserviceConfig() { return webserviceConfig; }
	public void setWebserviceConfig(WebserviceConfigPojo webserviceConfig) { this.webserviceConfig = webserviceConfig; }
	
	public Set<LogEntryPojo> getLogEntries() { return logEntries; }
	public void setLogEntries(Set<LogEntryPojo> logEntries) { this.logEntries = logEntries; }
	
	public Set<ParameterAssignmentPojo> getOutputParameters() { return outputParameters; }
	public void setOutputParameters(Set<ParameterAssignmentPojo> outputParameters) { this.outputParameters = outputParameters; }

}
