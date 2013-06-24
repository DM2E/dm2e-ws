package eu.dm2e.ws.services.workflow;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowConfigPojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.junit.GrafeoAssert;
import eu.dm2e.ws.services.publish.PublishService;
import eu.dm2e.ws.services.xslt.XsltService;

public class WorkflowITCase extends OmnomTestCase {

	private Logger log = Logger.getLogger(getClass().getName());
	private WorkflowPojo wf = null;
	String _ws_label = "XML -> XMLRDF -> DM2E yay";
	String _ws_param_provider = "providerID";
	String _ws_param_xmlinput = "inputXML";
	String _ws_param_xsltinput = "inputXSLT";
	String _ws_param_outgraph = "outputGraph";
	String _ws_param_datasetLabel = "datasetLabel";
	String _ws_pos1_label = "XML -> XMLRDF";
	String _ws_pos2_label = "XMLRDF -> Graphstore";
	String _ws_param_datasetID = "datasetID";

	@Before
	public void setUp()
			throws Exception {
		wf = createWorkflow();
		Assert.assertNotNull(wf);
		publishWorkflow();
		Assert.assertNotNull(wf.getId());
	}

	@Test
	public void testGetWorkflow() {
		// ClientResponse resp2 =
		// client.resource(wf.getId()).get(ClientResponse.class);
		// GrafeoImpl g = new GrafeoImpl(resp2.getEntityInputStream());
		// GrafeoAssert.graphsAreStructurallyEquivalent(g, wf.getGrafeo());
		// log.info(wf.getTerseTurtle());

		// WorkflowPojo wf_got =
		// g.getObjectMapper().getObject(WorkflowPojo.class, wf.getId());
		// log.info("" + wf.getGrafeo().listStatements(g.createBlank(), null,
		// null));

		// Grafeo gWas_skolem = wf.getGrafeo();
		// gWas_skolem.unskolemize();
		// Grafeo gGot_skolem = wf_got.getGrafeo();
		// gGot_skolem.unskolemize();
		// try {
		// gGot_skolem.visualizeWithGraphviz("wf-got.svg");
		// gWas_skolem.visualizeWithGraphviz("wf-was.svg");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// log.info(gWas_skolem.getTerseTurtle());
		// GrafeoAssert.sizeEquals(gWas_skolem, gGot_skolem);
		// GrafeoAssert.graphsAreStructurallyEquivalent(gWas_skolem,
		// gGot_skolem);

		// Assert.assertEquals("No more blank nodes", 0,
		// g.listAnonStatements(null, null).size() );
	}

	@Test
	public void testValidate()
			throws IOException {
		{
			WorkflowConfigPojo wfconf = new WorkflowConfigPojo();
			wfconf.setWorkflow(wf);
			wfconf.addParameterAssignment(_ws_param_xmlinput, "foo");
			wfconf.addParameterAssignment(_ws_param_xsltinput, "foo");
			wfconf.addParameterAssignment(_ws_param_datasetLabel, "A fascinating dataset indeed.");
			wfconf.addParameterAssignment(_ws_param_datasetID, "dataset-1234");
			wfconf.addParameterAssignment(_ws_param_provider, "onb");
			FileUtils.writeStringToFile(new File("workflow.test.ttl"), wfconf.getTurtle());
//			log.info(wfconf.getTurtle());
			try {
				wfconf.validate();
			} catch (AssertionError e) {
				Assert.fail("Invalid Workflow" + e);
			}
		}
	}

