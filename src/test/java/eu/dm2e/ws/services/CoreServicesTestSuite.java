package eu.dm2e.ws.services;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.dm2e.ws.services.data.ConfigServiceITCase;
import eu.dm2e.ws.services.file.FileServiceITCase;
import eu.dm2e.ws.services.job.JobServiceITCase;
import eu.dm2e.ws.services.workflow.WorkflowITCase;


@RunWith(Suite.class)
@SuiteClasses({
	FileServiceITCase.class,
	JobServiceITCase.class,
	ConfigServiceITCase.class,
	WorkflowITCase.class,
	ClientITCase.class })
public class CoreServicesTestSuite {

}
