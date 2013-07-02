package eu.dm2e.ws.api;



import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.services.xslt.XsltService;

public class WorkflowPojoTest extends OmnomTestCase {

	private WorkflowPojo wf;
	
	@Before
	public void setUp() throws Exception {
		wf = new WorkflowPojo();
		wf.addOutputParameter("fnor");
		wf.addOutputParameter("clors").setId("http://bloerk");
		
		final WebserviceConfigPojo pos1_conf = new WebserviceConfigPojo();
		pos1_conf.setWebservice(new XsltService().getWebServicePojo());
		pos1_conf.addParameterAssignment(XsltService.PARAM_XML_IN, "http://foo.xml");
		pos1_conf.addParameterAssignment(XsltService.PARAM_XSLT_IN, "http://foo.xsl");
		final WorkflowPositionPojo pos1 = new WorkflowPositionPojo();
		pos1.setWorkflow(wf);
		pos1.setWebserviceConfig(pos1_conf);
//		pos1.setId("http://foo");
		
		wf.addPosition(pos1);
		wf.validate();
	}
	
	@Test
	public void testJson() {
		{
//			WorkflowPojo wfNew = OmnomJsonSerializer.deserializeFromJSON(wf.toJson(), WorkflowPojo.class);
//			assertEquals(wf, wfNew);
		}
		log.info(wf.getTerseTurtle());
		log.info(wf.toJson());
		log.info(wf.getPositions().get(0).toJson());
		log.info(wf.getPositions().get(0).getTerseTurtle());
		{
//			JsonObject json = new JsonObject();
//			json.add("inputParams", new JsonArray());
//			json.add("parameterConnectors", new JsonArray());
//			JsonArray jPosArray = new JsonArray();
//			JsonObject jPosArrayObj = new JsonObject();
//			jPosArray.add(jPosArray)
		}
	}
}
