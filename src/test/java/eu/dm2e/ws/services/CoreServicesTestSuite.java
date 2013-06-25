package eu.dm2e.ws.services;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.dm2e.ws.services.data.WebServiceConfigServiceITCase;
import eu.dm2e.ws.services.file.FileServiceITCase;
import eu.dm2e.ws.services.file.NewFileServiceITCase;
import eu.dm2e.ws.services.job.JobServiceITCase;
import eu.dm2e.ws.services.workflow.WorkflowServiceITCase;


@RunWith(Suite.class)
@SuiteClasses({
	FileServiceITCase.class,
	NewFileServiceITCase.class,
	JobServiceITCase.class,
	WebServiceConfigServiceITCase.class,
	WorkflowServiceITCase.class,
	ClientITCase.class })
public class CoreServicesTestSuite {

}
