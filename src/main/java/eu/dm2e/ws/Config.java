package eu.dm2e.ws;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;


public final class Config {
	
	private static final String DM2E_WS_CONFIG = "config.xml";
	
	public static final Configuration config;
	
	private static Logger log = LoggerFactory.getLogger(Config.class.getName());
	
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
			log.error("Undefined config option " + string);
			throw new RuntimeException("Undefined config option " + string);
		}
		return conf;
	}
	
	public static String get(ConfigProp configProp) {
		return config.getString(configProp.getPropertiesName());
	}
	
//	public static String getEndpointQuery
	
//	public static String ENDPOINT_QUERY = getString("dm2e.ws.sparql_endpoint");
//	public static String ENDPOINT_UPDATE = getString("dm2e.ws.sparql_endpoint_statements");
	
}
