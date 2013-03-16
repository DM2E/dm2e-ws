package eu.dm2e.ws;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;


public final class Config {
	
	private static final String DM2E_WS_CONFIG = "config.xml";
	
	public static final Configuration config;
	
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
		return config.getString(string);
	}

}
