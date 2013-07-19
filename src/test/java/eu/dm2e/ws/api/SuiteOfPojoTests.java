package eu.dm2e.ws.api;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.dm2e.ws.services.file.FilePojoTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	JobPojoTest.class,
	LogEntryPojoTest.class,
	ParameterPojoTest.class,
	SerializablePojoTest.class,
	FilePojoTest.class,
	UserPojoTest.class,
	WebserviceConfigPojoTest.class,
	WebservicePojoTest.class,
	WorkflowConfigPojoTest.class,
	WorkflowJobPojoTest.class,
	WorkflowPojoTest.class })
public class SuiteOfPojoTests {

}
