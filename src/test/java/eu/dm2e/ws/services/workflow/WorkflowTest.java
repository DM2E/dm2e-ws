package eu.dm2e.ws.services.workflow;

import eu.dm2e.ws.api.*;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.publish.PublishServiceTest;
import eu.dm2e.ws.services.xslt.XsltServiceTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

public class WorkflowTest {

	private Logger log = Logger.getLogger(getClass().getName());

	@Test
	public void test() {
//		WorkflowPojo wf = getWorkflow();
		Assert.assertTrue(true);
	}

	@Test
	public void testWorkflowJon() {
		WorkflowPojo wf = getWorkflow();

		
		ParameterPojo xml_in, xslt_in, endpoint, graph;
		xslt_in = XsltServiceTest.getWebService().getParamByName("xsltInParam"); 
		xml_in  = XsltServiceTest.getWebService().getParamByName("xmlInParam"); 
		endpoint = PublishServiceTest.getWebService().getParamByName("targetEndpointParam"); 
		graph  = PublishServiceTest.getWebService().getParamByName("targetGraphParam"); 
		ParameterSlotPojo xml_in_slot, xslt_in_slot, endpoint_slot, graph_slot;
		// Position 0 holds the workflow itself
		xml_in_slot = wf.getSlotForPositionIndexAndParam(0, xml_in);
		xslt_in_slot = wf.getSlotForPositionIndexAndParam(0, xslt_in);
		endpoint_slot = wf.getSlotForPositionIndexAndParam(0, endpoint);
		graph_slot = wf.getSlotForPositionIndexAndParam(0, graph);
		ParameterSlotAssignmentPojo
			xml_in_slot_ass = new ParameterSlotAssignmentPojo(),
			xslt_in_slot_ass = new ParameterSlotAssignmentPojo(),
			endpoint_slot_ass = new ParameterSlotAssignmentPojo(),
			graph_slot_ass = new ParameterSlotAssignmentPojo();

		xml_in_slot_ass.setForSlot(xml_in_slot);
		xml_in_slot_ass.setParameterValue("http://foo/bar/input.xml");
		xslt_in_slot_ass.setForSlot(xslt_in_slot);
		xslt_in_slot_ass.setParameterValue("http://foo/bar/input.xsl");
		xslt_in_slot = wf.getSlotForPositionIndexAndParam(0, xslt_in);
		graph_slot_ass.setForSlot(graph_slot);
		graph_slot_ass.setParameterValue("urn:SOMEGRAPH");
		endpoint_slot_ass.setForSlot(endpoint_slot);
		endpoint_slot_ass.setParameterValue("http://SOMEENDPOINT");
//		endpoint_slot = wf.getSlotForPositionIndexAndParam(0, endpoint);
//		graph_slot = wf.getSlotForPositionIndexAndParam(0, graph);
//		ParameterSlotAssignmentPojo ass1 = new ParameterSlotAssignmentPojo();
//		ParameterSlotPojo x = wf.getSlotForPositionIndexAndParam(0,
		
		WorkflowJobPojo wfJob = new WorkflowJobPojo();
		wfJob.setWebService(wf);	
		wfJob.getSlotAssignments().add(xml_in_slot_ass);
		wfJob.getSlotAssignments().add(xslt_in_slot_ass);
		wfJob.getSlotAssignments().add(endpoint_slot_ass);
		wfJob.getSlotAssignments().add(graph_slot_ass);
		
		GrafeoImpl g = new GrafeoImpl();
		g.getObjectMapper().addObject(wfJob);
		log.info(g.getTurtle());
//		log.info("" + x);
		// ass1.setForSlot(
		// wfJob.getSlotAssignments().add()
	}

