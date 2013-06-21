package eu.dm2e.ws.api;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import eu.dm2e.utils.PojoUtils;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass(NS.OMNOM.CLASS_JOB)
@RDFInstancePrefix("http://localhost:9998/job/")
public class JobPojo extends AbstractJobPojo{
	
//	Logger log = Logger.getLogger(getClass().getName());
    
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
    	ass.setForParam(this.getWebService().getParamByName(forParam));
    	ass.setParameterValue(value);
    	this.outputParameters.add(ass);
    	return ass;
    }

    public ParameterAssignmentPojo getParameterAssignmentForParam(String paramName) {
        log.info("Access to param assignment by name: " + paramName);
        for (ParameterAssignmentPojo ass : this.outputParameters) {
        	if (ass.hasForParam()
	    			&&
    			ass.getForParam().matchesParameterName(paramName))
        			return ass;
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
		log.info(newPojo.getTurtle());
		return loc;
	}
    
	/**
	 * GETTERS/SETTERS (non-javadoc)
	 * 
	 * @see AbstractJobPojo for more
	 */

    @RDFProperty(NS.OMNOM.PROP_WEBSERVICE_CONFIG)
    private WebserviceConfigPojo webserviceConfig;
	public WebserviceConfigPojo getWebserviceConfig() { return webserviceConfig; }
	public void setWebserviceConfig(WebserviceConfigPojo webserviceConfig) { this.webserviceConfig = webserviceConfig; }
	
    @RDFProperty(NS.OMNOM.PROP_ASSIGNMENT)
    Set<ParameterAssignmentPojo> outputParameters= new HashSet<>();
	public Set<ParameterAssignmentPojo> getOutputParameters() { return outputParameters; }
	public void setOutputParameters(Set<ParameterAssignmentPojo> outputParameters) { this.outputParameters = outputParameters; }

//	@RDFProperty(NS.OMNOM.PROP_SLOT_ASSIGNMENT)
//	private Set<ParameterSlotAssignmentPojo> slotAssignments = new HashSet<>();
//	public Set<ParameterSlotAssignmentPojo> getSlotAssignments() { return slotAssignments; }
//	public void setSlotAssignments(Set<ParameterSlotAssignmentPojo> slotAssignments) { this.slotAssignments = slotAssignments; }
}
