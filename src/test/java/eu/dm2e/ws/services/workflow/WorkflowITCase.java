package eu.dm2e.ws.services.workflow;

import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.api.ParameterConnectorPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowConfigPojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.publish.PublishService;
import eu.dm2e.ws.services.xslt.XsltService;

public class WorkflowITCase extends OmnomTestCase {

	private Logger log = Logger.getLogger(getClass().getName());
	private WorkflowPojo wf = null;
	
	@Before
	public void setUp() throws Exception {
		wf = createWorkflow();
		Assert.assertNotNull(wf);
		publishWorkflow();
		Assert.assertNotNull(wf.getId());
	}
	
	@Test
	public void testGetWorkflow() {
		ClientResponse resp2 = client.resource(wf.getId()).get(ClientResponse.class);
		GrafeoImpl g = new GrafeoImpl(resp2.getEntityInputStream());
//		Assert.assertEquals("No more blank nodes", 0, g.listAnonStatements(null, null).size() );
	}
	
	@Test
	public void testValidate() {
		{
			WorkflowConfigPojo wfconf = new WorkflowConfigPojo();
			wfconf.setWorkflow(wf);
			wfconf.addParameterAssignment("inputXML", "foo");
			wfconf.addParameterAssignment("inputXSLT", "foo");
			log.info(wfconf.getTurtle());
			try {
				wfconf.validate();
			} catch (AssertionError e) {
				Assert.fail("Invalid Workflow" + e);
			}
		}
	}

	/**
	 * Workflow goes like this:
	 * 	- Transform XML to RDF/XML using XSLT
	 * 	- Publish RDF/XML
	 * @return 
	 * @throws Exception 
	 */
	public String publishWorkflow() throws Exception {
//		FileUtils.writeStringToFile(new File("SHOULD.ttl"), wf.getTurtle());
		log.info("testsimple: Publishing the workflow.");
		String x = wf.publishToService(client.getWorkflowWebResource());
//		log.info("testsimple: DONE Publishing the workflow.");
		{
			ClientResponse resp = client.resource(x).accept(DM2E_MediaType.TEXT_TURTLE).get(ClientResponse.class);
			Assert.assertEquals(200, resp.getStatus());
			String respStr = resp.getEntity(String.class);
			log.info("And here is the answer: \n" + respStr);
		}
//		wf.loadFromURI(wf.getId());
//		log.info(wf.getTurtle());
//		log.info(x);
		
//		WorkflowConfigPojo wfconf = new WorkflowConfigPojo();
//		wfconf.setId("WORKFLOW_CONFIG1");
//		wfconf.setWorkflow(wf);
//		log.info(wfconf.getTurtle());
//		try {
//			wfconf.getGrafeo().visualizeWithGraphviz();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		try {
			wf.getGrafeo().visualizeWithGraphviz();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		return wf.getId();
		
	}

/**
 * @return
 */
protected WorkflowPojo createWorkflow() {
	WorkflowPojo wf = new WorkflowPojo();
	String _ws_label = "XML -> XMLRDF -> DM2E yay";
	String _ws_param_xmlinput = "inputXML";
	String _ws_param_xsltinput = "inputXSLT";
	String _ws_param_outgraph = "outputGraph";
	String _ws_pos1_label = "XML -> XMLRDF";
	String _ws_pos2_label = "XMLRDF -> Graphstore";
	
	wf.setLabel(_ws_label);
	wf.addInputParameter(_ws_param_xmlinput);
	wf.addInputParameter(_ws_param_xsltinput);
	wf.addOutputParameter(_ws_param_outgraph);
	
	WorkflowPositionPojo step1_pos = new WorkflowPositionPojo();
	WebservicePojo step1_ws = new XsltService().getWebServicePojo();
	WebserviceConfigPojo step1_config = new WebserviceConfigPojo();
	step1_config.setWebservice(step1_ws);
	step1_pos.setWebserviceConfig(step1_config);
	step1_pos.setLabel(_ws_pos1_label);
	
	WorkflowPositionPojo step2_pos = new WorkflowPositionPojo();
	WebservicePojo step2_ws = new PublishService().getWebServicePojo();
	WebserviceConfigPojo step2_config = new WebserviceConfigPojo();
	step2_config.setWebservice(step2_ws);
	step2_pos.setWebserviceConfig(step2_config);
	step2_pos.setLabel(_ws_pos2_label);
	
	// workflow:inputXML => xmlrdf:xmlinput
	ParameterConnectorPojo step0_step1_xml = new ParameterConnectorPojo();
	step0_step1_xml.setFromWorkflow(wf);
	step0_step1_xml.setFromParam(wf.getParamByName(_ws_param_xmlinput));
	step0_step1_xml.setToPosition(step1_pos);
	step0_step1_xml.setToParam(step1_ws.getParamByName(XsltService.PARAM_XML_IN));
	wf.getParameterConnectors().add(step0_step1_xml);
	
	// workflow:inputXSLT => xmlrdf:xsltinput
	ParameterConnectorPojo step0_step1_xslt = new ParameterConnectorPojo();
	step0_step1_xslt.setFromWorkflow(wf);
	step0_step1_xslt.setFromParam(wf.getParamByName(_ws_param_xmlinput));
	step0_step1_xslt.setToPosition(step1_pos);
	step0_step1_xslt.setToParam(step1_ws.getParamByName(XsltService.PARAM_XML_IN));
	wf.getParameterConnectors().add(step0_step1_xslt);
	
	// xmlrdf:xmloutput => publish:to-publish
	ParameterConnectorPojo step1_step2_xml = new ParameterConnectorPojo();
	step1_step2_xml.setFromPosition(step1_pos);
	step1_step2_xml.setFromParam(step1_ws.getParamByName(XsltService.PARAM_XML_OUT));
	step1_step2_xml.setToPosition(step2_pos);
	step1_step2_xml.setToParam(step2_ws.getParamByName(PublishService.PARAM_TO_PUBLISH));
	wf.getParameterConnectors().add(step1_step2_xml);
	
	step1_pos.setWorkflow(wf);
	step1_config.setWebservice(step1_ws);
	step1_pos.setWebserviceConfig(step1_config);
	wf.addPosition(step1_pos);
	
	step2_config.setWebservice(step2_ws);
	step2_pos.setWorkflow(wf);
	step2_pos.setWebserviceConfig(step2_config);
	wf.addPosition(step2_pos);
	return wf;
}

}
