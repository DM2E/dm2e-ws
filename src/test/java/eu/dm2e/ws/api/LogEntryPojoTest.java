package eu.dm2e.ws.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jetty.util.log.Log;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.OmnomUnitTest;

public class LogEntryPojoTest extends OmnomUnitTest{
	private String id = "http://log.foo/bar/1234";
    private String level = "DEBUG";
	private DateTime timestamp;
	private String asCanonicalNTriples = "<http://log.foo/bar/1234> <http://onto.dm2e.eu/omnom/hasLogLevel> \"DEBUG\"^^<http://www.w3.org/2001/XMLSchema#string> ."
		 + "\n<http://log.foo/bar/1234> <http://purl.org/dc/elements/1.1/date> \"1913-02-10T19:10:20.000Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ."
		 + "\n<http://log.foo/bar/1234> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://onto.dm2e.eu/omnom/LogEntry> .";
	
	@Before
	public void setUp(){
		log.info("ffoo");
		timestamp = DateTime.parse("1913-02-10T19:10:20.000Z");
	}
	
	@Test
	public void test() {
		LogEntryPojo logentry = new LogEntryPojo();
		assertNotNull(logentry);
		logentry.setId(id);
		logentry.setLevel(level);
		logentry.setTimestamp(timestamp);
		assertEquals(id, logentry.getId());
		assertEquals(level, logentry.getLevel());
		assertEquals(timestamp, logentry.getTimestamp());
	}
	
	@Test
	public void testToCanonicalNTriples() {
		LogEntryPojo logentry = new LogEntryPojo();
		logentry.setId(id);
		logentry.setLevel(level);
		logentry.setTimestamp(timestamp);
		assertEquals(asCanonicalNTriples, logentry.getCanonicalNTriples());
	}
	
//	@Test
//	public void testFromCanonicalNTriples() {
//		LogEntryPojo logentry = null;
//		logentry = new LogEntryPojo().constructFromRdfString(asCanonicalNTriples, id);
//		assertEquals(asCanonicalNTriples, logentry.getCanonicalNTriples());
//	}

}
