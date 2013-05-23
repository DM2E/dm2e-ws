package eu.dm2e.ws.services.xslt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.dm2e.ws.api.JobPojo;

public enum XsltExecutorService {
	INSTANCE;
	
	private ExecutorService executor;
	private XsltExecutorService() {
		executor = Executors.newCachedThreadPool();
	}
			
	public void handleJob(JobPojo job) {
		executor.submit(new XsltWorker(job));
	}

}
