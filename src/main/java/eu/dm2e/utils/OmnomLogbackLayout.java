package eu.dm2e.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

public class OmnomLogbackLayout extends LayoutBase<ILoggingEvent>{
	
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	boolean shortenClassName = false;
	
	@Override
	public String doLayout(ILoggingEvent event) {
		StringBuilder sb = new StringBuilder();
		sb.append(event.getLevel());
		sb.append("  [");
		String loggerName;
		if (! shortenClassName) {
			 loggerName = event.getLoggerName();
		} else {
			// TODO remove dot-separated parts
			loggerName = event.getLoggerName();
		}
		sb.append(loggerName);
		sb.append("] ");
		StringBuilder messageSB = new StringBuilder();
		int i = 0;
		for (String line : event.getMessage().split("\n")) {
			if (i++>0) {
				messageSB.append(LINE_SEPARATOR);
				messageSB.append("\t");
			}
			messageSB.append(line);
		}
		sb.append(messageSB.toString());
		sb.append(LINE_SEPARATOR);
		return sb.toString();
	}

}
