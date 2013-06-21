package eu.dm2e.ws;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
public class OmnomUnitTest {
	
	protected Logger log = Logger.getLogger(getClass().getName());
	static {
		try {
			System.setProperty("java.util.logging.config.file", "logging.properties");
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

}
