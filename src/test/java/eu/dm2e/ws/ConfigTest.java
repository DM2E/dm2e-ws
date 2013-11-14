package eu.dm2e.ws;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.dm2e.ws.tests.OmnomTestCase;

public class ConfigTest extends OmnomTestCase {
	
	@Test
	public void testConfig() {
		assertFalse(Config.isNull());
		assertNotNull(Config.get(ConfigProp.BASE_URI));
		for (ConfigProp prop : ConfigProp.class.getEnumConstants()) {
			assertNotNull(Config.get(prop));
		}
	}

}
