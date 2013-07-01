package eu.dm2e.ws.api;

import java.net.URI;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass(NS.OMNOM.CLASS_JOB)
@RDFInstancePrefix("http://localhost:9998/job/")
public class JobPojo extends AbstractJobPojo {
	
	@Override
	public ParameterPojo getOutputParamByName(String needle) {
		if (null != this.getWebService())
			return this.getWebService().getParamByName(needle);
		return null;
	}
	
	@Override
	public ParameterPojo getInputParamByName(String needle) {
		if (null != this.getWebService())
			return this.getWebService().getParamByName(needle);
		return null;
	}
	
    public JobPojo() { 
    	// move along nothing to see here
    }
    public JobPojo(URI joburi) {
    	try {
			this.loadFromURI(joburi);
		} catch (Exception e) {
			log.severe("Could reload job pojo." + e);
			e.printStackTrace();
		}
	}
    
	/**
	 * GETTERS/SETTERS
	 * 
	 * @see AbstractJobPojo for more
	 */

    @RDFProperty(NS.OMNOM.PROP_WEBSERVICE)
    private WebservicePojo webService;
	public WebservicePojo getWebService() { return webService; }
	public void setWebService(WebservicePojo webService) { this.webService = webService; }

    @RDFProperty(NS.OMNOM.PROP_WEBSERVICE_CONFIG)
    private WebserviceConfigPojo webserviceConfig;
	public WebserviceConfigPojo getWebserviceConfig() { return webserviceConfig; }
	public void setWebserviceConfig(WebserviceConfigPojo webserviceConfig) { this.webserviceConfig = webserviceConfig; }


}
