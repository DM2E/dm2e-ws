package eu.dm2e.ws.tests;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.grafeo.json.GrafeoJsonSerializer;
import eu.dm2e.grafeo.json.JodaDateTimeSerializer;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.LogEntryPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.ParameterConnectorPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.UserPojo;
import eu.dm2e.ws.api.VersionedDatasetPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
public class OmnomUnitTest {
	
	protected Logger log = LoggerFactory.getLogger(getClass().getName());
	protected Gson testGson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, new JodaDateTimeSerializer())
			.setPrettyPrinting()
			.create();
	protected static Map<OmnomTestResources, String> configString = new HashMap<>();
	protected Map<OmnomTestResources, File> configFile = new HashMap<>();;
    public OmnomUnitTest() {
        GrafeoJsonSerializer.registerType(JobPojo.class);
        GrafeoJsonSerializer.registerType(FilePojo.class);
        GrafeoJsonSerializer.registerType(LogEntryPojo.class);
        GrafeoJsonSerializer.registerType(ParameterAssignmentPojo.class);
        GrafeoJsonSerializer.registerType(ParameterConnectorPojo.class);
        GrafeoJsonSerializer.registerType(ParameterPojo.class);
        GrafeoJsonSerializer.registerType(UserPojo.class);
        GrafeoJsonSerializer.registerType(VersionedDatasetPojo.class);
        GrafeoJsonSerializer.registerType(WebserviceConfigPojo.class);
        GrafeoJsonSerializer.registerType(WebservicePojo.class);
        GrafeoJsonSerializer.registerType(WorkflowPojo.class);
        GrafeoJsonSerializer.registerType(WorkflowPositionPojo.class);
		for (OmnomTestResources res : OmnomTestResources.values()) { 
			URL testConfigURL = OmnomTestCase.class.getResource(res.getPath());
			try {
				configFile.put(res, new File(testConfigURL.getFile()));
				configString.put(res, IOUtils.toString(testConfigURL.openStream()));
			} catch (Exception e) {
				org.junit.Assert.fail(res + " not found: " + e.toString());
			}
		}
    }

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
