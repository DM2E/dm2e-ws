package eu.dm2e.ws.api;

import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.utils.UriUtils;
import eu.dm2e.ws.NS;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/** Pojo for a webservice Job */
@RDFClass(NS.OMNOM.CLASS_JOB)
@RDFInstancePrefix("http://localhost:9998/job/")
public class JobPojo extends AbstractJobPojo {
	
	@Override
	public ParameterPojo getOutputParamByName(String needle) {
		if (null != this.getWebService())
			return this.getWebService().getParamByName(needle);
		return null;
	}
	
	@Override
	public ParameterPojo getInputParamByName(String needle) {
		return this.getOutputParamByName(needle);
	}
	
	@Override
	public Set<ParameterAssignmentPojo> getInputParameterAssignments() {
		if (null != this.getWebserviceConfig())
			return this.getWebserviceConfig().getParameterAssignments();
		return null;
	}
	
    public JobPojo() { 
    	// move along nothing to see here
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
	 * @see AbstractJobPojo for more
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
            rdfsLabelSB.append(getWebService().getLabel());
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
