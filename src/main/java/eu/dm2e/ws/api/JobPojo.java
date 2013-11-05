package eu.dm2e.ws.api;

import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.utils.UriUtils;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.model.LogLevel;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.*;

/** Pojo for a webservice Job */
@RDFClass(NS.OMNOM.CLASS_JOB)
@RDFInstancePrefix("http://localhost:9998/job/")
public class JobPojo extends AbstractPersistentPojo<JobPojo> {

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



    public ParameterPojo getOutputParamByName(String needle) {
		if (null != this.getWebService())
			return this.getWebService().getParamByName(needle);
		return null;
	}
	
	public ParameterPojo getInputParamByName(String needle) {
		return this.getOutputParamByName(needle);
	}
	
	public Set<ParameterAssignmentPojo> getInputParameterAssignments() {
		if (null != this.getWebserviceConfig())
			return this.getWebserviceConfig().getParameterAssignments();
		return null;
	}
	
    public JobPojo() { 
    	super();
    }
    public JobPojo(URI joburi) {
    	try {
			this.loadFromURI(joburi);
		} catch (Exception e) {
			log.error("Could reload job pojo." + e);
			e.printStackTrace();
		}
	}
    
	/**
	 * GETTERS/SETTERS
	 * 
	 */

    @RDFProperty(NS.OMNOM.PROP_WEBSERVICE)
    private WebservicePojo webService;
	public WebservicePojo getWebService() { return webService; }
	public void setWebService(WebservicePojo webService) { this.webService = webService; }

    @RDFProperty(NS.OMNOM.PROP_WEBSERVICE_CONFIG)
    private WebserviceConfigPojo webserviceConfig;
	public WebserviceConfigPojo getWebserviceConfig() { return webserviceConfig; }
	public void setWebserviceConfig(WebserviceConfigPojo webserviceConfig) { this.webserviceConfig = webserviceConfig; }

    @RDFProperty(value = NS.OMNOM.PROP_FINISHED_JOB, serializeAsURI=true)
    private Set<JobPojo> finishedJobs = new HashSet<>();
    public Set<JobPojo> getFinishedJobs() { return finishedJobs; }
    public void setFinishedJobs(Set<JobPojo> finishedJobs) { this.finishedJobs = finishedJobs; }

    @RDFProperty(value = NS.OMNOM.PROP_RUNNING_JOB, serializeAsURI=true)
    private Set<JobPojo> runningJobs = new HashSet<>();
    public Set<JobPojo> getRunningJobs() { return runningJobs; }
    public void setRunningJobs(Set<JobPojo> runningJobs) { this.runningJobs = runningJobs; }

    public void setHumanReadableLabel() {
        log.info("Creating human-readable label");
        {
            StringBuilder rdfsLabelSB = new StringBuilder();
            rdfsLabelSB.append("Web service Job ");
            rdfsLabelSB.append("'");
            rdfsLabelSB.append(getWebService().getLabelorURI());
            rdfsLabelSB.append("'");
            rdfsLabelSB.append(" [");
            rdfsLabelSB.append(getCreated().toString());
            rdfsLabelSB.append(" for ");
            rdfsLabelSB.append(
                    null != getWebserviceConfig().getCreator()
                            ? UriUtils.lastUriSegment(getWebserviceConfig().getCreator().getId())
                            : null != getWebserviceConfig().getWasGeneratedBy()
                            ? UriUtils.lastUriSegment(getWebserviceConfig().getWasGeneratedBy().getId())
                            : "Unknown Creator");
            setLabel(rdfsLabelSB.toString());
        }
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((webService == null) ? 0 : webService.hashCode());
		result = prime * result + ((webserviceConfig == null) ? 0 : webserviceConfig.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof JobPojo)) return false;
		JobPojo other = (JobPojo) obj;
		if (webService == null) {
			if (other.webService != null) return false;
		} else if (!webService.equals(other.webService)) return false;
		if (webserviceConfig == null) {
			if (other.webserviceConfig != null) return false;
		} else if (!webserviceConfig.equals(other.webserviceConfig)) return false;
		return true;
	}

}
