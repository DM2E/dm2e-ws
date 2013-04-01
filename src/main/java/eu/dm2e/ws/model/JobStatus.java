package eu.dm2e.ws.model;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.worker.AbstractWorker;

public class JobStatus {
	
	private WebResource jobStatusResource;
	
	public JobStatus(Client client, String jobUri) {
		this.jobStatusResource = client.resource(jobUri + "/status");
	}
	public JobStatus(AbstractWorker worker, String jobUri) {
		this(worker.getClient(), jobUri);
	}
	
	public void failed() { jobStatusResource.put(JobStatusConstants.FAILED.toString()); }
	public void not_started() { jobStatusResource.put(JobStatusConstants.NOT_STARTED.toString()); }
	public void started() { jobStatusResource.put(JobStatusConstants.STARTED.toString()); }
	public void waiting() { jobStatusResource.put(JobStatusConstants.WAITING.toString()); }
	public void finished() { jobStatusResource.put(JobStatusConstants.FINISHED.toString()); }

}
