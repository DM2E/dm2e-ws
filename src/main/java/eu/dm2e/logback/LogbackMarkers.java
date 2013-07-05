package eu.dm2e.logback;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class LogbackMarkers {
	
	/**
	 * To be used for responses from remote servers
	 */
	public static final Marker HTTP_RESPONSE_DUMP = MarkerFactory.getMarker("HTTP_RESPONSE_DUMP");
	/**
	 * To be used for RDF or JSON dumps of Pojos
	 */
	public static final Marker DATA_DUMP = MarkerFactory.getMarker("DATA_DUMP");
	/**
	 * To be used for messages pertaining to inter-server communication
	 */
	public static final Marker SERVER_COMMUNICATION = MarkerFactory.getMarker("SERVER_COMMUNICATION");
	/**
	 * To be used for passwords, users etc.
	 */
	public static final Marker SENSITIVE_INFORMATION = MarkerFactory.getMarker("SENSITIVE_INFORMATION");
	
}
