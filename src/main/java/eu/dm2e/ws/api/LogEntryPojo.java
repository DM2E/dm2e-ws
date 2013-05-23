package eu.dm2e.ws.api;

import java.util.Calendar;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.model.LogLevel;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass("omnom:LogEntry")
public class LogEntryPojo extends AbstractPersistentPojo<LogEntryPojo>{
	
	@RDFId
	private String id;
	
	@RDFProperty("omnom:hasLogMessage")
	private String message;
	
	@RDFProperty("omnom:hasLogLevel")
	private String level;
	
	@RDFProperty("dc:date")
	private Calendar timestamp;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	public String getLevel() { return level; }
	public void setLevel(String level) { this.level = level; }
	public void setLevel(LogLevel level) { this.level = level.toString(); }
	
	public Calendar getTimestamp() { return timestamp; }
	public void setTimestamp(Calendar timestamp) { this.timestamp = timestamp; }

}
