package eu.dm2e.ws.api;

import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

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

}
