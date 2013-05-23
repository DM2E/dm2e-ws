package eu.dm2e.ws.api;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/31/13
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "skos", "http://www.w3.org/2004/02/skos/core#",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass("omnom:Parameter")
public class ParameterPojo {
	
    @RDFId
    private String id;

    // TODO huh?
    @RDFProperty("omnom:helloWorld")
    private String hello;
    
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
	 * GETTERS/SETTERS
	 *****************/

	public WebservicePojo getWebservice() { return webservice; }
    public void setWebservice(WebservicePojo webservice) { this.webservice = webservice; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHello() { return hello; }
    public void setHello(String hello) { this.hello = hello; }

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
	public void setParameterType(String parameterType) { this.parameterType = parameterType; }


}
