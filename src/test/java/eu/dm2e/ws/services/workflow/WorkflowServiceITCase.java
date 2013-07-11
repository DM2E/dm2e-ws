package eu.dm2e.ws.services.workflow;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.api.ParameterConnectorPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowConfigPojo;
import eu.dm2e.ws.api.WorkflowJobPojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.junit.GrafeoAssert;
import eu.dm2e.ws.services.publish.PublishService;
import eu.dm2e.ws.services.xslt.XsltService;

public class WorkflowServiceITCase extends OmnomTestCase {

	private Logger log = LoggerFactory.getLogger(getClass().getName());
	private WorkflowPojo wf = null;
	final String _ws_label = "XML -> XMLRDF -> DM3E yay";
	final String _ws_param_provider = "providerID";
	final String _ws_param_xmlinput = "inputXML";
	final String _ws_param_xsltinput = "inputXSLT";
	final String _ws_param_outgraph = "outputGraph";
	final String _ws_param_datasetLabel = "datasetLabel";
	final String _ws_pos1_label = "XML -> XMLRDF";
	final String _ws_pos2_label = "XMLRDF -> Graphstore";
	final String _ws_param_datasetID = "datasetID";
	
	String _file_xml_in;
	String _file_xslt_in;

	@Before
	public void setUp()
			throws Exception {
		wf = createWorkflow();
		Assert.assertNotNull(wf);
		testGetWebserviceDescription();
		publishWorkflow();
		Assert.assertNotNull(wf.getId());
		_file_xml_in = client.publishFile(configFile.get(OmnomTestResources.METS_EXAMPLES));
		_file_xslt_in = client.publishFile(configFile.get(OmnomTestResources.METS2EDM));
	}

	private void testGetWebserviceDescription() {
		Response resp = client.getWorkflowWebTarget().request().get();
		Assert.assertEquals(200, resp.getStatus());
	}

