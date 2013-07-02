package eu.dm2e.ws.api;

import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass(NS.OMNOM.CLASS_WEBSERVICE_CONFIG)
//@RDFInstancePrefix("http://data.dm2e.eu/config/")
@RDFInstancePrefix("http://localhost:9998/config/")
public class WebserviceConfigPojo extends AbstractConfigPojo<WebserviceConfigPojo> {
	
	@Override
	public ParameterPojo getParamByName(String needle) {
		return (this.getWebservice() != null)
				? this.getWebservice().getParamByName(needle)
				: null;
	}
	/*********************
	 * HELPERS
	 ********************/
    @Override
	public void validate() throws Exception {
		/*
		 * Validate the config against the webservice description
		 */
		IWebservice ws = getWebservice();
		if (null == ws) {
			throw new RuntimeException(this + ": Can't validate without webservice description:\n" + this.getTerseTurtle());
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
			log.info("Param is valid: " + param);
		}
		
		log.info("This config is valid: " + this );
    }
    
    
	/*********************
	 * GETTERS SETTERS
	 ********************/
	
//	@RDFId
//	private String id;
//	public String getId() { return id; }
//	public void setId(String id) { this.id = id; }

//	@RDFProperty(NS.OMNOM.PROP_ASSIGNMENT)
//	private Set<ParameterAssignmentPojo> parameterAssignments = new HashSet<>();
//	public Set<ParameterAssignmentPojo> getParameterAssignments() { return parameterAssignments; }
//	public void setParameterAssignments(Set<ParameterAssignmentPojo> parameterAssignments) { this.parameterAssignments = parameterAssignments; }

	@RDFProperty(NS.OMNOM.PROP_WEBSERVICE)
	private WebservicePojo webservice;
	public WebservicePojo getWebservice() { return webservice; }
	public void setWebservice(WebservicePojo webservice) { this.webservice = webservice; }

}
