package eu.dm2e.logback;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Logback Markers used in our project, which can be passed as first argument to log statements.
 *
 * <pre>
 * {@code
 * log.debug(LogbackMarkers.DATA_DUMP, obj.methodThatProducesCopiousOutput());
 * }
 * </pre>
 *
 * @author Konstantin Baierer
 */
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
	/**
	 * Used for tracing time, evaluating queries etc.
	 */
	public static final Marker TRACE_TIME = MarkerFactory.getMarker("TRACE_TIME");
	
}
