package eu.dm2e.ws.api;

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
@Namespaces({
	"omnom", "http://onto.dm2e.eu/omnom/",
	"rdfs", "http://www.w3.org/2000/01/rdf-schema#"
	})
@RDFClass("omnom:Webservice")
public class WebservicePojo extends AbstractPersistentPojo<WebservicePojo> {



//    @RDFId(prefix="http://data.dm2e.eu/data/services/")
	@RDFId
    private String id;
	
	@RDFProperty("rdfs:label")
	private String label;

    @RDFProperty("omnom:inputParam")
    private Set<ParameterPojo> inputParams = new HashSet<>();

    @RDFProperty("omnom:outputParam")
    private Set<ParameterPojo> outputParams = new HashSet<>();
    
    /*********************
     * HELPER FUNCTIONS
     * @return 
     *********************/
    private ParameterPojo addParameterByName(String paramName, boolean isOutput) {
    	ParameterPojo param = new ParameterPojo();
    	param.setId(this.getId() + "/param/" + paramName);
    	param.setLabel(paramName);
    	param.setWebservice(this);
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
    	Set<ParameterPojo> allParams = new HashSet<>();
    	allParams.addAll(inputParams);
    	allParams.addAll(outputParams);
    	for (ParameterPojo param : allParams) {
    		if (param.getId().matches(".*" + needle + "$")) {
    			return param;
    		}
    	}
        log.warning("No parameter found for needle: " + needle);
    	return null;
    }
    
    /*********************
     * CONSTRUCTORS
     ********************/
    
    public WebservicePojo() {
    	// to make BeanUtils happy
    }

    public WebservicePojo(URI uri) {
        Grafeo g = new GrafeoImpl(uri.toString());
        WebservicePojo ws = g.getObjectMapper().getObject(WebservicePojo.class, uri);
        try {
            BeanUtils.copyProperties(this, ws);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }
    /*********************
     * GETTERS/SETTERS
     ********************/

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	
	public Set<ParameterPojo> getInputParams() { return inputParams; }
	public void setInputParams(Set<ParameterPojo> inputParams) { this.inputParams = inputParams; }
	
	public Set<ParameterPojo> getOutputParams() { return outputParams; }
	public void setOutputParams(Set<ParameterPojo> outputParams) { this.outputParams = outputParams; }

}
