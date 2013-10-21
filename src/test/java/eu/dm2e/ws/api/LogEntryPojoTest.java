package eu.dm2e.ws.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomUnitTest;
import eu.dm2e.ws.api.json.OmnomJsonSerializer;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.junit.GrafeoAssert;

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
		expected.addProperty(SerializablePojo.JSON_FIELD_UUID, logE.getUuid());
		expected.addProperty(NS.OMNOM.PROP_LOG_MESSAGE, "foo message");
		expected.addProperty(NS.DC.PROP_DATE, dt.toString());
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
//		expected.addProperty(SerializablePojo.JSON_FIELD_UUID, logE.getUuid());
		expected.addProperty(NS.OMNOM.PROP_LOG_MESSAGE, "foo");
		expected.addProperty(NS.DC.PROP_DATE, dt.toString());
		expected.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, logE.getRDFClassUri());
		assertEquals(testGson.toJson(expected), logE.toJson());
		assertEquals(testGson.toJson(expected), OmnomJsonSerializer.serializeToJSON(logE, LogEntryPojo.class));
	}
	
	
	@Test 
	public void testDeserializeJsonWithOutId() {
		DateTime dt = DateTime.now();
		String uuid = createUUID();
		JsonObject asJson = new JsonObject();
		asJson.addProperty(SerializablePojo.JSON_FIELD_UUID, uuid);
		asJson.addProperty(NS.OMNOM.PROP_LOG_MESSAGE, "foo");
		asJson.addProperty(NS.DC.PROP_DATE, dt.toString());
		asJson.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, LogEntryPojo.class.getAnnotation(RDFClass.class).value());
		LogEntryPojo logE = OmnomJsonSerializer.deserializeFromJSON(asJson.toString(), LogEntryPojo.class);
		assertEquals(logE.getUuid(), uuid);
		
		LogEntryPojo expected = new LogEntryPojo();
		expected.setUuid(uuid);
		expected.setMessage("foo");
		expected.setTimestamp(dt);
		
		log.info(expected.toJson());
		log.info(testGson.toJson(asJson));
		log.info(logE.toJson());
		
		assertEquals(expected.getUuid(), logE.getUuid());
		assertEquals(expected.getLevel(), logE.getLevel());
		assertEquals(expected.getMessage(), logE.getMessage());
		assertEquals(expected.getLevel(), logE.getLevel());
		assertEquals(expected.getTimestamp().toString(), logE.getTimestamp().toString());
		GrafeoAssert.graphsAreEquivalent(expected, logE);
		assertEquals(expected, logE);
		assertEquals(testGson.toJson(asJson), logE.toJson());
	}

	@Test 
	public void testDeserializeJsonWithId() {
		DateTime dt = DateTime.now();
		JsonObject asJson = new JsonObject();
		asJson.addProperty(SerializablePojo.JSON_FIELD_ID, "http://foo/bar/quux");
//		asJson.addProperty(SerializablePojo.JSON_FIELD_UUID, uuid);
		asJson.addProperty(NS.OMNOM.PROP_LOG_MESSAGE, "foo");
		asJson.addProperty(NS.DC.PROP_DATE, dt.toString());
		asJson.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, LogEntryPojo.class.getAnnotation(RDFClass.class).value());
		LogEntryPojo logE = OmnomJsonSerializer.deserializeFromJSON(asJson.toString(), LogEntryPojo.class);
		
		LogEntryPojo expected = new LogEntryPojo();
		expected.setId("http://foo/bar/quux");
		expected.setMessage("foo");
		expected.setTimestamp(dt);
		
		log.info(expected.toJson());
		log.info(testGson.toJson(asJson));
		log.info(logE.toJson());
		
		assertEquals(expected.getLevel(), logE.getLevel());
		assertEquals(expected.getMessage(), logE.getMessage());
		assertEquals(expected.getId(), logE.getId());
		assertEquals(expected.getLevel(), logE.getLevel());
		assertEquals(expected.getTimestamp().toString(), logE.getTimestamp().toString());
		assertEquals(expected, logE);
		assertEquals(testGson.toJson(asJson), logE.toJson());
	}
	
//	@Test
//	public void testFromCanonicalNTriples() {
//		LogEntryPojo logentry = null;
//		logentry = new LogEntryPojo().constructFromRdfString(asCanonicalNTriples, id);
//		assertEquals(asCanonicalNTriples, logentry.getCanonicalNTriples());
//	}

}
