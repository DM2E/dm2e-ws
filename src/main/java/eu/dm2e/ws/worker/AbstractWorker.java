package eu.dm2e.ws.worker; 
import java.util.Iterator;
import java.util.List;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.model.JobLogger;

/**
 * Abstract Base class of all workers
 * @author kb
 *
 */
public abstract class AbstractWorker implements Runnable {
	
	protected JobPojo job;
	
	public AbstractWorker(JobPojo job){
		this.job = job;
	}
	
//	private AbstractWorker() { 
//		// A worker must always be connected to exactly one job
//	}

	/**
	 * Reacts to a message sent to the worker by interpreting it as a run
	 * configuration and doing its thing.
	 * 
	 * @param message
	 *            The message sent to the worker
	 * @throws InterruptedException
	 */
	public abstract void run();
	
//	public abstract String getServiceUri();
	
	// The HTTP REST client
	protected Client client = new Client();
	public Client getClient() { return client; }
	
//	public abstract String getRabbitQueueName();

	protected void ensureResourcesReady(List<WebResource> unreadyResources, JobLogger log) throws Throwable {
		Iterator<WebResource> resIter = unreadyResources.iterator();
		while (resIter.hasNext()) {
			WebResource r = resIter.next();
			if (!r.getURI().getScheme().matches("^(h|f)ttps?")) {
				throw new Exception("Not an http/ftp link: " + r.getURI().getScheme());
			}
			log.fine("Polling HEAD on " + r.getURI());
			// TODO think about a good strategy to do this
			ClientResponse resp = r.head();
			if (resp.getStatus() == 200) {
				log.fine("Resource " + r.getURI() + " is ready now.");
				resIter.remove();
			} else {
				throw new Exception("Resource " + r.getURI() + " not available. Will croak for now.");
			}
		}
	}


	public void waitForResources(List<WebResource> unreadyResources) {
	}
	
	/*******************
	 * GETTERS/SETTERS
	 *******************/

	public JobPojo getJob() { return job; }

	public void setJob(JobPojo job) { this.job = job; }
}