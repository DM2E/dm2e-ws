package eu.dm2e.ws.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.xslt.XsltServiceTest;

public class WebservicePojoTest {
	
	Logger log = Logger.getLogger(getClass().getName());
	
	WebservicePojo ws;
	ParameterPojo xsltInParam
				, xmlInParam
				, xmlOutParam;

	@Before
	public void setUp() throws Exception {
		
		String serviceUri = "http://data.dm2e.eu/data/services/xslt";
		
		ws = new WebservicePojo();
		ws.setId(serviceUri);
		
		xsltInParam = new ParameterPojo();
		xsltInParam.setTitle("XSLT input");
		xsltInParam.setIsRequired(true);
		xsltInParam.setWebservice(ws);
		xsltInParam.setId(serviceUri + "/xsltInParam");
		
		xmlInParam = new ParameterPojo();
		xmlInParam.setTitle("XML input");
		xmlInParam.setIsRequired(true); 
		xmlInParam.setWebservice(ws);
		xmlInParam.setId(serviceUri + "/xmlInParam");
		
		xmlOutParam = new ParameterPojo();
		xmlOutParam.setTitle("XML output");
		xmlOutParam.setWebservice(ws);
		xmlOutParam.setId(serviceUri + "/xmlOutParam");
	}

	@Test
	public void testWebServicePojo() {
		ws.getInputParams().add(xmlInParam);
		ws.getInputParams().add(xsltInParam);
		ws.getOutputParams().add(xmlOutParam);
		GrafeoImpl g = new GrafeoImpl();
		g.addObject(ws);
		log.info(g.getTurtle());
	}
	
	@Test
	public void testRunXsltService() throws URISyntaxException {
		WebservicePojo ws = XsltServiceTest.getWebService();
		WebServiceConfigPojo wsconf = new WebServiceConfigPojo();
		GrafeoImpl g = new GrafeoImpl();
		
		ParameterAssignmentPojo ass1 = new ParameterAssignmentPojo();
		ass1.setForParam(xmlInParam);
		ass1.setParameterValue(g.literal(new URI("http://141.20.126.155/api/file/50c73992e18a91933e00001a/data")));
		
		ParameterAssignmentPojo ass2 = new ParameterAssignmentPojo();
		ass2.setForParam(xsltInParam);
		ass2.setParameterValue(g.literal(new URI("http://141.20.126.155/api/file/50c7266ee18a91933e000003/data")));
		
		wsconf.setWebservice(ws);
		wsconf.getParameterAssignments().add(ass1);
		wsconf.getParameterAssignments().add(ass2);
		
		g.addObject(wsconf);
		log.info(g.getTurtle());
	}

}
