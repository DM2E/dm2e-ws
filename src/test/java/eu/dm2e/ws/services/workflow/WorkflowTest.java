package eu.dm2e.ws.services.workflow;

import java.util.logging.Logger;

import org.junit.Test;

import eu.dm2e.ws.api.ParameterSlotPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.publish.PublishServiceTest;
import eu.dm2e.ws.services.xslt.XsltServiceTest;

public class WorkflowTest {
	
	Logger log = Logger.getLogger(getClass().getName());

	@Test
	public void test() {
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
		
		// Position 1 is the XSLT service
		WorkflowPositionPojo position1 = new WorkflowPositionPojo();
		position1.setWebService(ws_xslt);
		wf.getPositions().add(position1);
		
		// Position 2 is the Publish service
		WorkflowPositionPojo position2 = new WorkflowPositionPojo();
		position2.setWebService(ws_publish);
		wf.getPositions().add(position2);
		
		// Service Slots (defining the flow of data through the positions)
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
		
		// Workflow Slots
		ParameterSlotPojo slot_0_xslt_in = new ParameterSlotPojo();
		slot_0_xslt_in.setForPosition(position1);
		slot_0_xslt_in.setForParam(ws_xslt.getParamByName("xsltInParam"));
		ParameterSlotPojo slot_0_xml_in = new ParameterSlotPojo();
		slot_0_xml_in.setForPosition(position1);
		slot_0_xml_in.setForParam(ws_xslt.getParamByName("xmlInParam"));
		ParameterSlotPojo slot_0_endpoint = new ParameterSlotPojo();
		slot_0_endpoint.setForPosition(position2);
		slot_0_endpoint.setForParam(ws_publish.getParamByName("targetEndpointParam"));
		ParameterSlotPojo slot_0_graph = new ParameterSlotPojo();
		slot_0_graph.setForPosition(position2);
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
		g.addObject(wf);
//		slot1.setWebService("http://foo");
//		log.info(g.getCanonicalNTriples());
		log.info(g.getTurtle());
//		wf.addParameter(new ParameterPojo("foo", "bar"));
//		wf.addSlot(slot1);
//		System.out.println(wf);
	}

}
