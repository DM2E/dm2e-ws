package eu.dm2e.ws;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;


/**
 * Config singleton handling all configuration from properties/XML files.
 *
 * 
 * @author Konstantin Baierer
 */
public enum Config {
	INSTANCE
	;
	
	private static final String DM2E_WS_CONFIG = "/config.xml";
	private final Configuration config;
//	private Logger log = LoggerFactory.getLogger(Config.class.getName());
	
	private Config() {
		Configuration c;
		DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
		builder.setFile(new File(DM2E_WS_CONFIG));
		try {
			c =  builder.getConfiguration();
		} catch (ConfigurationException e) {
			c = null;
			throw new RuntimeException(e);
		}
		config = c;
	}

//	public static String getString(String string) {
//		String conf =  config.getString(string);
//		if (null == conf) {
//			log.error("Undefined config option " + string);
//			throw new RuntimeException("Undefined config option " + string);
//		}
//		return conf;
//	}
	
	/**
	 * Get a configuration setting.
	 * 
	 * <pre>{@code
	 * String baseUri = Config.get(ConfigProp.BASE_URI)
	 * }</pre>
	 *
	 * @return Config value as String
	 */
	public static String get(ConfigProp configProp) {
		return INSTANCE.config.getString(configProp.getPropertiesName());
	}
	/**
	 * Set a configuration setting.
	 *
	 * Think carefully before using this, if config needs change, change the config file(s).
	 *
	 */
	public static void set(ConfigProp configProp, String value) {
		INSTANCE.config.setProperty(configProp.getPropertiesName(), value);
	}

	/**
	 * Whether the backing config singleton is null.
	 *
	 * @return true if there was an error initiating the config, false otherwise
	 */
	public static boolean isNull() {
		return INSTANCE.config == null;
	}
	
//	public static String getEndpointQuery
	
//	public static String ENDPOINT_QUERY = getString("dm2e.ws.sparql_endpoint");
//	public static String ENDPOINT_UPDATE = getString("dm2e.ws.sparql_endpoint_statements");
	
}
