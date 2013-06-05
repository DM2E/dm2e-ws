package eu.dm2e.ws.api;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.sun.jersey.api.client.ClientResponse;

import eu.dm2e.utils.PojoUtils;
import eu.dm2e.ws.DM2E_MediaType;
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
	
	public static final String PROP_LOG_ENTRY = "omnom:hasLogEntry";
	public static final String PROP_JOB_STATUS = "omnom:status";
	public static final String PROP_OUTPUT_ASSIGNMENT = "omnom:assignment";
	public static final String PROP_WEBSERVICE = "omnom:webservice";
	public static final String PROP_WEBSERVICE_CONFIG = "omnom:webserviceConfig";
	
//	Logger log = Logger.getLogger(getClass().getName());
	
    @RDFId
    private String id;
    
    @RDFProperty(PROP_JOB_STATUS)
    private String status;
    
    // TODO the job probably doesn't even need a webservice reference since it's in the conf already
    @RDFProperty(PROP_WEBSERVICE)
    private WebservicePojo webService;

    @RDFProperty(PROP_WEBSERVICE_CONFIG)
    private WebserviceConfigPojo webserviceConfig;
    
    @RDFProperty(PROP_LOG_ENTRY)
    private Set<LogEntryPojo> logEntries = new HashSet<>();
    
    @RDFProperty(PROP_OUTPUT_ASSIGNMENT)
    private Set<ParameterAssignmentPojo> outputParameters= new HashSet<>();
    
    public JobPojo() { 
    	// move along nothing to see here
    }
    
    public JobPojo(URI joburi) {
    	this.loadFromURI(joburi);
	}
	/**
     * LOGGING
     */
    public void addLogEntry(LogEntryPojo entry) {
    	this.logEntries.add(entry);
    	publishLogEntry(entry);
    }
    public LogEntryPojo addLogEntry(String message, String level) {
    	LogEntryPojo entry = new LogEntryPojo();
    	entry.setMessage(message);
    	entry.setLevel(level);
    	entry.setTimestamp(new Date());
    	addLogEntry(entry);
    	return entry;
    }
    public void trace(String message) { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.TRACE.toString()); }
    public void debug(String message) { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.DEBUG.toString()); }
    public void info(String message)  { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.INFO.toString());  }
    public void warn(String message)  { log.warning("Job " + getId() +": " + message); this.addLogEntry(message, LogLevel.WARN.toString());  }
    public void fatal(String message) { log.severe("Job " + getId() +": " + message);  this.addLogEntry(message, LogLevel.FATAL.toString()); }
    
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
    	if (null != this.getId()) {
    		client
    			.resource(getId())
    			.path("assignment")
    			.type(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
    			.entity(ass.getNTriples())
    			.post();
    	}
    }
    public ParameterAssignmentPojo addOutputParameterAssignment(String forParam, String value) {
    	if (null == this.getWebService()) {
    		throw new RuntimeException("Job needs webservice.");
    	}
    	ParameterAssignmentPojo ass = new ParameterAssignmentPojo();
    	ass.setForParam(this.webService.getParamByName(forParam));
    	ass.setParameterValue(value);
    	this.outputParameters.add(ass);
    	return ass;
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
	 * Updating status
	 */
	public void setStatus(JobStatusConstants status) {
		setStatus(status.toString());
	}
	public void setStarted() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatusConstants.STARTED);
		this.setStatus(JobStatusConstants.STARTED.toString()); 
		publishJobStatus(status);
	}
	public void setFinished() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatusConstants.FINISHED);
		this.setStatus(JobStatusConstants.FINISHED.toString()); 
		publishJobStatus(status);
	}
	public void setFailed() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatusConstants.FAILED);
		this.setStatus(JobStatusConstants.FAILED.toString()); 
		publishJobStatus(status);
	}
	
	public boolean isFinished() { return this.getStatus().equals(JobStatusConstants.FINISHED.toString()); }
	public boolean isFailed() { return this.getStatus().equals(JobStatusConstants.FAILED.toString()); }
	public boolean isStarted() { return ! this.getStatus().equals(JobStatusConstants.NOT_STARTED.toString()); }
	
	@Override
	public String publishToService() {
		String loc = super.publishToService(this.client.getJobWebResource());
		JobPojo newPojo = new JobPojo();
		newPojo.loadFromURI(loc);
		try {
			PojoUtils.copyProperties(this, newPojo);
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.severe("Couldn't refresh this pojo with live data: " + e);
		}
		return loc;
	}
	protected void publishJobStatus(String status) {
		if (null != this.getId()) {
			ClientResponse resp = client
					.resource(this.getId())
					.path("status")
					.entity(status)
					.put(ClientResponse.class);
			if (resp.getStatus() != 201) {
				throw new RuntimeException("Couldn update status " + resp);
			}
		}
	}
	protected void publishLogEntry(LogEntryPojo entry) {
		if (null != this.getId()) {
			this.client
				.resource(this.getId())
				.path("log")
				.type(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
				.entity(entry.getNTriples())
				.post();
    	}
	}

	/*********************
	 * 
	 * GETTERS/SETTERS
	 * 
	 *********************/
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getStatus() {
		if (null != status) return status;
		return JobStatusConstants.NOT_STARTED.toString();
	}
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
