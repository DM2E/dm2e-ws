package eu.dm2e.ws.api;



import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.OmnomTestCase;

@SuppressWarnings("unused")
public class WorkflowConfigPojoTest extends OmnomTestCase {
	
	private WorkflowPojo wf;
	private WorkflowConfigPojo wfconf;
	private String
		wfconfId = "http://quux.bzr/bar",
		wfconfLabel = "some workflow conf yo.";
	
	@Before
	public void setUp() {
		wf = new WorkflowPojo();
		wfconf = new WorkflowConfigPojo();
		wfconf.setId(wfconfId);
//		wfconf.setWorkflow(null)
	}
	
	@Test
	public void testJson() {
		
	}

}
