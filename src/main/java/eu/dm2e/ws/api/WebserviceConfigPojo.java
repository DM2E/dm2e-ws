package eu.dm2e.ws.api;

import eu.dm2e.utils.UriUtils;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.grafeo.annotations.RDFProperty;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration of a webservice
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass(NS.OMNOM.CLASS_WEBSERVICE_CONFIG)
//@RDFInstancePrefix("http://data.dm2e.eu/config/")
@RDFInstancePrefix("http://localhost:9998/config/")
public class WebserviceConfigPojo extends AbstractPersistentPojo<WebserviceConfigPojo> implements IValidatable {



    /**
     * Get ParameterAssignmentPojo for a specific parameter.
     *
     * @see ParameterPojo#matchesParameterName(String)
     * @param param  ParameterPojo to match
     * @return The ParameterAssignmentPojo if found or null otherwise
     * @see #getParameterAssignmentForParam(String)
     */
    public ParameterAssignmentPojo getParameterAssignmentForParam(ParameterPojo param) {
        return getParameterAssignmentForParam(param.getId());
    }

    /**
     * Get ParameterAssignmentPojo for a parameter by name.
     *
     * @see ParameterPojo#matchesParameterName(String)
     * @param paramName  parameter name to match
     * @return The ParameterAssignmentPojo if found or null otherwise
     */
    public ParameterAssignmentPojo getParameterAssignmentForParam(String paramName) {
        log.info("Access to param assignment by name: " + paramName);
        for (ParameterAssignmentPojo ass : this.getParameterAssignments()) {
            log.trace("Parameter being checked " + ass.getForParam());
            if (ass.getForParam().matchesParameterName(paramName)) {
                log.info("GOTCHA : " + ass.getParameterValue());
                return ass;
            }
        }
        log.info("No assignment for " + paramName + " in config " + this);
        return null;
    }

    /**
     * Creates a ParameterAssignmentPojo and adds it to the object's assignments.
     *
     * @param paramName  Name of the parameter, used to match a parameter and as the assignment's label
     * @param paramValue Value to set the parameter to.
     * @return the created ParameterAssignmentPojo
     *
     * @throws RuntimeException if no parameter matches paramName
     * @throws RuntimeException if paramValue is null
     */
    public ParameterAssignmentPojo addParameterAssignment(String paramName, String paramValue) {
        log.info("Adding parameter assignment for {}", paramName);
        ParameterPojo param = this.getParamByName(paramName);
        if (null == param) {
            throw new RuntimeException("Webservice/Workflow " + this.toString() + " contains no such parameter: " + paramName);
        }
        if (null == paramValue) {
            throw new RuntimeException("Parameter value for param " + param.getId() + " is null!");
        }

        ParameterAssignmentPojo ass = new ParameterAssignmentPojo();
        ass.setLabel(paramName);
        ass.setForParam(param);
        ass.setParameterValue(paramValue);
        this.getParameterAssignments().add(ass);
        return ass;
    }

    /** Add a ParameterAssignmentPojo to the assignments */
    public void addParameterAssignment(ParameterAssignmentPojo ass) {
        this.getParameterAssignments().add(ass);
    }

    /**
     * Get the value of an assignment by the name of the parameter
     * @param needle  A parameter's ID, label or substring thereof
     * @return The value if an assignment could be found, null otherwise
     */
    public String getParameterValueByName(String needle) {
        ParameterAssignmentPojo ass = this.getParameterAssignmentForParam(needle);
        if (null != ass) {
            String value = ass.getParameterValue();
            value = UriUtils.sanitizeInput(value);
            return value;
        }
        log.info("No value found for: " + needle);
        return null;
    }

    public String getParameterValueOrDefaultByName(String needle) {
        String value = getParameterValueByName(needle);
        if (value==null) {
            value = getParamByName(needle).getDefaultValue();
            value = UriUtils.sanitizeInput(value);
        }
        log.debug("Value returned for " + needle + ": " + value);
        return value;
    }


    /*********************
     * GETTERS/SETTERS
     ********************/

    @RDFProperty(NS.OMNOM.PROP_ASSIGNMENT)
    private Set<ParameterAssignmentPojo> parameterAssignments = new HashSet<>();
    /** parameterAssignments getter */
    public Set<ParameterAssignmentPojo> getParameterAssignments() { return parameterAssignments; }
    /** parameterAssignments setter */
    public void setParameterAssignments(Set<ParameterAssignmentPojo> parameterAssignments) { this.parameterAssignments = parameterAssignments; }



    @RDFProperty(NS.DCTERMS.PROP_MODIFIED)
    private DateTime modified = DateTime.now();
    /** modified getter */
    public DateTime getModified() { return modified; }
    /** modified setter */
    public void setModified(DateTime modified) { this.modified = modified; }

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


    @RDFProperty(NS.OMNOM.PROP_JOB_STARTED)
    Set<WebserviceConfigPojo> startedJobs = new HashSet<>();
    public void setStartedJobs(Set<WebserviceConfigPojo> startedJobs) { this.startedJobs = startedJobs;}
    public Set<WebserviceConfigPojo> getStartedJobs() {return startedJobs;}


    @RDFProperty(NS.OMNOM.PROP_JOB_PARENT)
    JobPojo parentJob;
    public void setParentJob(JobPojo job) {parentJob = job;}
    public JobPojo getParentJob() {return parentJob;}


}
