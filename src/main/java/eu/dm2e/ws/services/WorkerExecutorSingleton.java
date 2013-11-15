package eu.dm2e.ws.services;

import eu.dm2e.ws.api.JobPojo;

import java.util.HashMap;
import java.util.Map;
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
    Map<String,JobPojo> jobs = new HashMap<>();
    Map<String,Runnable> threads = new HashMap<>();

    private WorkerExecutorSingleton() {
		executor = Executors.newCachedThreadPool();
	}

    public void addJobPojo(String uuid, JobPojo pojo) {
        jobs.put(uuid,pojo);
    }

    public JobPojo getJob(String uuid) {
        return jobs.get(uuid);
    }

	public void handleJob(String uuid, Runnable runner) {
		threads.put(uuid,runner);
        executor.submit(runner);
    }


}
