package eu.dm2e.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Field for logging pattern that returns either ' -- MARKER_NAME' or the empty string.
 *
 * @author Konstantin Baierer
 */
public class SeparatorMarkerOrEmptyStringConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		if (event.getMarker() == null)
			return "";
		return " -- " + event.getMarker().getName();
	}

}
