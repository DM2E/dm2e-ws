package eu.dm2e.ws;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.ComparisonFailure;

import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
public class OmnomUnitTest {
	
	protected Logger log = Logger.getLogger(getClass().getName());
	static {
		try {
	        System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize", "100000000");
			System.setProperty("java.util.logging.config.file", "logging.properties");
			System.setProperty(GrafeoImpl.NO_EXTERNAL_URL_FLAG, "true");
			LogManager.getLogManager().readConfiguration();
			Logger log = Logger.getLogger("LOGGING INIT");
			log.info("Initialized logging.");
			log.info(LogManager.getLogManager().getProperty("java.util.logging.ConsoleHandler.formatter"));
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void assertStringsEqual(String s1, String s2) {
		if (! s1.equals(s2))
			throw new ComparisonFailure("Strings aren't equal", s1, s2);
			
	}

}
