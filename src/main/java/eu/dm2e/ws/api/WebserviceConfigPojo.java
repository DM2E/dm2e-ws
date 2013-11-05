package eu.dm2e.ws.api;

import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.grafeo.annotations.RDFProperty;

/**
 * Configuration of a webservice
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass(NS.OMNOM.CLASS_WEBSERVICE_CONFIG)
//@RDFInstancePrefix("http://data.dm2e.eu/config/")
@RDFInstancePrefix("http://localhost:9998/config/")
public class WebserviceConfigPojo extends AbstractConfigPojo<WebserviceConfigPojo> {
	
	@Override
	public ParameterPojo getParamByName(String needle) {
		if (this.getWebservice() == null) {
			log.warn("{} has no webservice!", this);
			return null;
		}
		return this.getWebservice().getParamByName(needle);
				
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

	// link to the parent workflow that created this config
	@RDFProperty(NS.PROV.PROP_WAS_GENERATED_BY)
	private JobPojo wasGeneratedBy;
	public JobPojo getWasGeneratedBy() { return wasGeneratedBy; }
	public void setWasGeneratedBy(JobPojo wasGeneratedBy) { this.wasGeneratedBy = wasGeneratedBy; }

	// link to a person if it is a workflow config
	@RDFProperty(NS.DCTERMS.PROP_CREATOR)
	private UserPojo creator;
	public UserPojo getCreator() { return creator; }
	public void setCreator(UserPojo creator) { this.creator = creator; }
	
	// link to the position in a specific workflow this job executes
    @RDFProperty(value = NS.OMNOM.PROP_EXECUTES_POSITION)
    WorkflowPositionPojo executesPosition;
	public WorkflowPositionPojo getExecutesPosition() { return executesPosition; }
	public void setExecutesPosition(WorkflowPositionPojo executesPosition) { this.executesPosition = executesPosition; }


}
