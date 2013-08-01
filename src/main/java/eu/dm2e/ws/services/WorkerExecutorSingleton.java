package eu.dm2e.ws.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread Pool for running the asynchronous instances of AbstractAsynchronousRDFServices.
 *
 * @author Konstantin Baierer
 */
public enum WorkerExecutorSingleton {
	INSTANCE;

	private ExecutorService executor;
	private WorkerExecutorSingleton() {
		executor = Executors.newCachedThreadPool();
	}
			
	public void handleJob(Runnable runner) {
		executor.submit(runner);
    }


}
