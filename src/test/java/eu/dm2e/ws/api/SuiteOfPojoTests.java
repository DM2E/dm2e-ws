package eu.dm2e.ws.api;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	JobPojoTest.class,
	LogEntryPojoTest.class,
	ParameterPojoTest.class,
	SerializablePojoITCase.class,
	WebserviceConfigPojoTest.class,
	WebservicePojoTest.class,
	WorkflowConfigPojoTest.class,
	WorkflowJobPojoITCase.class,
	WorkflowJobPojoTest.class,
	WorkflowPojoTest.class })
public class SuiteOfPojoTests {

}
