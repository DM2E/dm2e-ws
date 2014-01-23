package eu.dm2e.ws.tests.unit.api;

import java.io.File;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoMongoImpl;
import eu.dm2e.grafeo.json.GrafeoJsonSerializer;
import eu.dm2e.grafeo.junit.GrafeoAssert;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.tests.OmnomTestResources;
import eu.dm2e.ws.tests.OmnomUnitTest;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
//import eu.dm2e.ws.services.xslt.XsltService;

public class WebservicePojoTest extends OmnomUnitTest {
	
	WebservicePojo ws;
	ParameterPojo xsltInParam
				, xmlInParam
				, xmlOutParam;
	
	static String 	WS_PARAM_XML_IN = "xmlIn",
					WS_PARAM_XSLT_IN = "xsltIn",
					WS_PARAM_XML_OUT = "xmlOut";

	@Before
	public void setUp() throws Exception {
		
		String serviceUri = "http://NON_RESOLVABLE";
		
		ws = new WebservicePojo();
		ws.setId(serviceUri);
		
		xsltInParam = ws.addInputParameter(WS_PARAM_XML_IN);
		xsltInParam.setComment("XSLT input");
		xsltInParam.setIsRequired(true);
		
		xmlInParam = ws.addInputParameter(WS_PARAM_XSLT_IN);
		xmlInParam.setComment("XML input");
		xmlInParam.setIsRequired(true); 
		
		xmlOutParam = ws.addOutputParameter(WS_PARAM_XML_OUT);
		xmlOutParam.setComment("XML output");
	}

	@Test
	public void testWebServicePojo() {
		ws.getInputParams().add(xmlInParam);
		ws.getInputParams().add(xsltInParam);
		ws.getOutputParams().add(xmlOutParam);
		GrafeoMongoImpl g = new GrafeoMongoImpl();
        g.getObjectMapper().addObject(ws);
        log.info("WS: " + g.getTurtle());
        WebservicePojo ws2 = g.getObjectMapper().getObject(WebservicePojo.class, ws.getId());
        Grafeo g2 = new GrafeoMongoImpl();
        g2.getObjectMapper().addObject(ws2);
        log.info("WS 2: " + g2.getTurtle());
        GrafeoAssert.graphsAreEquivalent(g, g2);
    }
	
	@Test
	public void testSerializeToJson() {
//		log.info(ws.toJson());
		WebservicePojo wsNew = GrafeoJsonSerializer.deserializeFromJSON(ws.toJson(), WebservicePojo.class);
		
//		assertEquals(ws.getUuid(), wsNew.getUuid());
		assertEquals(ws.getId(), wsNew.getId());
		assertEquals(ws.getInputParams().size(), wsNew.getInputParams().size());
		assertEquals(ws.getOutputParams().size(), wsNew.getOutputParams().size());
		// FIXME this could fail because of randomness in Set iterator
//		assertEquals(ws.getInputParams().iterator().next().getUuid(), 
//				wsNew.getInputParams().iterator().next().getUuid());
//		log.info(wsNew.toJson());
//		assertEquals(ws.toJson(), wsNew.toJson());
//		assertEquals(ws, wsNew);
	}
	@Test
	public void testValidateXsltService() throws Exception {
		WebserviceConfigPojo wsconf = new WebserviceConfigPojo();
		
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
	
	@Test
	public void testWorkflowWebservice() throws Exception {
		String wfwsTTL = configString.get(OmnomTestResources.WORKFLOW_WEBSERVICE_DESC);
		GrafeoMongoImpl g = new GrafeoMongoImpl();
		g.readHeuristically(wfwsTTL);
		WebservicePojo wfws = g.getObjectMapper().getObject(WebservicePojo.class, "http://localhost:9998/api/exec/workflow/ddfa3a03-cdf0-4298-937e-e69cb863a48b");
//		log.debug("" + GrafeoJsonSerializer.getGson().getAdapter(WebservicePojo.class));
		wfws.toJson();
	}

}
