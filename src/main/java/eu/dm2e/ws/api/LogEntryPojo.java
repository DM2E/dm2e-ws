package eu.dm2e.ws.api;

import org.joda.time.DateTime;

import eu.dm2e.NS;
import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.model.LogLevel;

/**
 *  Pojo for a single Log entry 
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass(NS.OMNOM.CLASS_LOG_ENTRY)
@RDFInstancePrefix("http://data.dm2e.eu/logentry/")
public class LogEntryPojo extends AbstractPersistentPojo<LogEntryPojo>{
	
	@RDFProperty(NS.OMNOM.PROP_LOG_MESSAGE)
	private String message;
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	@RDFProperty(NS.OMNOM.PROP_LOG_LEVEL)
	private String level;
	public String getLevel() { return level; }
	public void setLevel(String level) { this.level = level; }
	public void setLevel(LogLevel level) { this.level = level.toString(); }
	
	@RDFProperty(NS.DC.PROP_DATE)
	private DateTime timestamp;
	public DateTime getTimestamp() { return timestamp; }
	public void setTimestamp(DateTime timestamp) { this.timestamp = timestamp; }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof LogEntryPojo)) return false;
		LogEntryPojo other = (LogEntryPojo) obj;
		if (getId() == null) {
			if (other.getId() != null) return false;
		} else if (!getId().equals(other.getId())) return false;
		if (level == null) {
			if (other.level != null) return false;
		} else if (!level.equals(other.level)) return false;
		if (message == null) {
			if (other.message != null) return false;
		} else if (!message.equals(other.message)) return false;
		if (timestamp == null) {
			if (other.timestamp != null) return false;
		} else if (!timestamp.equals(other.timestamp)) return false;
		return true;
	}
	
//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append(this.getTimestamp());
//		sb.append(" ");
//		sb.append(this.getLevel());
//		sb.append("> ");
//		sb.append(this.getMessage());
//		return sb.toString();
//	}
	

}
