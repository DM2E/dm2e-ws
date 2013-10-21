package eu.dm2e.ws;

import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.dm2e.ws.api.json.JodaDateTimeSerializer;
import eu.dm2e.ws.api.json.OmnomJsonSerializer;
import eu.dm2e.grafeo.jena.GrafeoImpl;
public class OmnomUnitTest {
	
	protected Logger log = LoggerFactory.getLogger(getClass().getName());
	protected Gson testGson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, new JodaDateTimeSerializer())
			.setPrettyPrinting()
			.create();
	protected OmnomJsonSerializer jsonSerializer = new OmnomJsonSerializer();
	static {
//		try {
			System.setProperty("dm2e-ws.test.properties_file", "dm2e-ws.test.properties");
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
	
//	public static void assertStringsEqual(String s1, String s2) {
//		if (! s1.equals(s2))
//			throw new ComparisonFailure("Strings aren't equal", s1, s2);
//	}
	
	public static String createUUID() {
		return UUID.randomUUID().toString();
	}

}
