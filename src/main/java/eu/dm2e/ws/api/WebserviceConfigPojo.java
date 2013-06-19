package eu.dm2e.ws.api;

import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass(NS.OMNOM.CLASS_WEBSERVICE_CONFIG)
//@RDFInstancePrefix("http://data.dm2e.eu/config/")
@RDFInstancePrefix("http://localhost:9998/config/")
public class WebserviceConfigPojo extends AbstractPersistentPojo<WebserviceConfigPojo> implements IValidatable {
	
	/*********************
	 * HELPERS
	 ********************/
	
	public ParameterAssignmentPojo getParameterAssignmentForParam(String paramName) {
		log.info("Access to param assignment by name: " + paramName);
        for (ParameterAssignmentPojo ass : this.getParameterAssignments()) {
        	log.info("" + ass.getForParam());
			if (ass.getForParam().matchesParameterName(paramName)) {
				log.info("GOTCHA : " + ass.getParameterValue());
				return ass;
			}
		}
		log.warning("FAILED: Access to param assignment by name: " + paramName);
		return null;
	}
	public void addParameterAssignment(ParameterAssignmentPojo ass) {
		this.getParameterAssignments().add(ass);
	}
	public void addParameterAssignment(String paramName, String paramValue) {
		log.info("adding parameter assignment");
		
		IWebservice ws = this.getWebservice();
		if (null == ws) {
			throw new RuntimeException("WebserviceConfig contains no webservice. Can't introspect parameters.");
		}

		ParameterPojo param = this.getWebservice().getParamByName(paramName);
		if (null == param) {
			throw new RuntimeException("Webservice contains no such parameter: " + paramName);
		}
		ParameterAssignmentPojo ass = param.createAssignment(paramValue);
        // TODO: Why do we need a URI here, I created assignments successfully with blank nodes... (Kai)
		// TODO: Because export of blank nodes is broken. Fixing it. (kb)
		
        if (null != this.getId()) {
			ass.setId(this.getId() + "/assignment/" + UUID.randomUUID().toString());
			this.publishToService();
        }
		this.getParameterAssignments().add(ass);
	}
	
    public String getParameterValueByName(String needle) {
    	ParameterAssignmentPojo ass = this.getParameterAssignmentForParam(needle);
    	if (null != ass) {
    		return ass.getParameterValue();
    	}
        log.info("No value found for: " + needle);
    	return null;
    }
    
    /* (non-Javadoc)
	 * @see eu.dm2e.ws.api.ConfigPojo#validateConfig()
	 */
    @Override
	public void validate() throws Exception {
		/*
		 * Validate the config against the webservice description
		 */
		IWebservice ws = getWebservice();
		if (null == ws) {
			throw new RuntimeException("Can't validate without webservice description:\n" + this.getTurtle());
		}
		for (ParameterPojo param : ws.getInputParams()) {
			log.info("Validating param: " + param.getId());
			if (param.getIsRequired() && null == this.getParameterAssignmentForParam(param.getId())) {
				throw new RuntimeException(param.getId() + ": " + ErrorMsg.REQUIRED_PARAM_MISSING.toString());
			}
			ParameterAssignmentPojo ass = this.getParameterAssignmentForParam(param.getId());
			if (null == ass) {
				continue;
			}
			log.info("Validating assignment '" +  this.getParameterAssignmentForParam(param.getId()) + "' of parameter <" + param.getId() + "> against its restriction.");
			try {
				param.validateParameterInput(ass.getParameterValue());
			} catch (NumberFormatException e) {
				throw new RuntimeException(ass.getParameterValue() + ": "
						+ ErrorMsg.ILLEGAL_PARAMETER_VALUE.toString());
			}
		}
    }
    
    
	/*********************
	 * GETTERS SETTERS
	 ********************/
	
//	@RDFId
//	private String id;
//	public String getId() { return id; }
//	public void setId(String id) { this.id = id; }

	@RDFProperty(NS.OMNOM.PROP_ASSIGNMENT)
	private Set<ParameterAssignmentPojo> parameterAssignments = new HashSet<>();
	public Set<ParameterAssignmentPojo> getParameterAssignments() { return parameterAssignments; }
	public void setParameterAssignments(Set<ParameterAssignmentPojo> parameterAssignments) { this.parameterAssignments = parameterAssignments; }

	@RDFProperty(NS.OMNOM.PROP_WEBSERVICE)
	private WebservicePojo webservice;
	public WebservicePojo getWebservice() { return webservice; }
	public void setWebservice(WebservicePojo webservice) { this.webservice = webservice; }

}
