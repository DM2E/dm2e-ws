package eu.dm2e.ws.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonObject;

import eu.dm2e.ws.OmnomUnitTest;
import eu.dm2e.ws.api.json.OmnomJsonSerializer;
import eu.dm2e.ws.grafeo.annotations.RDFClass;

public class LogEntryPojoTest extends OmnomUnitTest{
	private String logId = "http://log.foo/bar/1234";
    private String level = "DEBUG";
	private DateTime timestamp;
	private String asCanonicalNTriples = "<http://log.foo/bar/1234> <http://onto.dm2e.eu/omnom/hasLogLevel> \"DEBUG\"^^<http://www.w3.org/2001/XMLSchema#string> ."
		 + "\n<http://log.foo/bar/1234> <http://purl.org/dc/elements/1.1/date> \"1913-02-10T19:10:20.000Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ."
		 + "\n<http://log.foo/bar/1234> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://onto.dm2e.eu/omnom/LogEntry> .";
	
	@Before
	public void setUp(){
		timestamp = DateTime.parse("1913-02-10T19:10:20.000Z");
	}
	
	@Test
	public void test() {
		LogEntryPojo logentry = new LogEntryPojo();
		assertNotNull(logentry);
		logentry.setId(logId);
		logentry.setLevel(level);
		logentry.setTimestamp(timestamp);
		assertEquals(logId, logentry.getId());
		assertEquals(level, logentry.getLevel());
		assertEquals(timestamp, logentry.getTimestamp());
	}
	
	@Test
	public void testToCanonicalNTriples() {
		LogEntryPojo logentry = new LogEntryPojo();
		logentry.setId(logId);
		logentry.setLevel(level);
		logentry.setTimestamp(timestamp);
		assertEquals(asCanonicalNTriples, logentry.getCanonicalNTriples());
	}
	
	@Test
	public void testSerializeJsonNoId() {
		final DateTime dt = DateTime.now();
		LogEntryPojo logE = new LogEntryPojo();
		logE.setTimestamp(dt);
		logE.setMessage("foo message");

		JsonObject expected = new JsonObject();
		expected.addProperty("message", "foo message");
		expected.addProperty("timestamp", dt.toString());
		expected.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, logE.getRDFClassUri());
		log.info(logE.toJson());
		assertEquals(testGson.toJson(expected), logE.toJson());
		assertEquals(testGson.toJson(expected), OmnomJsonSerializer.serializeToJSON(logE, LogEntryPojo.class));
	}
	
	@Test
	public void testSerializeJsonWithId() {
		final DateTime dt = DateTime.now();
		LogEntryPojo logE = new LogEntryPojo();
		logE.setTimestamp(dt);
		logE.setMessage("foo");
		logE.setId(logId);

		JsonObject expected = new JsonObject();
		expected.addProperty(SerializablePojo.JSON_FIELD_ID, logId);
		expected.addProperty("message", "foo");
		expected.addProperty("timestamp", dt.toString());
		expected.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, logE.getRDFClassUri());
		assertEquals(testGson.toJson(expected), logE.toJson());
		assertEquals(testGson.toJson(expected), OmnomJsonSerializer.serializeToJSON(logE, LogEntryPojo.class));
	}
	
	

	@Ignore
	@Test 
	public void testDeserializeJson() {
		DateTime dt = DateTime.now();
		JsonObject asJson = new JsonObject();
		asJson.addProperty("message", "foo");
		asJson.addProperty("timestamp", dt.toString());
		asJson.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, LogEntryPojo.class.getAnnotation(RDFClass.class).value());
		log.info(asJson.toString());
		LogEntryPojo logE = OmnomJsonSerializer.deserializeFromJSON(asJson.toString(), LogEntryPojo.class);
		log.info("" + logE);
		
		LogEntryPojo expected = new LogEntryPojo();
		expected.setMessage("foo");
		expected.setTimestamp(dt);
		assertEquals(expected, logE);
		assertEquals(testGson.toJson(expected), OmnomJsonSerializer.serializeToJSON(logE, LogEntryPojo.class));
	}
	
//	@Test
//	public void testFromCanonicalNTriples() {
//		LogEntryPojo logentry = null;
//		logentry = new LogEntryPojo().constructFromRdfString(asCanonicalNTriples, id);
//		assertEquals(asCanonicalNTriples, logentry.getCanonicalNTriples());
//	}

}
