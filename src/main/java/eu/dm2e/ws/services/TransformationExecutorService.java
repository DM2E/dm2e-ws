package eu.dm2e.ws.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum TransformationExecutorService {
	INSTANCE;

	private ExecutorService executor;
	private TransformationExecutorService() {
		executor = Executors.newCachedThreadPool();
	}
			
	public void handleJob(AbstractTransformationService runner) {
		executor.submit(runner);
    }


}
