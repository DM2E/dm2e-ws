package eu.dm2e.ws.tests.unit.api;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	JobPojoTest.class,
	LogEntryPojoTest.class,
	ParameterPojoTest.class,
	FilePojoTest.class,
	UserPojoTest.class,
	WebserviceConfigPojoTest.class,
	WebservicePojoTest.class,
	WorkflowPojoTest.class })
public class SuiteOfPojoTests {

}
