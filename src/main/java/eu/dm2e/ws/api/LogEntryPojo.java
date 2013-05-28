package eu.dm2e.ws.api;

import java.util.Date;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.model.LogLevel;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass("omnom:LogEntry")
@RDFInstancePrefix("http://data.dm2e.eu/logentry/")
public class LogEntryPojo extends AbstractPersistentPojo<LogEntryPojo>{
	
	@RDFId
	private String id;
	
	@RDFProperty("omnom:hasLogMessage")
	private String message;
	
	@RDFProperty("omnom:hasLogLevel")
	private String level;
	
	@RDFProperty("dc:date")
	private Date timestamp;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	public String getLevel() { return level; }
	public void setLevel(String level) { this.level = level; }
	public void setLevel(LogLevel level) { this.level = level.toString(); }
	
	public Date getTimestamp() { return timestamp; }
	public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getTimestamp());
		sb.append(" ");
		sb.append(this.getLevel());
		sb.append("> ");
		sb.append(this.getMessage());
		return sb.toString();
	}

}
