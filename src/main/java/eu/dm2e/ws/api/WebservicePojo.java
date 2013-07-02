package eu.dm2e.ws.api;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.beanutils.BeanUtils;

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
    	Set<ParameterPojo> paramSet = (isOutput) ? this.outputParams : this.inputParams;
    	paramSet.add(param);
    	return param;
    }
    
    public WebserviceConfigPojo createConfig() {
    	WebserviceConfigPojo wsconf = new WebserviceConfigPojo();
    	wsconf.setWebservice(this);
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
    	Logger log = Logger.getLogger(getClass().getName());
    	Set<ParameterPojo> allParams = new HashSet<>();
    	allParams.addAll(inputParams);
    	allParams.addAll(outputParams);
    	for (ParameterPojo param : allParams) {
    		log.info(""+param);
    		if (param.matchesParameterName(needle)) {
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
        IWebservice ws = g.getObjectMapper().getObject(WebservicePojo.class, uri);
        try {
            BeanUtils.copyProperties(this, ws);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result + ((inputParams == null) ? 0 : inputParams.hashCode());
		result = prime * result + ((outputParams == null) ? 0 : outputParams.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof WebservicePojo)) return false;
		WebservicePojo other = (WebservicePojo) obj;
		if (inputParams == null) {
			if (other.inputParams != null) return false;
		} else if (!inputParams.equals(other.inputParams)) return false;
		if (outputParams == null) {
			if (other.outputParams != null) return false;
		} else if (!outputParams.equals(other.outputParams)) return false;
		return true;
	}

}
