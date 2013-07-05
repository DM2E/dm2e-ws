package eu.dm2e.logback;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class OmnomLogbackLayout extends PatternLayout{
	
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Override
	public String doLayout(ILoggingEvent event) {
		StringBuilder messageSB = new StringBuilder();
		int i = 0;
		for (String line : super.doLayout(event).split(LINE_SEPARATOR)) {
			if (i++>0) {
				messageSB.append(LINE_SEPARATOR);
				messageSB.append("\t");
			}
			messageSB.append(line);
		}
		messageSB.append(LINE_SEPARATOR);
		return messageSB.toString();
	}

}
