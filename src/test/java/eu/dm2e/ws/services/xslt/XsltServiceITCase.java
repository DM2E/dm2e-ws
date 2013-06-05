package eu.dm2e.ws.services.xslt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;

import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * @author kb
 *
 */
public class XsltServiceITCase extends OmnomTestCase {
	
    private String SERVICE_URI;
    private WebservicePojo SERVICE_POJO;

	@Before
    public void setUp() throws Exception {
        SERVICE_URI = URI_BASE + "service/xslt";
    	SERVICE_POJO = new XsltService().getWebServicePojo();
    }
	
	@Ignore("Focus on the other tests")
    @Test
    public void testDescription() {
    	log.info(SERVICE_URI);
    	Grafeo g = new GrafeoImpl(client.getJerseyClient()
    			.resource(SERVICE_URI)
    			.accept("text/turtle")
    			.get(InputStream.class));
    	log.info(g.getTurtle());
    	assertTrue(g.containsStatementPattern(SERVICE_URI, "rdf:type", "omnom:Webservice"));
    	assertTrue(g.containsStatementPattern(SERVICE_URI, "omnom:inputParam", SERVICE_URI + "/param/xmlInput"));
    	assertTrue(g.containsStatementPattern(SERVICE_URI + "/param/xmlInput", "rdf:type", "omnom:Parameter"));
    	assertTrue(g.containsStatementPattern(SERVICE_URI + "/param/xmlInput", "omnom:parameterType", g.literal(g.expand("xsd:anyURI"))));
    }
    
	/**
	 * TODO  The test works but there is some weird bug that occurs infrequently, Jena complaining* about QNames when trying to do Grafeo.load(String). Need to investigate
	 * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testTransformation() throws IOException, InterruptedException {
    	OmnomTestResources xmlRes = OmnomTestResources.XML_DTA_GRIMM;
    	OmnomTestResources xsltRes = OmnomTestResources.XSLT_KBA_BBAW_TO_EDM;
    	String xmlUri = client.publishFile(configFile.get(xmlRes));
    	String xsltUri = client.publishFile(configFile.get(xsltRes));
    	assertNotNull(xmlUri);
    	assertNotNull(xsltUri);
    	
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	assertThat(tC.getId(), is(nullValue()));
    	tC.publishToService();
    	assertThat(tC.getId(), not(nullValue()));
    	log.info("config uri: " + tC.getId());
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.XML_IN_PARAM_NAME, xmlUri);
    	tC.addParameterAssignment(XsltService.XSLT_IN_PARAM_NAME, xsltUri);
    	tC.publishToService();
    	
    	ClientResponse resp = client.putPojoToService(tC, SERVICE_URI);
    	log.info(tC.getTurtle());
    	log.info(resp.getEntity(String.class));
    	assertEquals(202, resp.getStatus());
    	assertNotNull(resp.getLocation());
    	URI jobUri = resp.getLocation();
    	
    	JobPojo job = new JobPojo();
    	job.loadFromURI(jobUri);
    	
    	int maxWait = 10;
    	int i = 0;
    	while (!(job.isFinished() || job.isFailed())) {
    		if (i++ == maxWait) {
    			break;
    		}
	    	Thread.sleep(1000);
	    	job.loadFromURI(jobUri);
	    	log.info(job.toLogString());
    	}
    	String resultUri = job.getParameterValueByName(XsltService.XML_OUT_PARAM_NAME);
    	assertNotNull(resultUri);
    	log.info("Job finished. Result is at " + resultUri );
    	
    	String xmlContent = client.resource(resultUri).get(String.class);
    	log.info(xmlContent);
    }
		
}
