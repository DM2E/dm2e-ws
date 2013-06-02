package eu.dm2e.ws.api;

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "skos", "http://www.w3.org/2004/02/skos/core#",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass("omnom:Parameter")
public class ParameterPojo {
	
    @RDFId
    private String id;

    @RDFProperty("dc:title")
    private String title;
    
    @RDFProperty("skos:label")
    private String label;
    
    @RDFProperty("omnom:isRequired")
    private boolean isRequired;
	
//	@RDFProperty("omnom:parameterValue")
//	private String parameterValue;
//	
	@RDFProperty("omnom:parameterType")
	private String parameterType;

	@RDFProperty("omnom:webservice")
    private WebservicePojo webservice;
    
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
    
    public void validateParameterInput(String input) throws NumberFormatException {
    	if (null == getParameterType()) {
    		return;
    	}
    	GrafeoImpl g = new GrafeoImpl();
    	String type = g.shorten(this.getParameterType());
    	if (type.equals("xsd:int")) {
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

	public WebservicePojo getWebservice() { return webservice; }
    public void setWebservice(WebservicePojo webservice) { this.webservice = webservice; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

    public String getLabel() { return label; }
	public void setLabel(String label) { this.title = label; }

	public boolean getIsRequired() { return isRequired; }
	public void setIsRequired(boolean isRequired) { this.isRequired = isRequired; }
//
//	public String getParameterValue() { return parameterValue; }
//	public void setParameterValue(String parameterValue) { this.parameterValue = parameterValue; }

	public String getParameterType() { return parameterType; }
	public void setParameterType(String parameterType) { 
		Grafeo g = new GrafeoImpl();
		this.parameterType = g.expand(parameterType); 
	}


}
