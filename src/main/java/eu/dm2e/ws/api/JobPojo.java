package eu.dm2e.ws.api;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass(NS.OMNOM.CLASS_JOB)
@RDFInstancePrefix("http://localhost:9998/job/")
public class JobPojo extends AbstractJobPojo {
	
    public JobPojo() { 
    	// move along nothing to see here
    }
    public JobPojo(URI joburi) {
    	try {
			this.loadFromURI(joburi);
		} catch (Exception e) {
			log.severe("Could reload job pojo." + e);
			e.printStackTrace();
		}
	}
    
    /**
     * Output Parameters
     */
    public void addOutputParameterAssignment(ParameterAssignmentPojo ass) {
    	this.outputParameters.add(ass);
//    	if (null != this.getId()) {
//    		client
//    			.resource(getId())
//    			.path("assignment")
//    			.type(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
//    			.entity(ass.getNTriples())
//    			.post();
//    	}
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
    
	/**
	 * GETTERS/SETTERS (non-javadoc)
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
	
    @RDFProperty(NS.OMNOM.PROP_ASSIGNMENT)
    Set<ParameterAssignmentPojo> outputParameters= new HashSet<>();
	public Set<ParameterAssignmentPojo> getOutputParameters() { return outputParameters; }
	public void setOutputParameters(Set<ParameterAssignmentPojo> outputParameters) { this.outputParameters = outputParameters; }

}
