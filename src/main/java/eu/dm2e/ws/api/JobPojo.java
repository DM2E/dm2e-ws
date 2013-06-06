package eu.dm2e.ws.api;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.model.JobStatus;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass("omnom:Job")
@RDFInstancePrefix("http://localhost:9998/job/")
public class JobPojo extends AbstractJobPojo{
	
	public static final String PROP_OUTPUT_ASSIGNMENT = "omnom:assignment";
	public static final String PROP_WEBSERVICE = "omnom:webservice";
	public static final String PROP_WEBSERVICE_CONFIG = "omnom:webserviceConfig";
	
//	Logger log = Logger.getLogger(getClass().getName());
    
    // TODO the job probably doesn't even need a webservice reference since it's in the conf already
    @RDFProperty(PROP_WEBSERVICE)
    private WebservicePojo webService;

    @RDFProperty(PROP_WEBSERVICE_CONFIG)
    private WebserviceConfigPojo webserviceConfig;
    
    @RDFProperty(PROP_OUTPUT_ASSIGNMENT) Set<ParameterAssignmentPojo> outputParameters= new HashSet<>();
    
    public JobPojo() { 
    	// move along nothing to see here
    }
    public JobPojo(URI joburi) {
    	this.loadFromURI(joburi);
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
    
	public WebservicePojo getWebService() { return webService; }
	public void setWebService(WebservicePojo webService) { this.webService = webService; }
	
	public WebserviceConfigPojo getWebserviceConfig() { return webserviceConfig; }
	public void setWebserviceConfig(WebserviceConfigPojo webserviceConfig) { this.webserviceConfig = webserviceConfig; }
	
	public Set<ParameterAssignmentPojo> getOutputParameters() { return outputParameters; }
	public void setOutputParameters(Set<ParameterAssignmentPojo> outputParameters) { this.outputParameters = outputParameters; }
	
	/*
	 * from AbstractJobPojo
	 */
	
    @RDFId String id;
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

    @RDFProperty(PROP_JOB_STATUS) String status;
	public String getStatus() {
		if (null != status) return status;
		return JobStatus.NOT_STARTED.toString();
	}
	public void setStatus(String status) { this.status = status; }

    @RDFProperty(PROP_LOG_ENTRY) Set<LogEntryPojo> logEntries = new HashSet<>();
	public Set<LogEntryPojo> getLogEntries() { return logEntries; }
	public void setLogEntries(Set<LogEntryPojo> logEntries) { this.logEntries = logEntries; }
	
	@RDFProperty("omnom:hasSlotAssignment")
	private Set<ParameterSlotAssignmentPojo> slotAssignments = new HashSet<>();

	public Set<ParameterSlotAssignmentPojo> getSlotAssignments() { return slotAssignments; }
	public void setSlotAssignments(Set<ParameterSlotAssignmentPojo> slotAssignments) { this.slotAssignments = slotAssignments; }
}