	/**
	 * Workflow goes like this: - Transform XML to RDF/XML using XSLT - Publish
	 * RDF/XML
	 * 
	 * @return
	 * @throws Exception
	 */
	public String publishWorkflow()
			throws Exception {
		// FileUtils.writeStringToFile(new File("SHOULD.ttl"), wf.getTurtle());
		log.info("testsimple: Publishing the workflow.");

		Grafeo gBefore = wf.getGrafeo();
		assertNull(wf.getId());

		Assert.assertEquals(1, wf
			.getGrafeo()
			.listStatements(null, "omnom:hasPosition", null)
			.size());

		String respStr = wf.publishToService(client.getWorkflowWebResource());
		
		assertNotNull(wf.getId());

		Assert.assertEquals(1, wf
			.getGrafeo()
			.listStatements(null, "omnom:hasPosition", null)
			.size());

		Grafeo gAfter = wf.getGrafeo();

		log.info(gAfter.getTerseTurtle());
		GrafeoAssert.sizeEquals(gBefore, gAfter);
		GrafeoAssert.graphsAreNotEquivalent(gBefore, gAfter);
		GrafeoAssert.graphsAreStructurallyEquivalent(gBefore, gAfter);
		// log.info("testsimple: DONE Publishing the workflow.");
		{
			ClientResponse resp = client.resource(respStr).accept(DM2E_MediaType.TEXT_TURTLE).get(
					ClientResponse.class);
			Assert.assertEquals(200, resp.getStatus());
			String respStr2 = resp.getEntity(String.class);
			log.info("And here is the answer: \n" + respStr2);
		}
		// wf.loadFromURI(wf.getId());
		// log.info(wf.getTurtle());
		// log.info(x);

		// WorkflowConfigPojo wfconf = new WorkflowConfigPojo();
		// wfconf.setId("WORKFLOW_CONFIG1");
		// wfconf.setWorkflow(wf);
		// log.info(wfconf.getTurtle());
		// try {
		// wfconf.getGrafeo().visualizeWithGraphviz();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		try {
			wf.getGrafeo().visualizeWithGraphviz("output.svg");
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

		wf.setLabel(_ws_label);
		wf.addInputParameter(_ws_param_xmlinput);
		wf.addInputParameter(_ws_param_provider);
		wf.addInputParameter(_ws_param_xsltinput);
		wf.addOutputParameter(_ws_param_outgraph);
		wf.addInputParameter(_ws_param_datasetLabel);
		wf.addInputParameter(_ws_param_datasetID);

		WorkflowPositionPojo step1_pos = new WorkflowPositionPojo();
		WebservicePojo step1_ws = new XsltService().getWebServicePojo();
		WebserviceConfigPojo step1_config = new WebserviceConfigPojo();
		step1_config.setWebservice(step1_ws);
		step1_pos.setWebserviceConfig(step1_config);
		step1_pos.setLabel(_ws_pos1_label);
		step1_pos.setWorkflow(wf);
		wf.addPosition(step1_pos);


		WorkflowPositionPojo step2_pos = new WorkflowPositionPojo();
		WebservicePojo step2_ws = new PublishService().getWebServicePojo();
		WebserviceConfigPojo step2_config = new WebserviceConfigPojo();
		step2_config.setWebservice(step2_ws);
		step2_pos.setWebserviceConfig(step2_config);
		step2_pos.setLabel(_ws_pos2_label);
		step2_pos.setWorkflow(wf);
		wf.addPosition(step2_pos);

		// workflow:inputXML => xmlrdf:xmlinput
		wf.addConnectorFromWorkflowToPosition(
				_ws_param_xmlinput, 
				step1_pos, 
				XsltService.PARAM_XML_IN);

		// workflow:inputXSLT => xmlrdf:xsltinput
		wf.addConnectorFromWorkflowToPosition(
				_ws_param_xsltinput,
				step1_pos,
				XsltService.PARAM_XSLT_IN);
		
		// workflow:datasetID => publish:dataset-id
		wf.addConnectorFromWorkflowToPosition(
				_ws_param_datasetID,
				step2_pos, 
				PublishService.PARAM_DATASET_ID);

		// workflow:providerID => publish:providerID
		wf.addConnectorFromWorkflowToPosition(
				_ws_param_provider,
				step2_pos,
				PublishService.PARAM_PROVIDER_ID);

		// xmlrdf:xmloutput => publish:to-publish
		wf.addConnectorFromPositionToPosition(
				step1_pos,
				XsltService.PARAM_XML_OUT,
				step2_pos,
				PublishService.PARAM_TO_PUBLISH);

		// workflow:label => publish:label
		wf.addConnectorFromWorkflowToPosition(
				_ws_param_datasetLabel,
				step2_pos,
				PublishService.PARAM_LABEL);

		// publish:result-dataset-id => workfow:outputGraph
		wf.addConnectorFromPositionToWorkflow(
				step2_pos,
				PublishService.PARAM_RESULT_DATASET_ID,
				_ws_param_outgraph);

		return wf;
	}
	
	@Test
	public void testRunWorkflow() {
		
		WorkflowConfigPojo wfconf = new WorkflowConfigPojo();
		
//		wfconf.addParameterAssignment(wf.getParamByName(URI_BASE), URI_BASE)
		
		
	}

}
