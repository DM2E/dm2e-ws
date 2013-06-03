package eu.dm2e.ws;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;


public final class Config {
	
	private static final String DM2E_WS_CONFIG = "config.xml";
	
	public static final Configuration config;
	
	private static Logger log = Logger.getLogger(Config.class.getName());
	
	static {
		Configuration c;
		DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
		builder.setFile(new File(DM2E_WS_CONFIG));
		try {
			c =  builder.getConfiguration();
		} catch (ConfigurationException e) {
			c = null;
		}
		config = c;
	}

	public static String getString(String string) {
		String conf =  config.getString(string);
		if (null == conf) {
			log.severe("Undefined config option " + string);
		}
		return conf;
	}
	
	public static String ENDPOINT_QUERY = getString("dm2e.ws.sparql_endpoint");
	public static String ENDPOINT_UPDATE = getString("dm2e.ws.sparql_endpoint_statements");
	
}
