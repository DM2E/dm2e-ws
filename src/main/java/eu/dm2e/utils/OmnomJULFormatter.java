package eu.dm2e.utils;


public class OmnomJULFormatter  { }
//public class OmnomJULFormatter extends Formatter {
//	
//    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
//	
//	public OmnomJULFormatter() {
//		super();
//	}
//
//	@Override
//	public String format(LogRecord record) {
//		
//		StringBuilder sb = new StringBuilder();
//		
//		// Get the date from the LogRecord and add it to the buffer
////		Date date = new Date(record.getMillis());
////		sb.append(date.toString());
////		sb.append(" ");
//		
//		boolean flagShortClassName = Boolean.parseBoolean(
//				LogManager.getLogManager().getProperty("eu.dm2e.utils.OmnomJULFormatter.shortClassName"));
//		
//		sb.append(record.getLevel().getName());
//		sb.append(" ");
//		
//		sb.append("[");
//		if (flagShortClassName) {
//			String simpleClassName = record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.')+1);
//			sb.append(simpleClassName);
//		}
//		else {
//			sb.append(record.getSourceClassName());
//		}
//		sb.append(".");
//		sb.append(record.getSourceMethodName());
//		sb.append("] ");
//		
//		StringBuilder messageSB = new StringBuilder();
//		int i = 0;
//		for (String line : formatMessage(record).split("\n")) {
//			if (i++>0) {
//				messageSB.append(LINE_SEPARATOR);
//				messageSB.append("\t");
//			}
//			messageSB.append(line);
//		}
//		sb.append(messageSB.toString());
//
//		sb.append(LINE_SEPARATOR);
//		return sb.toString();
//	}
//
//}
