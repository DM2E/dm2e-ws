package eu.dm2e.ws.api;



import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomUnitTest;
import eu.dm2e.ws.api.json.OmnomJsonSerializer;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public class UserPojoTest extends OmnomUnitTest {

	@Test
	public void testDeserializeStrings() throws Exception {
		String uri = "http://foo/bar/user1";

		UserPojo userPojo = new UserPojo();
		userPojo.setId(uri);
		userPojo.setName("John Doe");
		userPojo.setPreferredTheme(UserPojo.THEMES.DARK.getName());
		log.info("from turtle");
		{
			String ttlStr = 
					"@prefix foaf:  <http://xmlns.com/foaf/0.1/> ."
					+"@prefix omnom: <http://onto.dm2e.eu/omnom/> ."
					+"<"+uri+"> a foaf:Person ;"
					+"   foaf:name \"John Doe\" ;"
					+"   omnom:preferredTheme \"dark\".";
			GrafeoImpl g = new GrafeoImpl(ttlStr, true);
			log.debug(LogbackMarkers.DATA_DUMP, g.getTerseTurtle());
			UserPojo newUserPojo = g.getObjectMapper().getObject(UserPojo.class, uri);
			assertEquals(userPojo.getPreferredTheme(), newUserPojo.getPreferredTheme());
			assertEquals(userPojo.getName(), newUserPojo.getName());
			assertEquals(userPojo.getId(), newUserPojo.getId());
		}
		log.info("from json");
		{
			String jsonStr = "{"
					+"\"id\":\"" + uri + "\""
					+","
					+"\"http://xmlns.com/foaf/0.1/name\":\"John Doe\""
					+","
					+"\"http://onto.dm2e.eu/omnom/preferredTheme\":\"dark\""
					+"}";
			UserPojo newUserPojo = OmnomJsonSerializer.deserializeFromJSON(jsonStr, UserPojo.class);
			assertEquals(userPojo.getPreferredTheme(), newUserPojo.getPreferredTheme());
			assertEquals(userPojo.getName(), newUserPojo.getName());
			assertEquals(userPojo.getId(), newUserPojo.getId());
		}
	}



	@Test
	public void testDeserializeWebServices() throws Exception {
		String uri = "http://foo/bar/user1";

		UserPojo userPojo = new UserPojo();
		
		// fileservices
		final String fs1_uri = "http://service/file1";
		final String fs2_uri = "http://service/file2";
		WebservicePojo fs1 = new WebservicePojo();
		fs1.setId(fs1_uri);
		WebservicePojo fs2 = new WebservicePojo();
		fs2.setId(fs2_uri);
		userPojo.getFileServices().add(fs1);
		userPojo.getFileServices().add(fs2);
		
		// web services
		final String ws1_uri = "http://service/service1";
		final String ws2_uri = "http://service/service2";
		WebservicePojo ws1 = new WebservicePojo();
		ws1.setId(ws1_uri);
		WebservicePojo ws2 = new WebservicePojo();
		ws2.setId(ws2_uri);
		userPojo.getWebServices().add(ws1);
		userPojo.getWebServices().add(ws2);

		log.info("from turtle");
		{
			final String ttlStr = 
					"@prefix foaf:  <http://xmlns.com/foaf/0.1/> ."
					+"@prefix omnom: <http://onto.dm2e.eu/omnom/> ."
					+"<"+uri+">"
					+"   omnom:fileservice <" + fs1_uri + ">;"
					+"   omnom:fileservice <" + fs2_uri + ">;"
					+"   omnom:webservice <" + ws1_uri + ">;"
					+"   omnom:webservice <" + ws2_uri + ">;"
					+" a foaf:Person .";
			GrafeoImpl g = new GrafeoImpl(ttlStr, true);
			UserPojo newUserPojo = g.getObjectMapper().getObject(UserPojo.class, uri);
			assertEquals(2, newUserPojo.getFileServices().size());
			for (Iterator<WebservicePojo> iter = newUserPojo.getFileServices().iterator() ; iter.hasNext() ; ) {
				final String tocheck = iter.next().getId();
				assertTrue(tocheck.equals(fs1_uri) || tocheck.equals(fs2_uri));
			}
			assertEquals(2, newUserPojo.getWebServices().size());
			for (Iterator<WebservicePojo> iter = newUserPojo.getWebServices().iterator() ; iter.hasNext() ; ) {
				final String tocheck = iter.next().getId();
				assertTrue(tocheck.equals(ws1_uri) || tocheck.equals(ws2_uri));
			}
		}
		log.info("from json");
		{
			String jsonStr = "{"
					+"\"id\":\"" + uri + "\""
					+","
					+"\"http://onto.dm2e.eu/omnom/fileservice\":["
						+"{ \"id\":\""+ fs1_uri +"\"}"
						+","
						+"{ \"id\":\""+ fs2_uri +"\"}"
					+"]"
					+","
					+"\"http://onto.dm2e.eu/omnom/webservice\":["
						+"{ \"id\":\""+ ws1_uri +"\"}"
						+","
						+"{ \"id\":\""+ ws2_uri +"\"}"
					+"]"
					+"}";
			log.debug(jsonStr);
			UserPojo newUserPojo = OmnomJsonSerializer.deserializeFromJSON(jsonStr, UserPojo.class);
			assertEquals(2, newUserPojo.getFileServices().size());
			for (Iterator<WebservicePojo> iter = newUserPojo.getFileServices().iterator() ; iter.hasNext() ; ) {
				final String tocheck = iter.next().getId();
				assertTrue(tocheck.equals(fs1_uri) || tocheck.equals(fs2_uri));
			}
			assertEquals(2, newUserPojo.getWebServices().size());
			for (Iterator<WebservicePojo> iter = newUserPojo.getWebServices().iterator() ; iter.hasNext() ; ) {
				final String tocheck = iter.next().getId();
				assertTrue(tocheck.equals(ws1_uri) || tocheck.equals(ws2_uri));
			}
		}
		log.info("from json (flat ids)");
		{
			String jsonStr = "{"
					+"\"id\":\"" + uri + "\""
					+","
					+"\"http://onto.dm2e.eu/omnom/fileservice\":["
						+"\""+ fs1_uri +"\""
						+","
						+"\""+ fs2_uri +"\""
					+"]"
					+","
					+"\"http://onto.dm2e.eu/omnom/webservice\":["
						+"\""+ ws1_uri +"\""
						+","
						+"\""+ ws2_uri +"\""
					+"]"
				+"}";
			log.debug(jsonStr);
			UserPojo newUserPojo = OmnomJsonSerializer.deserializeFromJSON(jsonStr, UserPojo.class);
			assertEquals(2, newUserPojo.getFileServices().size());
			for (Iterator<WebservicePojo> iter = newUserPojo.getFileServices().iterator() ; iter.hasNext() ; ) {
				final String tocheck = iter.next().getId();
				assertTrue(tocheck.equals(fs1_uri) || tocheck.equals(fs2_uri));
			}
			assertEquals(2, newUserPojo.getWebServices().size());
			for (Iterator<WebservicePojo> iter = newUserPojo.getWebServices().iterator() ; iter.hasNext() ; ) {
				final String tocheck = iter.next().getId();
				assertTrue(tocheck.equals(ws1_uri) || tocheck.equals(ws2_uri));
			}
		}
	}
	
	@Test
	public void testSerializeWebServices() throws Exception {
		String uri = "http://foo/bar/user1";

		UserPojo userPojo = new UserPojo();
		
		// fileservices
		final String fs1_uri = "http://service/file1";
		final String fs2_uri = "http://service/file2";
		WebservicePojo fs1 = new WebservicePojo();
		fs1.setId(fs1_uri);
		WebservicePojo fs2 = new WebservicePojo();
		fs2.setId(fs2_uri);
		userPojo.getFileServices().add(fs1);
		userPojo.getFileServices().add(fs2);
		
		// web services
		final String ws1_uri = "http://service/service1";
		final String ws2_uri = "http://service/service2";
		WebservicePojo ws1 = new WebservicePojo();
		ws1.setId(ws1_uri);
		WebservicePojo ws2 = new WebservicePojo();
		ws2.setId(ws2_uri);
		userPojo.getWebServices().add(ws1);
		userPojo.getWebServices().add(ws2);
		userPojo.setId(uri);

		log.info("to ntriples");
		{
			final String ttlStr = ""
					+"<"+uri+"> <" + NS.OMNOM.PROP_FILE_SERVICE  +"> <" + fs1_uri + "> .\n"
					+"<"+uri+"> <" + NS.OMNOM.PROP_FILE_SERVICE  +"> <" + fs2_uri + "> .\n"
					+"<"+uri+"> <" + NS.OMNOM.PROP_WEBSERVICE  +"> <" + ws1_uri + "> .\n"
					+"<"+uri+"> <" + NS.OMNOM.PROP_WEBSERVICE  +"> <" + ws2_uri + "> .\n"
					+"<"+uri+"> <" + NS.RDF.PROP_TYPE  +"> <" + NS.FOAF.CLASS_PERSON + "> ."
					;
//			assertEquals(ttlStr, userPojo.getCanonicalNTriples());
		}
		log.info("to json");
		{
			String jsonStr = "{\n"
					+"  \"id\": \"" + uri + "\""
					+",\n"
					+"  \"" + NS.OMNOM.PROP_FILE_SERVICE + "\": [\n"
					+"    {\"id\": \""+ fs1_uri +"\"}"
					+",\n"
					+"    {\"id\": \""+ fs2_uri +"\"}"
					+"\n  ]"
					+",\n"
					+"  \"" + NS.OMNOM.PROP_WEBSERVICE + "\": [\n"
					+"    {\"id\": \""+ ws1_uri +"\"}"
					+",\n"
					+"    {\"id\": \""+ ws2_uri +"\"}"
					+"\n  ]"
					+",\n"
					+"\"" + NS.RDF.PROP_TYPE + "\": \"" + NS.FOAF.CLASS_PERSON + "\"\n"
					+"}";
			log.debug(jsonStr);
			log.debug(userPojo.toJson());
			JSONAssert.assertEquals(jsonStr, userPojo.toJson(), false);
		}
		log.info("from json (flat ids)");
		{
			String jsonStr = "{"
					+"\"id\":\"" + uri + "\""
					+","
					+"\"http://onto.dm2e.eu/omnom/fileservice\":["
						+"\""+ fs1_uri +"\""
						+","
						+"\""+ fs2_uri +"\""
					+"]"
					+","
					+"\"http://onto.dm2e.eu/omnom/webservice\":["
						+"\""+ ws1_uri +"\""
						+","
						+"\""+ ws2_uri +"\""
					+"]"
				+"}";
			log.debug(jsonStr);
			UserPojo newUserPojo = OmnomJsonSerializer.deserializeFromJSON(jsonStr, UserPojo.class);
			assertEquals(2, newUserPojo.getFileServices().size());
			for (Iterator<WebservicePojo> iter = newUserPojo.getFileServices().iterator() ; iter.hasNext() ; ) {
				final String tocheck = iter.next().getId();
				assertTrue(tocheck.equals(fs1_uri) || tocheck.equals(fs2_uri));
			}
			assertEquals(2, newUserPojo.getWebServices().size());
			for (Iterator<WebservicePojo> iter = newUserPojo.getWebServices().iterator() ; iter.hasNext() ; ) {
				final String tocheck = iter.next().getId();
				assertTrue(tocheck.equals(ws1_uri) || tocheck.equals(ws2_uri));
			}
		}
	}
}
