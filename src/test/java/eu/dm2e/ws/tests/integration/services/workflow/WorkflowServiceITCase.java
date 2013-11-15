package eu.dm2e.ws.tests.integration.services.workflow;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.grafeo.junit.GrafeoAssert;
import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.*;
import eu.dm2e.ws.services.demo.DemoService;
import eu.dm2e.ws.services.publish.PublishService;
import eu.dm2e.ws.services.workflow.WorkflowService;
import eu.dm2e.ws.services.xslt.XsltService;
import eu.dm2e.ws.tests.OmnomTestCase;
import eu.dm2e.ws.tests.OmnomTestResources;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class WorkflowServiceITCase extends OmnomTestCase {

	private Logger log = LoggerFactory.getLogger(getClass().getName());
	private WorkflowPojo xsltWorkflow = null;
	private WorkflowPojo simpleWorkflow = null;
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

		_file_xml_in = client.publishFile(configFile.get(OmnomTestResources.METS_EXAMPLES));
		_file_xslt_in = client.publishFile(configFile.get(OmnomTestResources.METS2EDM));

		xsltWorkflow = createWorkflow();
		Assert.assertNotNull(xsltWorkflow);
		testGetWebserviceDescription();
		publishWorkflow();

		simpleWorkflow = createSimpleWorkflow();
		Assert.assertNotNull(xsltWorkflow.getId());
		publishSimpleWorkflow();
	}

	private void testGetWebserviceDescription() {
		Response resp = client.getWorkflowWebTarget().request().get();
		Assert.assertEquals(200, resp.getStatus());
	}

