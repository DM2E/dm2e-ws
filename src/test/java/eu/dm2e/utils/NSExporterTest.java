package eu.dm2e.utils;

import org.junit.Test;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.NS.OMNOM;
import eu.dm2e.ws.tests.OmnomTestCase;

public class NSExporterTest extends OmnomTestCase {
	
	@Test
	public void testExportToOwl() throws IllegalArgumentException, IllegalAccessException {
		
		Class<OMNOM> cls = NS.OMNOM.class;
		String base = NS.OMNOM.BASE;
		String x = NSExporter.exportStaticClassToOWL(base, cls);
		log.debug(x);
	}

}
