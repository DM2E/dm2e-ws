package eu.dm2e.ws.api;

import eu.dm2e.ws.grafeo.annotations.*;
import eu.dm2e.ws.model.JobStatusConstants;
import eu.dm2e.ws.model.LogLevel;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    @RDFProperty("omnom:webservice")
    private WebservicePojo webService;

    @RDFProperty("omnom:webserviceConfig")
    private WebserviceConfigPojo webserviceConfig;
    
    @RDFProperty("omnom:hasLogEntry")
    private Set<LogEntryPojo> logEntries = new HashSet<>();
    
    @RDFProperty("omnom:hasOutputParam")
    private Set<ParameterAssignmentPojo> outputParameters= new HashSet<>();
    
    
    /**
     * LOGGING
     */
    public void addLogEntry(LogEntryPojo entry) {
    	entry.setId(getId() + "/log/" + UUID.randomUUID().toString());
    	this.logEntries.add(entry);
    	// TODO update to triplestore
    }
    public LogEntryPojo addLogEntry(String message, String level) {
    	LogEntryPojo entry = new LogEntryPojo();
    	entry.setId(getId() + "/log/" + UUID.randomUUID().toString());
    	entry.setMessage(message);
    	entry.setLevel(level);
    	entry.setTimestamp(new Date());
    	this.logEntries.add(entry);
    	return entry;
    }
    public void trace(String message) { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.TRACE.toString()); this.publishToEndpoint();}
    public void debug(String message) { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.DEBUG.toString()); this.publishToEndpoint();}
    public void info(String message)  { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.INFO.toString());  this.publishToEndpoint();}
    public void warn(String message)  { log.warning("Job " + getId() +": " + message); this.addLogEntry(message, LogLevel.WARN.toString());  this.publishToEndpoint();}
    public void fatal(String message) { log.severe("Job " + getId() +": " + message);  this.addLogEntry(message, LogLevel.FATAL.toString()); this.publishToEndpoint();}
    
    public void trace(Throwable e) { String msg = this.exceptionToString(e); this.trace(msg); }
    public void debug(Throwable e) { String msg = this.exceptionToString(e); this.debug(msg); }
    public void fatal(Throwable e) { String msg = this.exceptionToString(e); this.fatal(msg); }
    
    private String exceptionToString(Throwable e) {
    	StringBuilder messageSB = new StringBuilder();
    	messageSB.append(ExceptionUtils.getStackTrace(e));
    	return messageSB.toString();
    }
    
    public Set<LogEntryPojo> getLogEntries(LogLevel minLevel, LogLevel maxLevel) {
		Set<LogEntryPojo> restrictedLogEntries = new HashSet<>();
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
    	try { minLevel = LogLevel.valueOf(minLevelStr);
		} catch (Exception e) { /* this isn't really a problem */ }
    	try { maxLevel = LogLevel.valueOf(maxLevelStr);
		} catch (Exception e) { /* this isn't really a problem */ }
    	return getLogEntries(minLevel, maxLevel);
    }
    public Set<LogEntryPojo> getLogEntries(LogLevel minLevel) {
    	return getLogEntries(minLevel, null);
    }
    public Set<LogEntryPojo> getLogEntries(String minLevelStr) {
    	return getLogEntries(minLevelStr, null);
    }
    public List<LogEntryPojo> getLogEntriesSortedByDate(LogLevel minLevel, LogLevel maxLevel) {
    	List<LogEntryPojo> logList = new ArrayList<>(this.getLogEntries(minLevel, maxLevel));
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
    	try { minLevel = LogLevel.valueOf(minLevelStr);
		} catch (Exception e) { /* this isn't really a problem */ }
    	try { maxLevel = LogLevel.valueOf(maxLevelStr);
		} catch (Exception e) { /* this isn't really a problem */ }
    	return getLogEntriesSortedByDate(minLevel, maxLevel);
    }
    public String toLogString() {
    	return this.toLogString(null, null);
    }
    public String toLogString(String minLevel, String maxLevel) {
    	List<LogEntryPojo> logEntries = this.getLogEntriesSortedByDate(minLevel, maxLevel);
    	StringBuilder outputBuilder = new StringBuilder();
		for (LogEntryPojo logEntry : logEntries) {
			outputBuilder.append("[");
			outputBuilder.append(logEntry.getLevel());
			outputBuilder.append("] ");
			outputBuilder.append(logEntry.getTimestamp());
			outputBuilder.append(": ");
			outputBuilder.append(logEntry.getMessage());
			outputBuilder.append("\n");
		} 
		return outputBuilder.toString();
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
    	this.publishToEndpoint();
    	// TODO update to triplestore
    }

    public ParameterAssignmentPojo getParameterAssignmentForParam(String paramName) {
        log.info("Access to param assignment by name: " + paramName);
        for (ParameterAssignmentPojo ass : this.outputParameters) {
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
    public String getParameterValueByName(String needle) {
        ParameterAssignmentPojo ass = this.getParameterAssignmentForParam(needle);
        if (null != ass) {
            return ass.getParameterValue();
        }
        log.info("No value found for: " + needle);
        return null;
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
		this.publishToEndpoint();
	}
	public void setFinished() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatusConstants.FINISHED);
		this.setStatus(JobStatusConstants.FINISHED.toString()); 
		this.publishToEndpoint();
	}
	public void setFailed() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatusConstants.FAILED);
		this.setStatus(JobStatusConstants.FAILED.toString()); 
		this.publishToEndpoint();
	}
	
	public boolean isFinished() { return this.status.equals(JobStatusConstants.FINISHED.toString()); }
	public boolean isFailed() { return this.status.equals(JobStatusConstants.FINISHED.toString()); }
	public boolean isStarted() { return ! this.status.equals(JobStatusConstants.NOT_STARTED.toString()); }
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
