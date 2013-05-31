package eu.dm2e.ws;

public enum OmnomTestResources {
	DEMO_JOB("/job/demo_job.ttl"),
	DEMO_LOG("/job/log_entry.ttl"),
	DEMO_LOG_WITH_URI("/job/log_entry_uri.ttl");
	
	final String path;
	OmnomTestResources(String path) { this.path = path; }
	public String getPath() { return path; }
}