	//@Test
	public void testGetWorkflow() {
		// Response resp2 =
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

	//@Test
	public void testValidate()
			throws IOException {
		{
			WorkflowConfigPojo wfconf = new WorkflowConfigPojo();
			wfconf.setWorkflow(wf);
			wfconf.addParameterAssignment(_ws_param_xmlinput, _file_xml_in);
			wfconf.addParameterAssignment(_ws_param_xsltinput, _file_xslt_in);
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
	public String publishWorkflow()  {
		// FileUtils.writeStringToFile(new File("SHOULD.ttl"), wf.getTurtle());
		log.info("testsimple: Publishing the workflow.");

		GrafeoImpl gBefore = (GrafeoImpl) wf.getGrafeo();
		assertNull(wf.getId());

		Assert.assertEquals(1, wf .getGrafeo() .listStatements(null, "omnom:hasPosition", null) .size());

		String respStr = null;
		try {
			respStr = wf.publishToService(client.getWorkflowWebTarget());
		} catch (Exception e1) {
			log.error("FAILOR" + e1);
			throw new RuntimeException(e1);
		}
		
		assertNotNull(wf.getId());

		Assert.assertEquals(1, wf.getGrafeo().listStatements(null, "omnom:hasPosition", null).size());
		
		for (WorkflowPositionPojo pos : wf.getPositions()) {
			assertNotNull(pos.getId());
		}

		Grafeo gAfter = wf.getGrafeo();

		log.info(gAfter.getTerseTurtle());
//		GrafeoAssert.sizeEquals(gBefore, gAfter);
		GrafeoAssert.graphContainsGraph(gAfter, gBefore);
		// log.info("testsimple: DONE Publishing the workflow.");
		{
			Response resp = client.target(respStr).request(DM2E_MediaType.TEXT_TURTLE).get();
			Assert.assertEquals(200, resp.getStatus());
			String respStr2 = resp.readEntity(String.class);
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
		wf.addInputParameter(_ws_param_xmlinput).setIsRequired(true);
		wf.addInputParameter(_ws_param_provider).setIsRequired(true);
		wf.addInputParameter(_ws_param_xsltinput).setIsRequired(true);
		wf.addInputParameter(_ws_param_datasetLabel).setIsRequired(true);
		wf.addInputParameter(_ws_param_datasetID).setIsRequired(true);
		
		wf.addOutputParameter(_ws_param_outgraph);

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
		ParameterConnectorPojo x = wf.addConnectorFromWorkflowToPosition(
				_ws_param_datasetID,
				step2_pos, 
				PublishService.PARAM_DATASET_ID);
		log.info(x.getTerseTurtle());

		// workflow:providerID => publish:providerID
		wf.addConnectorFromWorkflowToPosition(
				_ws_param_provider,
				step2_pos,
				PublishService.PARAM_PROVIDER_ID);

		// workflow:label => publish:label
		wf.addConnectorFromWorkflowToPosition(
				_ws_param_datasetLabel,
				step2_pos,
				PublishService.PARAM_LABEL);

		// xmlrdf:xmloutput => publish:to-publish
		wf.addConnectorFromPositionToPosition(
				step1_pos,
				XsltService.PARAM_XML_OUT,
				step2_pos,
				PublishService.PARAM_TO_PUBLISH);

		// publish:result-dataset-id => workfow:outputGraph
		wf.addConnectorFromPositionToWorkflow(
				step2_pos,
				PublishService.PARAM_RESULT_DATASET_ID,
				_ws_param_outgraph);

		return wf;
	}
	
	@Test
	@Ignore("Working but jetty in fuseki croaks on the large form post")
	public void testRunWorkflow() throws Exception {
		WorkflowConfigPojo wfconf = new WorkflowConfigPojo();
		wfconf.setWorkflow(wf);
		log.info(wf.getTerseTurtle());
		wfconf.addParameterAssignment(_ws_param_xmlinput, _file_xml_in);
		wfconf.addParameterAssignment(_ws_param_xsltinput, _file_xslt_in);
		wfconf.addParameterAssignment(_ws_param_datasetLabel, "A fascinating dataset indeed.");
		wfconf.addParameterAssignment(_ws_param_datasetID, "dataset-1234");
		wfconf.addParameterAssignment(_ws_param_provider, "onb");
		
		Assert.assertNull(wfconf.getId());
		log.info("<VALIDATE>");
		wfconf.validate();
		log.info("</VALIDATE>");
		wfconf.publishToService(client.getConfigWebTarget());
		log.info("<VALIDATE>");
		wfconf.validate();
		log.info("</VALIDATE>");
		Assert.assertNotNull(wfconf.getId());
		
		WorkflowConfigPojo wfconf2 = new WorkflowConfigPojo();
		wfconf2.loadFromURI(wfconf.getId());
		GrafeoAssert.graphsAreEquivalent(wfconf.getGrafeo(), wfconf2.getGrafeo());
		
		log.info("RUNNING WORKFLOW");
		Response resp = client
			.target(wf.getId())
			.request()
			.put(Entity.text(wfconf.getId()));
		log.info("RESPONSE FROM WORKFLOW " + wf.getId() +": "+ resp);
		Assert.assertEquals(202, resp.getStatus());
		log.info("Location: " + resp.getLocation());
		WorkflowJobPojo workflowJob = new WorkflowJobPojo();
		do {
			workflowJob.loadFromURI(resp.getLocation());
			log.info(workflowJob.toLogString());
			Thread.sleep(500);
		} while (workflowJob.isStillRunning());
		Thread.sleep(5000);
		workflowJob.loadFromURI(resp.getLocation());
		log.info(workflowJob.getTerseTurtle());
		String compLog = workflowJob.getOutputParameterValueByName(WorkflowService.PARAM_COMPLETE_LOG);
		assertNotNull(compLog);
		log.info(compLog);
		
		System.in.read();
		
	}
}
