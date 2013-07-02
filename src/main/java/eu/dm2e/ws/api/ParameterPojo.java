package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
@Namespaces({"omnom", NS.OMNOM.BASE, 
			 "skos", NS.SKOS.BASE,
			 "dc", NS.DC.BASE
			 })
@RDFClass(NS.OMNOM.CLASS_PARAMETER)
public class ParameterPojo extends SerializablePojo<ParameterPojo>{
	
	/******************
	 * CONSTRUCTORS
	 *****************/

//    public ParameterPojo(String title, String value) {
//    	this.title = title;
//    	this.parameterValue = value;
//	}

	public ParameterPojo() {
		// TODO Auto-generated constructor stub
	}


    /******************
     * HELPERS
     *****************/

    public ParameterAssignmentPojo createAssignment(String value) {
        ParameterAssignmentPojo pa = new ParameterAssignmentPojo();
        pa.setForParam(this);
        pa.setParameterValue(value);
        return pa;
    }
    
    public boolean matchesParameterName(String needle) {
    	if (null == needle || "".equals(needle)) return false;
		return (
				(this.hasId() && this.getId().equals(needle))
			||
				(this.hasId() && this.getId().matches(".*/" + needle + "$"))
			||
				(this.hasLabel() && this.getLabel().equals(needle))
			);
    }
    
    public void validateParameterInput(String input) throws NumberFormatException {
    	if (null == getParameterType()) {
    		return;
    	}
    	String type = this.getParameterType();
    	if (type.equals(NS.XSD.INT)) {
			try {
				Integer.parseInt(input);
			} catch (NumberFormatException e) {
				throw e;
			}
    	}
    }
    
	/******************
	 * GETTERS/SETTERS
	 *****************/

	@RDFProperty(NS.OMNOM.PROP_WEBSERVICE)
    private WebservicePojo webservice;
	public WebservicePojo getWebservice() { return webservice; }
    public void setWebservice(WebservicePojo webservice) { this.webservice = webservice; }
    
	@RDFProperty(NS.OMNOM.PROP_WORKFLOW)
    private WorkflowPojo workflow;
	public WorkflowPojo getWorkflow() { return workflow; }
	public void setWorkflow(WorkflowPojo workflow) { this.workflow = workflow; }
    
    @RDFProperty(NS.RDFS.PROP_COMMENT)
    private String comment;
    public String getComment() { return comment; }
	public void setComment(String comment) { this.comment = comment; }
	
    @RDFProperty(NS.OMNOM.PROP_DEFAULT_VALUE)
    private String defaultValue;
    public String getDefaultValue() { return defaultValue; }
	public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

    @RDFProperty(NS.OMNOM.PROP_IS_REQUIRED)
    private boolean isRequired;
	public boolean getIsRequired() { return isRequired; }
	public void setIsRequired(boolean isRequired) { this.isRequired = isRequired; }
	
	@RDFProperty(NS.OMNOM.PROP_PARAMETER_TYPE)
	private String parameterType;
	public String getParameterType() { return parameterType; }
	public void setParameterType(String parameterType) { 
		Grafeo g = new GrafeoImpl();
		this.parameterType = g.expand(parameterType); 
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + (isRequired ? 1231 : 1237);
		result = prime * result + ((parameterType == null) ? 0 : parameterType.hashCode());
		result = prime * result + ((webservice == null) ? 0 : webservice.hashCode());
		result = prime * result + ((workflow == null) ? 0 : workflow.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof ParameterPojo)) return false;
		ParameterPojo other = (ParameterPojo) obj;
		if (comment == null) {
			if (other.comment != null) return false;
		} else if (!comment.equals(other.comment)) return false;
		if (defaultValue == null) {
			if (other.defaultValue != null) return false;
		} else if (!defaultValue.equals(other.defaultValue)) return false;
		if (isRequired != other.isRequired) return false;
		if (parameterType == null) {
			if (other.parameterType != null) return false;
		} else if (!parameterType.equals(other.parameterType)) return false;
		if (webservice == null) {
			if (other.webservice != null) return false;
		} else if (!webservice.equals(other.webservice)) return false;
		if (workflow == null) {
			if (other.workflow != null) return false;
		} else if (!workflow.equals(other.workflow)) return false;
		return true;
	}

}
