package eu.dm2e.ws.api;

import java.lang.reflect.InvocationTargetException;
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
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.model.LogLevel;

public abstract class AbstractJobPojo extends AbstractPersistentPojo<JobPojo> {
	
	public static final String PROP_LOG_ENTRY = "omnom:hasLogEntry";
	public static final String PROP_JOB_STATUS = "omnom:status";

	public AbstractJobPojo() {
		super();
	}
	/**
     * LOGGING
     */
    public void addLogEntry(LogEntryPojo entry) {
    	this.getLogEntries().add(entry);
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
	 * Updating status
	 */
	public void setStatus(JobStatus status) {
		setStatus(status.toString());
	}

	public void setStarted() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatus.STARTED);
		this.setStatus(JobStatus.STARTED.toString()); 
		publishJobStatus(this.getStatus());
	}

	public void setFinished() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatus.FINISHED);
		this.setStatus(JobStatus.FINISHED.toString()); 
		publishJobStatus(this.getStatus());
	}

	public void setFailed() {
		this.trace("Status change: " + this.getStatus() + " => " + JobStatus.FAILED);
		this.setStatus(JobStatus.FAILED.toString()); 
		publishJobStatus(this.getStatus());
	}

	public boolean isFinished() { return this.getStatus().equals(JobStatus.FINISHED.toString()); }

	public boolean isFailed() { return this.getStatus().equals(JobStatus.FAILED.toString()); }

	public boolean isStarted() { return ! this.getStatus().equals(JobStatus.NOT_STARTED.toString()); }

	@Override
	public String publishToService() {
		String loc = this.publishToService(this.client.getJobWebResource());
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
			ClientResponse resp = this.client
				.resource(this.getId())
				.path("log")
				.type(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
				.entity(entry.getNTriples())
				.post(ClientResponse.class);
			if (resp.getStatus() != 201) {
				throw new RuntimeException("Couldn post log " + resp);
			}
		}
	}

	/*********************
	 * 
	 * GETTERS/SETTERS
	 * 
	 *********************/
	
	abstract public String getId();
	abstract public void setId(String id);

	public abstract String getStatus();
	public abstract void setStatus(String status);

	public abstract Set<LogEntryPojo> getLogEntries();
	public abstract void setLogEntries(Set<LogEntryPojo> logEntries);


}