package eu.dm2e.ws.worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum XsltExecutorService {
	INSTANCE;
	
	private ExecutorService executor;
	private XsltExecutorService() {
		executor = Executors.newCachedThreadPool();
	}
			
	public void handleJobUri(String jobUri) {
		executor.submit(new XsltWorker(jobUri));
	}

}
