package eu.dm2e.ws.services.demo;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebServiceConfigPojo;
import eu.dm2e.ws.model.JobStatusConstants;
import eu.dm2e.ws.worker.AbstractWorker;

public class DemoWorker extends AbstractWorker{

	public DemoWorker(JobPojo job) { super(job); }

	@Override
	public void run() {
		job.debug("DemoWorker starts to run now.");
		
		WebServiceConfigPojo wsConf = job.getWebServiceConfig();
		job.debug("wsConf: " + wsConf);
		
		int sleepTime = 0;
		job.debug(wsConf.getParameterValueByName("sleepTime"));
		try {
			sleepTime = Integer.parseInt(wsConf.getParameterValueByName("sleepTime"));
		} catch(Exception e) {
			job.warn("Exception occured!: " + e);
				
		}
		
		job.debug("DemoWorker will sleep for " + sleepTime + " seconds.");
		job.setStarted();
		
		// snooze
		try {
			for (int i=0 ; i < sleepTime ; i++) {
				job.debug("Still Sleeping for " + (sleepTime - i) + "seconds.");
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			job.setStatus(JobStatusConstants.FAILED);
			job.fatal(e.toString());
			return;
		}
		
		job.debug("DemoWorker is finished now.");
		job.setStatus(JobStatusConstants.FINISHED);
	}

}
