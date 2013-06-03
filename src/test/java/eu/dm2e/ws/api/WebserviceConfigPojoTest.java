package eu.dm2e.ws.api;

import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public class WebserviceConfigPojoTest {
	
	private Logger log = Logger.getLogger(getClass().getName());
	
	
	@Test 
	public void testGetParameterAssignmentForParam() {
		WebservicePojo wsDesc = new WebservicePojo();
		wsDesc.setId("http://quux.bork/ws1/");
		ParameterPojo fooParam = wsDesc.addInputParameter("foo");
		fooParam.setIsRequired(true);
		
		WebserviceConfigPojo wsConf = new WebserviceConfigPojo();
		wsConf.setId("http://quux.bork/config/1");
		wsConf.setWebservice(wsDesc);
		wsConf.addParameterAssignment("foo", "bar");
		wsConf.validateConfig();
		
	}

	@Ignore
	@Test
	public void testSerialization() {
        log.info("TEST: Config serislization");
        WebserviceConfigPojo config = new WebserviceConfigPojo();
        WebservicePojo ws = new WebservicePojo();
        ws.setId("http://example.org/webservice1");
        ws.addInputParameter("testparam1");
        ws.addInputParameter("testparam2");
        ws.addOutputParameter("testparam3");
        config.setWebservice(ws);
        config.addParameterAssignment("testparam1", "1");
        config.addParameterAssignment("testparam2", "2");
        config.setId("http://example.org/config1");
        Grafeo g = new GrafeoImpl();
        g.getObjectMapper().addObject(config);
        log.fine("Serialized config: " + g.getTurtle());
        WebserviceConfigPojo config2 = g.getObjectMapper().getObject(WebserviceConfigPojo.class, "http://example.org/config1");
        assert (config.getId().equals(config2.getId()));
        assert (config.getWebservice().getId().equals(config2.getWebservice().getId()));
        assert (config.getParameterValueByName("testparam1").equals("1"));
        assert (config.getParameterValueByName("testparam2").equals("2"));
        assert (config2.getParameterValueByName("testparam1").equals("1"));
        assert (config2.getParameterValueByName("testparam2").equals("2"));
    }
	


}
