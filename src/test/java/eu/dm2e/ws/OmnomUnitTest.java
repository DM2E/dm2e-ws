package eu.dm2e.ws;

import org.joda.time.DateTime;
import org.junit.ComparisonFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.dm2e.ws.api.json.JodaDateTimeSerializer;
import eu.dm2e.ws.api.json.OmnomJsonSerializer;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
public class OmnomUnitTest {
	
	protected Logger log = LoggerFactory.getLogger(getClass().getName());
	protected Gson testGson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, new JodaDateTimeSerializer())
			.setPrettyPrinting()
			.create();
	protected OmnomJsonSerializer jsonSerializer = new OmnomJsonSerializer();
	static {
		 // Optionally remove existing handlers attached to j.u.l root logger
		 SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)

		 // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
		 // the initialization phase of your application
		 SLF4JBridgeHandler.install();
	}
	static {
//		try {
	        System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize", "100000000");
//			System.setProperty("java.util.logging.config.file", "logging.properties");
			System.setProperty(GrafeoImpl.NO_EXTERNAL_URL_FLAG, "true");
//			LogManager.getLogManager().readConfiguration();
			Logger log = LoggerFactory.getLogger("LOGGING INIT");
			log.info("Initialized logging.");
//			log.info("Logging Handlers: " + Arrays.asList(log.getHandlers()));
//			log.info(LogManager.getLogManager().getProperty("java.util.logging.ConsoleHandler.formatter"));
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.exit(1);
//		}
	}
	
	public static void assertStringsEqual(String s1, String s2) {
		if (! s1.equals(s2))
			throw new ComparisonFailure("Strings aren't equal", s1, s2);
			
	}

}
