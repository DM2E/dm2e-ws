package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/30/13
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:Webservice")
public class WebservicePojo {

//    @RDFId(prefix="http://data.dm2e.eu/data/services/")
	@RDFId
    private String id;

    @RDFProperty("omnom:helloWorld")
    private String hello;

    @RDFProperty("omnom:inputParam")
    private Set<ParameterPojo> inputParams = new HashSet<ParameterPojo>();

    @RDFProperty("omnom:outputParam")
    private Set<ParameterPojo> outputParams = new HashSet<ParameterPojo>();
    
    /*********************
     * HELPER FUNCTIONS
     * @return 
     *********************/
    private ParameterPojo addParameterByName(String paramName, boolean isOutput) {
    	ParameterPojo param = new ParameterPojo();
    	param.setId(this.getId() + "/param/" + paramName);
    	param.setLabel(paramName);
    	Set<ParameterPojo> paramSet = (isOutput) ? this.outputParams : this.inputParams;
    	paramSet.add(param);
    	return param;
    }
    
    public ParameterPojo addInputParameter(String paramName) {
    	return this.addParameterByName(paramName, false);
    }
    public ParameterPojo addOutputParameter(String paramName) {
    	return this.addParameterByName(paramName, true);
    }
    
    public ParameterPojo getParamByName(String needle) {
    	Logger log = Logger.getLogger(getClass().getName());
    	Set<ParameterPojo> allParams = new HashSet<ParameterPojo>();
    	allParams.addAll(inputParams);
    	allParams.addAll(outputParams);
    	for (ParameterPojo param : allParams) {
    		if (param.getId().matches(".*" + needle + "$")) {
    			return param;
    		}
    		else {
    			log.severe(param.getId() + " doesn't match " + needle);
    		}
    	}
    	return null;
    }
    
    /*********************
     * CONSTRUCTORS
     ********************/
    
    public WebservicePojo() {
    	// to make BeanUtils happy
    }
    
    /*********************
     * GETTERS/SETTERS
     ********************/

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getHello() { return hello; }
	public void setHello(String hello) { this.hello = hello; }
	public Set<ParameterPojo> getInputParams() { return inputParams; }
	public void setInputParams(Set<ParameterPojo> inputParams) { this.inputParams = inputParams; }
	public Set<ParameterPojo> getOutputParams() { return outputParams; }
	public void setOutputParams(Set<ParameterPojo> outputParams) { this.outputParams = outputParams; }

}