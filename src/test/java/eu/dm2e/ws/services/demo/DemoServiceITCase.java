package eu.dm2e.ws.services.demo;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 5/27/13
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class DemoServiceITCase {

    Logger log = Logger.getLogger(getClass().getName());

    private Client client;
    private static String URI_BASE = "http://localhost:9998";

    @Before
    public void setUp()
            throws Exception {
        client = new Client();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDemo() {
        // fail("Not yet implemented");
        WebResource webResource = client.resource(URI_BASE + "/service/newdemo");
        WebserviceConfigPojo config = new WebserviceConfigPojo();
        Grafeo g1 = new GrafeoImpl();
        g1.load(webResource.getURI().toString());
        WebservicePojo ws = g1.getObject(WebservicePojo.class, webResource.getURI());
        config.setWebservice(ws);
        ParameterAssignmentPojo pa = new ParameterAssignmentPojo();
        pa.setForParam(ws.getParamByName("sleeptime"));
        pa.setParameterValue("10000");
        config.getParameterAssignments().add(pa);
        Grafeo g = new GrafeoImpl();
        g.addObject(config);
        log.info("Config to post: " + g.getTurtle());
        webResource.post(g.getTurtle());


    }

}
