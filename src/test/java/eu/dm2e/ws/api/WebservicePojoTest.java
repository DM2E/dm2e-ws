package eu.dm2e.ws.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.OmnomUnitTest;
import eu.dm2e.ws.api.json.OmnomJsonSerializer;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.junit.GrafeoAssert;
import eu.dm2e.ws.services.xslt.XsltService;

public class WebservicePojoTest extends OmnomUnitTest {
	
	WebservicePojo ws;
	ParameterPojo xsltInParam
				, xmlInParam
				, xmlOutParam;

	@Before
	public void setUp() throws Exception {
		
		String serviceUri = "http://localhost:9998/service/xslt";
		
		ws = new WebservicePojo();
		ws.setId(serviceUri);
		
		xsltInParam = ws.addInputParameter(XsltService.PARAM_XSLT_IN);
		xsltInParam.setComment("XSLT input");
		xsltInParam.setIsRequired(true);
		
		xmlInParam = ws.addInputParameter(XsltService.PARAM_XML_IN);
		xmlInParam.setComment("XML input");
		xmlInParam.setIsRequired(true); 
		
		xmlOutParam = ws.addInputParameter(XsltService.PARAM_XML_OUT);
		xmlOutParam.setComment("XML output");
	}

	@Test
	public void testWebServicePojo() {
		ws.getInputParams().add(xmlInParam);
		ws.getInputParams().add(xsltInParam);
		ws.getOutputParams().add(xmlOutParam);
		GrafeoImpl g = new GrafeoImpl();
        g.getObjectMapper().addObject(ws);
        log.info("WS: " + g.getTurtle());
        WebservicePojo ws2 = g.getObjectMapper().getObject(WebservicePojo.class, ws.getId());
        Grafeo g2 = new GrafeoImpl();
        g2.getObjectMapper().addObject(ws2);
        log.info("WS 2: " + g2.getTurtle());
        GrafeoAssert.graphsAreEquivalent(g, g2);
    }
	
	@Test
	public void testSerializeToJson() {
		WebservicePojo wsNew = OmnomJsonSerializer.deserializeFromJSON(ws.toJson(), WebservicePojo.class);
		log.info(ws.toJson());
		log.info(wsNew.toJson());
		assertEquals(ws.toJson(), wsNew.toJson());
//		assertEquals(ws, wsNew);
	}
	@Test
	public void testValidateXsltService() throws Exception {
		WebservicePojo ws = new XsltService().getWebServicePojo();
		WebserviceConfigPojo wsconf = new WebserviceConfigPojo();
//		GrafeoImpl g = new GrafeoImpl();
		
//		ParameterAssignmentPojo ass1 = new ParameterAssignmentPojo();
//		ass1.setForParam(xmlInParam);
//		ass1.setParameterValue("http://141.20.126.155/api/file/50c73992e18a91933e00001a/data");
//		
//		ParameterAssignmentPojo ass2 = new ParameterAssignmentPojo();
//		ass2.setForParam(xsltInParam);
//		ass2.setParameterValue("http://141.20.126.155/api/file/50c7266ee18a91933e000003/data");
		
		wsconf.setWebservice(ws);
		wsconf.addParameterAssignment(xmlInParam.getId(), "http://141.20.126.155/api/file/50c73992e18a91933e00001a/data");
		wsconf.addParameterAssignment(xsltInParam.getId(), "http://141.20.126.155/api/file/50c7266ee18a91933e000003/data");
		assertNotNull(wsconf.getParameterAssignmentForParam(xmlInParam.getId()));
		assertNotNull(wsconf.getParameterAssignmentForParam(xsltInParam.getId()));
//		log.info("" + wsconf.getParameterAssignmentForParam(xsltInParam.getId()));
//		g.getObjectMapper().addObject(wsconf);
//		log.info(g.getTerseTurtle());
		wsconf.validate();
		
	}


}
