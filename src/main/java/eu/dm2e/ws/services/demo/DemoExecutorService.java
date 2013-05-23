package eu.dm2e.ws.services.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.dm2e.ws.api.JobPojo;

public enum DemoExecutorService {
	INSTANCE;
	
	private ExecutorService executor;
	private DemoExecutorService() {
		executor = Executors.newCachedThreadPool();
	}
			
	public void handleJob(JobPojo job) {
		executor.submit(new DemoWorker(job));
	}

}
