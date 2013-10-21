package eu.dm2e.ws.tests.integration.services;

import eu.dm2e.ws.tests.integration.services.file.FileServiceITCase;
import eu.dm2e.ws.tests.integration.services.workflow.WorkflowServiceITCase;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.dm2e.ws.tests.integration.services.data.ConfigServiceITCase;
import eu.dm2e.ws.tests.integration.services.job.JobServiceITCase;


@RunWith(Suite.class)
@SuiteClasses({
	FileServiceITCase.class,
	JobServiceITCase.class,
	ConfigServiceITCase.class,
	WorkflowServiceITCase.class,
	ClientITCase.class })
public class CoreServicesTestSuite {

}