//	@Test
//	public void testGetWorkflow() {
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
//	}

	@Test
	public void testValidate()
			throws Exception {
		{
			WebserviceConfigPojo wfconf = new WebserviceConfigPojo();
            wfconf.setWebservice(xsltWorkflow.getWebservice());
            assertNotNull(wfconf.getWebservice());
			wfconf.addParameterAssignment(_ws_param_xmlinput, _file_xml_in);
			wfconf.addParameterAssignment(_ws_param_xsltinput, _file_xslt_in);
			wfconf.addParameterAssignment(_ws_param_datasetLabel, "A fascinating dataset indeed.");
			wfconf.addParameterAssignment(_ws_param_datasetID, "dataset-1234");
			wfconf.addParameterAssignment(_ws_param_provider, "onb");
			FileUtils.writeStringToFile(new File("workflow.test.ttl"), wfconf.getTurtle());
//			log.info(wfconf.getTurtle());
			wfconf.validate();
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

		GrafeoImpl gBefore = (GrafeoImpl) xsltWorkflow.getGrafeo();
		assertNull(xsltWorkflow.getId());

		Assert.assertEquals(1, xsltWorkflow .getGrafeo() .listStatements(null, NS.OMNOM.PROP_WORKFLOW_POSITION, null) .size());

		String respStr = null;
		try {
			respStr = xsltWorkflow.publishToService(client.getWorkflowWebTarget());
		} catch (Exception e1) {
			log.error("FAILOR" + e1);
			throw new RuntimeException(e1);
		}
		
		assertNotNull(xsltWorkflow.getId());

		Assert.assertEquals(1, xsltWorkflow.getGrafeo().listStatements(null, NS.OMNOM.PROP_WORKFLOW_POSITION, null).size());
		
		for (WorkflowPositionPojo pos : xsltWorkflow.getPositions()) {
			assertNotNull(pos.getId());
		}

		Grafeo gAfter = xsltWorkflow.getGrafeo();
		Grafeo gParsed = new GrafeoImpl();
		gParsed.load(respStr);

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
		// make sure the label is pertained
		Assert.assertEquals(1, gAfter.listStatements(gAfter.resource(xsltWorkflow.getId()), NS.RDFS.PROP_LABEL, null).size());
		Assert.assertEquals(1, gParsed.listStatements(gParsed.resource(xsltWorkflow.getId()), NS.RDFS.PROP_LABEL, null).size());
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
			xsltWorkflow.getGrafeo().visualizeWithGraphviz("output.svg");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return xsltWorkflow.getId();

	}

	/**
	 * @return
	 * @throws IOException 
	 */
	protected WorkflowPojo createWorkflow() throws IOException {
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
//		WebserviceConfigPojo step1_config = new WebserviceConfigPojo();
//		step1_config.setWebservice(step1_ws);
		step1_pos.setWebservice(step1_ws);
		step1_pos.setLabel(_ws_pos1_label);
		step1_pos.setWorkflow(wf);
		wf.addPosition(step1_pos);


		WorkflowPositionPojo step2_pos = new WorkflowPositionPojo();
		WebservicePojo step2_ws = new PublishService().getWebServicePojo();
//		WebserviceConfigPojo step2_config = new WebserviceConfigPojo();
//		step2_config.setWebservice(step2_ws);
		step2_pos.setWebservice(step2_ws);
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
		log.debug(LogbackMarkers.DATA_DUMP, x.getTerseTurtle());

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
		
		log.debug(wf.toJson());
		return wf;
	}
	
	@Test
	@Ignore("Working but jetty in fuseki croaks on the large form post")
	public void testRunWorkflow() throws Exception {
		WebserviceConfigPojo wfconf = new WebserviceConfigPojo();
        for (WebservicePojo ws:xsltWorkflow.getWebservices()) {
            wfconf.setWebservice(ws);
            break;
        }
        assertNotNull(wfconf.getWebservice());
        log.debug(LogbackMarkers.DATA_DUMP, xsltWorkflow.getTerseTurtle());
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
		
		WebserviceConfigPojo wfconf2 = new WebserviceConfigPojo();
		wfconf2.loadFromURI(wfconf.getId());
		GrafeoAssert.graphsAreEquivalent(wfconf.getGrafeo(), wfconf2.getGrafeo());
		
		log.info("RUNNING WORKFLOW");
		Response resp = client
			.target(wfconf.getWebservice().getId())
			.request()
			.put(Entity.text(wfconf.getId()));
		log.info("RESPONSE FROM WORKFLOW SERVICE " + wfconf.getWebservice().getId() +": "+ resp);
		Assert.assertEquals(202, resp.getStatus());
		log.info("Location: " + resp.getLocation());
		JobPojo workflowJob = new JobPojo();
		
		int loopCount = 0;
		do {
			workflowJob.loadFromURI(resp.getLocation());
			log.info(workflowJob.toLogString());
			Thread.sleep(1000);
//			assertEquals("[Loop #" + loopCount + "] Sub-Job running.", 1, workflowJob.getRunningJobs().size());
			log.error("[Loop #" + loopCount + "] Sub-Job running: " + workflowJob.getRunningJobs());
			loopCount++;
		} while (workflowJob.isStillRunning());
		Thread.sleep(5000);
		workflowJob.loadFromURI(resp.getLocation());
//		for (JobPojo finishedJob : workflowJob.getFinishedJobs()) {
//			assertNotNull(finishedJob.getExecutesPosition());
//		}
		log.info(LogbackMarkers.DATA_DUMP, workflowJob.getTerseTurtle());
		String compLog = workflowJob.getOutputParameterValueByName(WorkflowService.PARAM_COMPLETE_LOG);
		assertNotNull(compLog);
		log.info(compLog);
		
		// System.in.read();
		
	}

	/**
	 * @return
	 * @throws IOException 
	 */
	protected WorkflowPojo createSimpleWorkflow() throws IOException {
		WorkflowPojo wf = new WorkflowPojo();

//		wf.setLabel(_ws_label);
//		wf.addInputParameter(_ws_param_xmlinput).setIsRequired(true);
//		wf.addInputParameter(_ws_param_provider).setIsRequired(true);
//		wf.addInputParameter(_ws_param_xsltinput).setIsRequired(true);
//		wf.addInputParameter(_ws_param_datasetLabel).setIsRequired(true);
//		wf.addInputParameter(_ws_param_datasetID).setIsRequired(true);
//		
//		wf.addOutputParameter(_ws_param_outgraph);

		WorkflowPositionPojo step1_pos = new WorkflowPositionPojo();
		WebservicePojo step1_ws = new DemoService().getWebServicePojo();
//		WebserviceConfigPojo step1_config = new WebserviceConfigPojo();
//		step1_config.setWebservice(step1_ws);
		step1_pos.setWebservice(step1_ws);
		step1_pos.setLabel(_ws_pos1_label);
		step1_pos.setWorkflow(wf);
		wf.addPosition(step1_pos);

		return wf;
	}
	public void publishSimpleWorkflow() {
		String respStr = null;
		try {
			respStr = simpleWorkflow.publishToService(client.getWorkflowWebTarget());
		} catch (Exception e1) {
			log.error("FAILOR" + e1);
			throw new RuntimeException(e1);
		}
	}
	@Test
	public void testRunSimpleWorkflow() throws Exception {
		WebserviceConfigPojo wfconf = new WebserviceConfigPojo();
        for (WebservicePojo ws:simpleWorkflow.getWebservices()) {
            wfconf.setWebservice(ws);
            break;
        }
        assertNotNull(wfconf.getWebservice());
		wfconf.publishToService(client.getConfigWebTarget());
		JobPojo workflowJob = new JobPojo();
		log.info("RUNNING WORKFLOW");
		Response resp = client
			.target(wfconf.getWebservice().getId())
			.request()
			.put(Entity.text(wfconf.getId()));
		log.info("RESPONSE FROM WORKFLOW SERVICE " + wfconf.getWebservice().getId() +": "+ resp.getLocation());
		Assert.assertEquals(202, resp.getStatus());
		
		int loopCount = 0;
		do {
			workflowJob.loadFromURI(resp.getLocation());
            log.info("Job status in iteration {}: {}", loopCount, workflowJob.getJobStatus());
            log.info(workflowJob.toLogString());
			assertNotNull(workflowJob.getLabel());
			Thread.sleep(1000);
//			assertEquals("[Loop #" + loopCount + "] Sub-Job running.", 1, workflowJob.getRunningJobs().size());
			log.error("[Loop #" + loopCount + "] Sub-Job running: " + workflowJob.getRunningJobs());

            for (JobPojo runningJob : workflowJob.getRunningJobs()) {
				runningJob.loadFromURI(runningJob.getId());
//				log.debug("" + runningJob.getExecutesPosition());
//				log.debug(runningJob.getTerseTurtle());
//				log.debug(runningJob.toJson());
//				log.debug("" + runningJob.getExecutesPosition());
				// FIXME why does getExecutesPosition() return null??? Serialization works correctly!
				// kb, Wed Sep 25 18:46:00 CEST 2013
//				assertNotNull(runningJob.getExecutesPosition());
			}
			loopCount++;
		} while (workflowJob.isStillRunning());
//		Thread.sleep(5000);
		workflowJob.loadFromURI(resp.getLocation());
		
		log.debug("Test related Jobs call");
		{
			Response respRelatedJobs = client
					.target(simpleWorkflow.getId() + "/relatedJobs")
					.request(MediaType.APPLICATION_JSON)
					.get();
			final String respStr = respRelatedJobs.readEntity(String.class);
			log.debug("Response body: " + respStr);
		}
//		System.in.read();
//		for (JobPojo finishedJob : workflowJob.getFinishedJobs()) {
//			finishedJob.loadFromURI(finishedJob.getId());
//			assertNotNull(runningJob.getExecutesPosition());
//		}
//		log.info(LogbackMarkers.DATA_DUMP, workflowJob.getTerseTurtle());
//		String compLog = workflowJob.getOutputParameterValueByName(WorkflowService.PARAM_COMPLETE_LOG);
//		assertNotNull(compLog);
//		log.info(compLog);
	}
	
}
