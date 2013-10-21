package eu.dm2e.ws.tests.unit.api;

//import static org.junit.Assert.*;

import eu.dm2e.ws.api.WorkflowJobPojo;
import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.tests.OmnomUnitTest;

public class WorkflowJobPojoTest extends OmnomUnitTest {

	@Before
	public void setUp() {
	}
	
	@Test
	public void testWorkflowJob() {
		WorkflowJobPojo wfj = new WorkflowJobPojo();
		wfj.info("foo");
		wfj.setFailed();
		log.info(wfj.getJobStatus());
		log.info(wfj.getTurtle());
	}

//	@Test
//	public void testGetSlotAssignments() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetSlotAssignments() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetId() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetId() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testPublishToService() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAbstractJobPojo() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAddLogEntryLogEntryPojo() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAddLogEntryStringString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testTraceString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testDebugString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testInfo() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testWarn() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testFatalString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testTraceThrowable() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testDebugThrowable() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testFatalThrowable() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetLogEntriesLogLevelLogLevel() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetLogEntriesStringString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetLogEntriesLogLevel() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetLogEntriesString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetLogEntriesSortedByDateLogLevelLogLevel() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetLogEntriesSortedByDateStringString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testToLogString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testToLogStringStringString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetStatusJobStatus() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetStarted() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetFinished() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetFailed() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testIsFinished() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testIsFailed() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testIsStarted() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testPublishJobStatus() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testPublishLogEntry() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetStatus() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetStatusString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetLogEntries() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetLogEntries() {
//		fail("Not yet implemented");
//	}

}
