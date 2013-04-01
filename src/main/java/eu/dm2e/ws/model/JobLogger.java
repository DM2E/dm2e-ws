package eu.dm2e.ws.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.ws.worker.AbstractWorker;

public class JobLogger {
	
	private Logger logger;
	private String serviceUri;
	private WebResource jobLogResource;
	
	public JobLogger(AbstractWorker worker, WebResource jobLogResource) {
		this.logger = Logger.getLogger(worker.getClass().getName());
		this.serviceUri = worker.getServiceUri();
		this.jobLogResource = jobLogResource;
	}
	
	public JobLogger(AbstractWorker worker, String jobUri) {
		this.logger = Logger.getLogger(worker.getClass().getName());
		this.serviceUri = worker.getServiceUri();
		Client client = worker.getClient();
		this.jobLogResource = client.resource(jobUri + "/log");
	}
	
	private void log(Level level, String msg) {
		logger.log(level, msg);
		jobLogResource
			.header("Content-Type", "text/plain")
			.header("Referer", this.serviceUri)
			.post(level.toString() + ": " + msg);
	}
	
	public void finest(String msg)	 { this.log(Level.FINEST, msg); }
	public void finer(String msg)	 { this.log(Level.FINER, msg); }
	public void fine(String msg) 	 { this.log(Level.FINE, msg); }
	public void trace(String msg) 	 { this.log(Level.FINE, msg); }
	public void config(String msg)  { this.log(Level.CONFIG, msg); }
	public void info(String msg)    { this.log(Level.INFO, msg); }
	public void warning(String msg) { this.log(Level.WARNING, msg); }
	public void warn(String msg) 	 { this.log(Level.WARNING, msg); }
	public void severe(String msg)  { this.log(Level.SEVERE, msg); }

}
