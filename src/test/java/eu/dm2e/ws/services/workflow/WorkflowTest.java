package eu.dm2e.ws.services.workflow;

import java.util.logging.Logger;

import org.junit.Test;

import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public class WorkflowTest {
	
	Logger log = Logger.getLogger(getClass().getName());

	@Test
	public void test() {
		// Workflow
		WorkflowPojo wf = new WorkflowPojo();
		wf.setId("workflow_1");
		
		// WebService
		WebservicePojo ws1 = new WebservicePojo();
		ws1.setId("http://services.dm2e.eu/xslt/");
		
		// Positions
		WorkflowPositionPojo position1 = new WorkflowPositionPojo();
		position1.setWebService(ws1);
		WorkflowPositionPojo position2 = new WorkflowPositionPojo();
		position1.setWebService(ws1);
		wf.getPositions().add(position1);
		position2.setWebService(ws1);
		wf.getPositions().add(position2);
		
		// Slots
		
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
