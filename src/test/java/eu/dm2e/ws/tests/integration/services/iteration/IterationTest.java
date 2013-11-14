package eu.dm2e.ws.tests.integration.services.iteration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
import eu.dm2e.ws.services.Client;
import eu.dm2e.ws.services.demo.DemoService;
import eu.dm2e.ws.services.demo.IteratorService;
import eu.dm2e.ws.tests.OmnomTestCase;


//def static doit() {

public class IterationTest extends OmnomTestCase {
	
	@Test
	public void doit() throws IOException, InterruptedException {
		
		Logger log = LoggerFactory.getLogger(getClass().getName());
		String base = "http://localhost:9998/api/";
		String demoUrl = base + "service/demo";
		String iteratorUrl = base + "service/iterator";
		String workflowServiceUri = base + "workflow";

		WebservicePojo demo = new DemoService().getWebServicePojo();
		WebservicePojo iterator = new IteratorService().getWebServicePojo();;

		WorkflowPojo wf = new WorkflowPojo();
		wf.setLabel("Iterator Workflow");
		WorkflowPositionPojo iterPos = wf.addPosition(iterator);
		WorkflowPositionPojo demoPos = wf.addPosition(demo);
		log.debug(iterPos.getWebservice().getTerseTurtle());
		log.debug("FROM " + iterPos);
		log.debug("    " + IteratorService.PARAM_PHRASE);
		log.debug("TO  " + demoPos);
		log.debug("    " + DemoService.PARAM_COUNTDOWN_PHRASE); 
		wf.addConnectorFromPositionToPosition(
				iterPos,
				IteratorService.PARAM_PHRASE,
				demoPos,
				DemoService.PARAM_COUNTDOWN_PHRASE);
		wf.autowire();

		log.debug(wf.getTerseTurtle());
		String x = wf.publishToService(workflowServiceUri);
		wf.loadFromURI(wf.getId());
		log.debug(wf.getTerseTurtle());

		File file = new File("iterator.dot");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(wf.getFullDot().getBytes());
		fos.close();

		WebservicePojo wfService = wf.getWebservice();
		WebserviceConfigPojo config = wfService.createConfig();
		Client client = new Client();
		log.debug(Config.get(ConfigProp.CONFIG_BASEURI));
		client.publishPojoToConfigService(config);

		log.info("Configuration created for Test: " + config.getTurtle());

		Response response = client.target(wfService.getId()).request().put(Entity.text(config.getId()));

		log.info("JOB STARTED WITH RESPONSE: " + response.getStatus() + " / Location: "
				+ response.getLocation() + " / Content: " + response.readEntity(String.class));

		JobPojo job = new JobPojo();
		job.loadFromURI(response.getLocation());
		while (job.isStillRunning()) {
			Thread.sleep(2000);
			job.refresh(0, true);
			log.debug(job.toLogString());
		}
		log.debug(job.toLogString());
	}

}