	public WorkflowPojo getWorkflow() {
		// Workflow
		// ---------
		WorkflowPojo wf = new WorkflowPojo();
		wf.setId("workflow_1");

		// WebServices
		// -----------
		WebservicePojo ws_xslt = XsltServiceTest.getWebService();
		WebservicePojo ws_publish = PublishServiceTest.getWebService();

		// Positions
		// ---------
		// Position 0 holds the workflow itself
		WorkflowPositionPojo position0 = new WorkflowPositionPojo();
		position0.setWebService(wf);
		wf.getPositions().add(position0);

		// Position 1 is the XSLT service
		WorkflowPositionPojo position1 = new WorkflowPositionPojo();
		position1.setWebService(ws_xslt);
		wf.getPositions().add(position1);

		// Position 2 is the Publish service
		WorkflowPositionPojo position2 = new WorkflowPositionPojo();
		position2.setWebService(ws_publish);
		wf.getPositions().add(position2);

		// Service Slots (defining the flow of data through the positions)
		// -------------
		ParameterSlotPojo slot_1_xslt_in = new ParameterSlotPojo();
		slot_1_xslt_in.setForPosition(position1);
		slot_1_xslt_in.setForParam(ws_xslt.getParamByName("xsltInParam"));
		ParameterSlotPojo slot_1_xml_in = new ParameterSlotPojo();
		slot_1_xml_in.setForPosition(position1);
		slot_1_xml_in.setForParam(ws_xslt.getParamByName("xmlInParam"));
		ParameterSlotPojo slot_1_xml_out = new ParameterSlotPojo();
		slot_1_xml_out.setForPosition(position1);
		slot_1_xml_out.setForParam(ws_xslt.getParamByName("xmlOutParam"));
		ParameterSlotPojo slot_2_endpoint = new ParameterSlotPojo();
		slot_2_endpoint.setForPosition(position2);
		slot_2_endpoint.setForParam(ws_publish.getParamByName("targetEndpointParam"));
		ParameterSlotPojo slot_2_graph = new ParameterSlotPojo();
		slot_2_graph.setForPosition(position2);
		slot_2_graph.setForParam(ws_publish.getParamByName("targetGraphParam"));
		ParameterSlotPojo slot_2_rdfin = new ParameterSlotPojo();
		slot_2_rdfin.setForPosition(position2);
		slot_2_rdfin.setForParam(ws_publish.getParamByName("rdfInputParam"));
		wf.getParameterSlots().add(slot_1_xml_in);
		wf.getParameterSlots().add(slot_1_xml_out);
		wf.getParameterSlots().add(slot_1_xslt_in);
		wf.getParameterSlots().add(slot_2_endpoint);
		wf.getParameterSlots().add(slot_2_graph);
		wf.getParameterSlots().add(slot_2_rdfin);
		slot_1_xml_out.setConnectedSlot(slot_2_rdfin);

		// Workflow Parameters and Slots
		// ----------------
		ParameterSlotPojo slot_0_xslt_in = new ParameterSlotPojo();
		slot_0_xslt_in.setForPosition(position0);
		slot_0_xslt_in.setForParam(ws_xslt.getParamByName("xsltInParam"));
		ParameterSlotPojo slot_0_xml_in = new ParameterSlotPojo();
		slot_0_xml_in.setForPosition(position0);
		slot_0_xml_in.setForParam(ws_xslt.getParamByName("xmlInParam"));
		ParameterSlotPojo slot_0_endpoint = new ParameterSlotPojo();
		slot_0_endpoint.setForPosition(position0);
		slot_0_endpoint.setForParam(ws_publish.getParamByName("targetEndpointParam"));
		ParameterSlotPojo slot_0_graph = new ParameterSlotPojo();
		slot_0_graph.setForPosition(position0);
		slot_0_graph.setForParam(ws_publish.getParamByName("targetGraphParam"));
		wf.getParameterSlots().add(slot_0_xml_in);
		wf.getParameterSlots().add(slot_0_xslt_in);
		wf.getParameterSlots().add(slot_0_endpoint);
		wf.getParameterSlots().add(slot_0_graph);
		slot_0_xml_in.setConnectedSlot(slot_1_xml_in);
		slot_0_xslt_in.setConnectedSlot(slot_1_xslt_in);
		slot_0_endpoint.setConnectedSlot(slot_2_endpoint);
		slot_0_graph.setConnectedSlot(slot_2_graph);

		GrafeoImpl g = new GrafeoImpl();
		g.getObjectMapper().addObject(wf);
		// slot1.setWebService("http://foo");
		// log.info(g.getCanonicalNTriples());
		log.info(g.getTurtle());
		return wf;
		// wf.addParameter(new ParameterPojo("foo", "bar"));
		// wf.addSlot(slot1);
		// System.out.println(wf);
	}

}
