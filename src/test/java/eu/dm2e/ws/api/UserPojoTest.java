package eu.dm2e.ws.api;



import static org.junit.Assert.*;

import org.junit.Test;

import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.OmnomUnitTest;
import eu.dm2e.ws.api.json.OmnomJsonSerializer;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public class UserPojoTest extends OmnomUnitTest {

	@Test
	public void testDeserialize() throws Exception {
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

}
