package eu.dm2e.ws.api;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.NS;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Pojo for a webservice.
 */
@Namespaces({
	"omnom", "http://onto.dm2e.eu/omnom/",
	"rdfs", "http://www.w3.org/2000/01/rdf-schema#"
	})
@RDFClass(NS.OMNOM.CLASS_WEBSERVICE)
public class WebservicePojo extends AbstractPersistentPojo<WebservicePojo> implements IWebservice {
	
    /*********************
     * HELPER FUNCTIONS
     * @return 
     *********************/
    private ParameterPojo addParameterByName(String paramName, boolean isOutput) {
    	ParameterPojo param = new ParameterPojo();
    	param.setId(this.getId() + "/param/" + paramName);
    	param.setLabel(paramName);
    	param.setWebservice(this);
    	Set<ParameterPojo> paramSet = (isOutput) ? this.getOutputParams() : this.getInputParams();
    	paramSet.add(param);
    	return param;
    }
    
    public WebserviceConfigPojo createConfig() {
    	WebserviceConfigPojo wsconf = new WebserviceConfigPojo();
    	wsconf.setWebservice(this);
        wsconf.setLabel("Config for " + getLabel());
    	return wsconf;
    }
    
    /* (non-Javadoc)
	 * @see eu.dm2e.ws.api.IWebservicePojo#addInputParameter(java.lang.String)
	 */
    @Override
	public ParameterPojo addInputParameter(String paramName) {
    	return this.addParameterByName(paramName, false);
    }
    /* (non-Javadoc)
	 * @see eu.dm2e.ws.api.IWebservicePojo#addOutputParameter(java.lang.String)
	 */
    @Override
	public ParameterPojo addOutputParameter(String paramName) {
    	return this.addParameterByName(paramName, true);
    }
    
    /* (non-Javadoc)
	 * @see eu.dm2e.ws.api.IWebservicePojo#getParamByName(java.lang.String)
	 */
    @Override
	public ParameterPojo getParamByName(String needle) {
    	Logger log = LoggerFactory.getLogger(getClass().getName());
    	Set<ParameterPojo> allParams = new HashSet<>();
    	allParams.addAll(getInputParams());
    	allParams.addAll(getOutputParams());
    	for (ParameterPojo param : allParams) {
    		log.info("Checking whether {} matches '{}'", param, needle);
    		if (param.matchesParameterName(needle)) {
    			return param;
    		}
    	}
        log.warn("No parameter found for needle: " + needle);
    	return null;
    }
    
    /*********************
     * CONSTRUCTORS
     ********************/
    
    public WebservicePojo() {
    	// to make BeanUtils happy
    }

    public WebservicePojo(String uri) {
        Grafeo g = new GrafeoImpl(uri);
        IWebservice ws = g.getObjectMapper().getObject(WebservicePojo.class, uri);
        try {
            BeanUtils.copyProperties(this, ws);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }


    public WebservicePojo(URI uri) {
        this(uri.toString());
    }
    /*********************
     * GETTERS/SETTERS
     ********************/
	
    @RDFProperty(NS.OMNOM.PROP_INPUT_PARAM)
    private Set<ParameterPojo> inputParams = new HashSet<>();
	@Override public Set<ParameterPojo> getInputParams() { return inputParams; }
	@Override public void setInputParams(Set<ParameterPojo> inputParams) { this.inputParams = inputParams; }

    @RDFProperty(NS.OMNOM.PROP_OUTPUT_PARAM)
    private Set<ParameterPojo> outputParams = new HashSet<>();
    @Override public Set<ParameterPojo> getOutputParams() { return outputParams; }
    @Override public void setOutputParams(Set<ParameterPojo> outputParams) { this.outputParams = outputParams; }

    @RDFProperty(NS.OMNOM.PROP_WEBSERVICE_ID)
    private String implementationID = null;
    public String getImplementationID() { return implementationID; }
    public void setImplementationID(String implementationID) { this.implementationID = implementationID; }


}
