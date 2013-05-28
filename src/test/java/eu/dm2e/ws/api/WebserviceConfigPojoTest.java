package eu.dm2e.ws.api;

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import org.junit.Test;

import java.util.logging.Logger;

public class WebserviceConfigPojoTest {
	
	Logger log = Logger.getLogger(getClass().getName());
	


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
        config.getParameterAssignments().add(ws.getParamByName("testparam1").createAssignment("1"));
        config.getParameterAssignments().add(ws.getParamByName("testparam2").createAssignment("2"));
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
