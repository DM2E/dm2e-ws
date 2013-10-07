package eu.dm2e.ws.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.model.LogLevel;

/**
 * Abstract base class for JobPojos
 * 
 * <p>
 * Every Jobpojo, workflow or webservice, has logging, status and assignments.
 * </p>
 *
 * @author Konstantin Baierer
 */
public abstract class AbstractJobPojo extends AbstractPersistentPojo<AbstractJobPojo> {
	
	public AbstractJobPojo() {
		super();
	}
	/*********************
	 * 
     * LOGGING
     * 
     ********************/
    public void addLogEntry(LogEntryPojo entry) {
    	this.getLogEntries().add(entry);
    	publishLogEntry(entry);
    }
    public LogEntryPojo addLogEntry(String message, String level) {
    	LogEntryPojo entry = new LogEntryPojo();
    	entry.setMessage(message);
    	entry.setLevel(level);
    	entry.setTimestamp(DateTime.now());
    	addLogEntry(entry);
    	return entry;
    }
    public void trace(String message) { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.TRACE.toString()); }
    public void debug(String message) { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.DEBUG.toString()); }
    public void info(String message)  { log.info("Job " + getId() +": " + message);    this.addLogEntry(message, LogLevel.INFO.toString());  }
    public void warn(String message)  { log.warn("Job " + getId() +": " + message); this.addLogEntry(message, LogLevel.WARN.toString());  }
    public void fatal(String message) { log.error("Job " + getId() +": " + message);  this.addLogEntry(message, LogLevel.FATAL.toString()); }
    
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
		} catch (Exception e) { /* this isn't a problem */ }
    	try { maxLevel = LogLevel.valueOf(maxLevelStr);
		} catch (Exception e) { /* this isn't a problem */ }
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

	/**********************
	 * 
	 * Updating status
	 * 
	 ********************/
	public void setStatus(JobStatus status) {
		setJobStatus(status.toString());
	}

	public void setStarted() {
		this.trace("Status change: " + this.getJobStatus() + " => " + JobStatus.STARTED);
		this.setJobStatus(JobStatus.STARTED.toString()); 
		publishJobStatus(this.getJobStatus());
	}

	public void setFinished() {
		this.trace("Status change: " + this.getJobStatus() + " => " + JobStatus.FINISHED);
		this.setJobStatus(JobStatus.FINISHED.toString()); 
		publishJobStatus(this.getJobStatus());
	}

	public void setFailed() {
		this.trace("Status change: " + this.getJobStatus() + " => " + JobStatus.FAILED);
		this.setJobStatus(JobStatus.FAILED.toString()); 
		publishJobStatus(this.getJobStatus());
	}

	public boolean isFinished() { return this.getJobStatus().equals(JobStatus.FINISHED.toString()); }

	public boolean isFailed() { return this.getJobStatus().equals(JobStatus.FAILED.toString()); }

	public boolean isStarted() { return ! this.getJobStatus().equals(JobStatus.NOT_STARTED.toString()); }

	public boolean isStillRunning() {
		return !(isFinished() || isFailed());
	}

	protected void publishJobStatus(String status) {
		if (this.hasId()) {
			Response resp = client
					.target(this.getId())
					.path("status")
					.request()
					.put(Entity.text(status));
			if (resp.getStatus() != 201) {
				throw new RuntimeException("Couldn update status " + resp);
			}
		}
	}

	protected void publishLogEntry(LogEntryPojo entry) {
		if (null != this.getId()) {
			Response resp = client
				.target(this.getId())
				.path("log")
				.request()
				.post(entry.getNTriplesEntity());
			if (resp.getStatus() != 201) {
				throw new RuntimeException("Couldn post log " + resp);
			}
		}
	}
	
	/*********************
	 * 
	 * Assignments
	 * 
	 ********************/
	
	public ParameterAssignmentPojo addOutputParameterAssignment(String paramName, String paramValue) {
		log.info("adding parameter assignment");
		ParameterPojo param = this.getOutputParamByName(paramName);
		if (null == param) {
			throw new RuntimeException("Job has no such output parameter: " + paramName);
		}
		ParameterAssignmentPojo ass = new ParameterAssignmentPojo();
		ass.setLabel(paramName);
		ass.setForParam(param);
		ass.setParameterValue(paramValue);
		this.getOutputParameterAssignments().add(ass);
		return ass;
	}
	public ParameterAssignmentPojo getOutputParameterAssignmentForParam(ParameterPojo param) {
		return this.getOutputParameterAssignmentForParam(param.getId());
	}
	public ParameterAssignmentPojo getOutputParameterAssignmentForParam(String needle) {
		for (ParameterAssignmentPojo ass: this.getOutputParameterAssignments()) {
			if (ass.getForParam().matchesParameterName(needle)) {
				return ass;
			}
		}
		return null;
	}
	/**
	 * Returns the value of an output parameter.
	 * 
	 * @param needle
	 * @return
	 */
	public String getOutputParameterValueByName(String needle) {
		ParameterAssignmentPojo ass = getOutputParameterAssignmentForParam(needle);
		if (ass != null) {
			return ass.getParameterValue();
		}
		return null;
	}
	public ParameterAssignmentPojo getInputParameterAssignmentForParam(ParameterPojo param) {
		return getInputParameterAssignmentForParam(param.getId());
	}
	public ParameterAssignmentPojo getInputParameterAssignmentForParam(String needle) {
		for (ParameterAssignmentPojo ass: this.getInputParameterAssignments()) {
			if (ass.getForParam().matchesParameterName(needle)) {
				return ass;
			}
		}
		return null;
	}
	/**
	 * Returns the value of an input parameter, or its default value if no assignment is found.
	 * 
	 * @param needle
	 * @return
	 */
	public String getInputParameterValueByName(String needle) {
		ParameterAssignmentPojo ass = getInputParameterAssignmentForParam(needle);
		if (ass != null) {
			return ass.getParameterValue();
		} else {
			ParameterPojo param = getInputParamByName(needle);
			if (null == param) 
				return null;
			String defaultValue = param.getDefaultValue();
			return defaultValue;
		}
	}

	abstract public ParameterPojo getInputParamByName(String needle);
	abstract public Set<ParameterAssignmentPojo> getInputParameterAssignments();
	abstract public ParameterPojo getOutputParamByName(String paramName);

	/*********************
	 * 
	 * GETTERS/SETTERS
	 * 
	 *********************/
	
    @RDFProperty(NS.OMNOM.PROP_JOB_STATUS)
    String jobStatus;
	public String getJobStatus() {
		if (null != jobStatus) return jobStatus;
		return JobStatus.NOT_STARTED.toString();
	}
	public void setJobStatus(String status) { this.jobStatus = status; }
	
    @RDFProperty(NS.OMNOM.PROP_LOG_ENTRY) 
    Set<LogEntryPojo> logEntries = new HashSet<>();
	public Set<LogEntryPojo> getLogEntries() { return logEntries; }
	public void setLogEntries(Set<LogEntryPojo> logEntries) { this.logEntries = logEntries; }
	
    @RDFProperty(NS.OMNOM.PROP_ASSIGNMENT)
    Set<ParameterAssignmentPojo> outputParameterAssignments = new HashSet<>();
	public Set<ParameterAssignmentPojo> getOutputParameterAssignments() { return outputParameterAssignments; }
	public void setOutputParameterAssignments(Set<ParameterAssignmentPojo> outputParameters) { this.outputParameterAssignments = outputParameters; }

	@RDFProperty(NS.DCTERMS.PROP_MODIFIED)
	private DateTime modified = DateTime.now();
	public DateTime getModified() { return modified; }
	public void setModified(DateTime modified) { this.modified = modified; }

	@RDFProperty(NS.DCTERMS.PROP_CREATED)
	private DateTime created = DateTime.now();
	public DateTime getCreated() { return created; }
	public void setCreated(DateTime created) { this.created = created; }


}
