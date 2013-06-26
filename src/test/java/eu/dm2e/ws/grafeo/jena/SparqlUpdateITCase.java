package eu.dm2e.ws.grafeo.jena;



import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.grafeo.junit.GrafeoAssert;

public class SparqlUpdateITCase extends OmnomTestCase {
	
	private static final String ENDPOINT_QUERY = Config.ENDPOINT_QUERY;

	private static final String ENDPOINT_UPDATE = Config.ENDPOINT_UPDATE;

	public static final String BASE = "http://www.w3.org/2000/01/rdf-schema#";
	
	private static final String SUB1  = BASE + "sub1";
	private static final String SUB2  = BASE + "sub2";
	private static final String SUB3  = BASE + "sub3";
	
	private static final String PROP1 = BASE + "prop1";
	private static final String PROP2 = BASE + "prop2";
	private static final String PROP3 = BASE + "prop3";
	
	private static final String OBJ1  = BASE + "obj1";
	private static final String OBJ2  = BASE + "obj2";
	private static final String OBJ3  = BASE + "obj3";
	
	private static final String GRAPH1 = "http://graph/1";
	
	private GrafeoImpl g1 = new GrafeoImpl();
	private GrafeoImpl g2 = new GrafeoImpl();
	private GrafeoImpl g3 = new GrafeoImpl();
	private GrafeoImpl g4 = new GrafeoImpl();
	
	@Before
	public void setThisUp() {
		g1.empty();
		g1.addTriple(SUB1, PROP1, OBJ1);
		
		g2.empty();
		g2.addTriple(SUB2, PROP2, OBJ2);
		
		g3.empty();
		g3.addTriple(SUB1, PROP1, OBJ1);
		g3.addTriple(SUB2, PROP2, OBJ2);
		
		g4.empty();
		g4.addTriple(SUB3, PROP3, OBJ3);
		
		new SparqlUpdate.Builder()
			.delete("?s ?p ?o")
			.graph(g1.resource(GRAPH1))
			.endpoint(URI.create(ENDPOINT_UPDATE))
			.build()
			.execute();
		
//		new SparqlUpdate.Builder()
//			.delete("?s ?p ?o")
//			.endpoint(ENDPOINT_UPDATE)
//			.build()
//			.execute();
	}
	
	@Test
	public void testFails() {
		log.info("No insert/where");
		try { new SparqlUpdate.Builder()
					.grafeo(g1)
					.build();
			fail("This should not succeeed");
		} catch (RuntimeException e) { log.info(""+ e); }
		log.info("No endpoint/grafeo");
		try { new SparqlUpdate.Builder()
					.insert("")
					.build();
			fail("This should not succeeed");
		} catch (RuntimeException e) { log.info(""+ e); }
		log.info("Both endpoint/grafeo");
		try { new SparqlUpdate.Builder()
					.insert("")
					.grafeo(g1)
					.endpoint(ENDPOINT_UPDATE)
					.build();
			fail("This should not succeeed");
		} catch (RuntimeException e) { log.info(""+ e); }
	}
	
	@Test
	public void testInsertData() {
		log.info("Pattern, local");
		{
			GrafeoImpl gTest = new GrafeoImpl();
			new SparqlUpdate.Builder()
				.insert(g1.getNTriples())
				.grafeo(gTest)
				.build().execute();
			GrafeoAssert.graphsAreEquivalent(g1, gTest);
		}
		{
			log.info("Triples, local");
			GrafeoImpl gTest = new GrafeoImpl();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.insert(g1)
				.grafeo(gTest)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			GrafeoAssert.graphsAreEquivalent(g1, gTest);
		}
		{
			log.info("Triples, remote, graph");
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.insert(g1)
				.graph(GRAPH1)
				.endpoint(ENDPOINT_UPDATE)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			
			GrafeoImpl gTest = new GrafeoImpl();
			gTest.readFromEndpoint(ENDPOINT_QUERY, GRAPH1);
			GrafeoAssert.graphsAreEquivalent(g1, gTest);
		}
	}

	/**
	 * 
	 */
	@Test
	public void testFoo() {
		new SparqlUpdate.Builder()
			.insert(g1.getNTriples())
			.graph(GRAPH1)
			.endpoint(ENDPOINT_UPDATE)
			.build()
			.execute();
		
		SparqlUpdate sparul = new SparqlUpdate.Builder()
			.insert(g2.getNTriples())
			.where(g1.getNTriples())
			.graph(GRAPH1)
			.endpoint(ENDPOINT_UPDATE)
			.build();
		log.info(sparul.toString());
		sparul.execute();
		GrafeoImpl gLoaded = new GrafeoImpl();
		gLoaded.readFromEndpoint(ENDPOINT_QUERY, GRAPH1);
		GrafeoAssert.graphsAreEquivalent(g3, gLoaded);
	}
	
	@Test
	public void testDeleteData() {
		{
			log.info("Pattern, local");
			GrafeoImpl gTest = (GrafeoImpl) g3.copy();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete(g1.getNTriples())
				.grafeo(gTest)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			GrafeoAssert.sizeEquals(gTest, g3.size() - g1.size());
		}
		{
			log.info("Triples, local");
			GrafeoImpl gTest = (GrafeoImpl) g3.copy();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete(g1)
				.grafeo(gTest)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			GrafeoAssert.sizeEquals(gTest, g3.size() - g1.size());
		}
		{
			log.info("Triples, remote, graph");
			SparqlUpdate sparul1 = new SparqlUpdate.Builder()
				.insert(g3)
				.graph(GRAPH1)
				.endpoint(ENDPOINT_UPDATE)
				.build();
			sparul1.execute();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete(g1)
				.graph(GRAPH1)
				.endpoint(ENDPOINT_UPDATE)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			
			GrafeoImpl gTest = new GrafeoImpl();
			gTest.readFromEndpoint(ENDPOINT_QUERY, GRAPH1);
			GrafeoAssert.sizeEquals(gTest, g3.size() - g1.size());
		}
		{
			log.info("Triples, remote, ntriples");
			SparqlUpdate sparul1 = new SparqlUpdate.Builder()
				.insert(g3.getNTriples())
				.graph(GRAPH1)
				.endpoint(ENDPOINT_UPDATE)
				.build();
			sparul1.execute();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete(g1.getNTriples())
				.graph(GRAPH1)
				.endpoint(ENDPOINT_UPDATE)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			
			GrafeoImpl gTest = new GrafeoImpl();
			gTest.readFromEndpoint(ENDPOINT_QUERY, GRAPH1);
			GrafeoAssert.sizeEquals(gTest, g3.size() - g1.size());
		}
	}
	
	@Test
	public void testUpsertData() {
		log.info("Pattern, local");
		{
			GrafeoImpl gTest = (GrafeoImpl) g3.copy();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete(g3.getNTriples())
				.insert(g1.getNTriples())
				.grafeo(gTest)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			GrafeoAssert.graphsAreEquivalent(g1, gTest);
		}
		{
			log.info("Pattern, remote");
			SparqlUpdate sparul1 = new SparqlUpdate.Builder()
				.insert(g3.getNTriples())
				.endpoint(ENDPOINT_UPDATE)
				.graph(GRAPH1)
				.build();
			sparul1.execute();
			
			
			GrafeoImpl gTest = new GrafeoImpl();
			gTest.addTriple(SUB3, PROP3, OBJ3);
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.insert(gTest)
				.delete(g1)
				.endpoint(ENDPOINT_UPDATE)
				.graph(GRAPH1)
				.build();
//			log.info(sparul.toString());
			sparul.execute();
			
			GrafeoImpl gCompare = new GrafeoImpl();
			gCompare.addTriple(SUB2, PROP2, OBJ2);
			gCompare.addTriple(SUB3, PROP3, OBJ3);
			GrafeoImpl gLoaded = new GrafeoImpl();
			gLoaded.readFromEndpoint(ENDPOINT_QUERY, GRAPH1);
			GrafeoAssert.graphsAreEquivalent(gCompare, gLoaded);
		}
	}
	
	@Test
	public void testPatternLocal() {
		
		log.info("Delete by pattern, local");
		{
			GrafeoImpl gTest = (GrafeoImpl) g3.copy();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete(String.format("?s <%s> <%s>", PROP2, OBJ2))
				.where(String.format("?s <%s> <%s>", PROP2, OBJ2))
				.grafeo(gTest)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			GrafeoAssert.graphsAreEquivalent(g1, gTest);
		}
		log.info("Delete by dummy pattern, local");
		{
			GrafeoImpl gTest = (GrafeoImpl) g3.copy();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete(String.format("?s <%s> <%s>", PROP2, OBJ2))
				.where("?s ?p ?o.")
				.grafeo(gTest)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			GrafeoAssert.graphsAreEquivalent(g1, gTest);
		}
		log.info("Expect fail: Delete by non-backreferring pattern, local");
		{
			GrafeoImpl gTest = (GrafeoImpl) g3.copy();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete(String.format("?s <%s> <%s>", PROP2, OBJ2))
				.where("?a ?b ?c")
				.grafeo(gTest)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			GrafeoAssert.graphsAreNotEquivalent(g1, gTest);
		}
		log.info("Delete, implicit where, local");
		{
			GrafeoImpl gTest = (GrafeoImpl) g3.copy();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete(String.format("?s <%s> <%s>", PROP2, OBJ2))
				.grafeo(gTest)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			GrafeoAssert.graphsAreEquivalent(g1, gTest);
		}
		{
			GrafeoImpl gTest = (GrafeoImpl) g3.copy();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.delete(g3.getNTriples())
				.insert(g1.getNTriples())
				.where(g3.getNTriples())
				.grafeo(gTest)
				.build();
			log.info(sparul.toString());
			sparul.execute();
			GrafeoAssert.graphsAreEquivalent(g1, gTest);
		}
	}
	
	@Test
	public void testPatternRemote() {
		
		log.info("Delete by pattern from graph, remote");
		{
			SparqlUpdate sparulInsert = new SparqlUpdate.Builder()
				.insert(g3.getNTriples())
				.graph(GRAPH1)
				.endpoint(ENDPOINT_UPDATE)
				.build();
			sparulInsert.execute();
			
			new SparqlUpdate.Builder()
				.delete(String.format("?s <%s> <%s>", PROP2, OBJ2))
				.where(String.format("?s <%s> <%s>", PROP2, OBJ2))
				.graph(GRAPH1)
				.endpoint(ENDPOINT_UPDATE)
				.build()
				.execute();
			
			GrafeoImpl gLoaded = new GrafeoImpl();
			gLoaded.readFromEndpoint(ENDPOINT_QUERY, GRAPH1);
			GrafeoAssert.graphsAreEquivalent(g1, gLoaded);
		}
		log.info("Pattern, local");
		{
			GrafeoImpl gTest = (GrafeoImpl) g1.copy();
			new SparqlUpdate.Builder()
				.insert(String.format("<%s> <%s> <%s>", SUB3, PROP3, OBJ3))
				.where(g1.getNTriples())
				.grafeo(gTest)
				.build()
				.execute();
			GrafeoImpl gCompare = (GrafeoImpl) g1.copy();
			gCompare.addTriple(SUB3, PROP3, OBJ3);
			GrafeoAssert.graphsAreEquivalent(gCompare, gTest);
		}
		log.info("Triples, remote, graph");
		{
			new SparqlUpdate.Builder()
				.insert(g1)
				.graph(URI.create(GRAPH1))
				.endpoint(ENDPOINT_UPDATE)
				.build()
				.execute();
			new SparqlUpdate.Builder()
				.insert(g2)
				.where(g1.getNTriples())
				.graph(GRAPH1)
				.endpoint(ENDPOINT_UPDATE)
				.build()
				.execute();
			
			GrafeoImpl gTest = new GrafeoImpl();
			gTest.readFromEndpoint(ENDPOINT_QUERY, GRAPH1);
			GrafeoAssert.graphsAreEquivalent(g3, gTest);
		}
		log.info("Triples, remote, graph");
		{
			new SparqlUpdate.Builder()
				.insert(g3)
				.graph(URI.create(GRAPH1))
				.endpoint(ENDPOINT_UPDATE)
				.build()
				.execute();
			new SparqlUpdate.Builder()
				.delete(g2)
				.where(g1.getNTriples())
				.graph(GRAPH1)
				.endpoint(ENDPOINT_UPDATE)
				.build()
				.execute();
			
			GrafeoImpl gTest = new GrafeoImpl();
			gTest.readFromEndpoint(ENDPOINT_QUERY, GRAPH1);
			GrafeoAssert.graphsAreEquivalent(g1, gTest);
		}
		log.info("Triples, remote, graph");
		{
			new SparqlUpdate.Builder()
				.insert(g1)
				.graph(URI.create(GRAPH1))
				.endpoint(ENDPOINT_UPDATE)
				.build()
				.execute();
			new SparqlUpdate.Builder()
				.delete(g1)
				.insert(g3)
				.where(g1.getNTriples())
				.graph(GRAPH1)
				.endpoint(ENDPOINT_UPDATE)
				.build()
				.execute();
			
			GrafeoImpl gTest = new GrafeoImpl();
			gTest.readFromEndpoint(ENDPOINT_QUERY, GRAPH1);
			GrafeoAssert.graphsAreEquivalent(g3, gTest);
		}
	}
	
	@Test
	public void setPrefixes() {
		log.info("Pattern, local");
		final String fnorp_base = "http://fnorp/";
		final String fnorp_prefix = "quux";
		final String fnorp_res1 = fnorp_base + "res1";
		final String fnorp_res2 = fnorp_base + "res2";
		final String fnorp_res3 = fnorp_base + "res3";
		{
			GrafeoImpl gInsert = new GrafeoImpl();
			gInsert.addTriple(fnorp_res1, fnorp_res2, fnorp_res3);
			GrafeoImpl gTest = new GrafeoImpl();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.insert("quux:res1 quux:res2 quux:res3")
				.prefix(fnorp_prefix, fnorp_base)
				.grafeo(gTest)
				.build();
			sparul.execute();
			assertThat(sparul.toString(), containsString("PREFIX " + fnorp_prefix +": <" + fnorp_base +">"));
			GrafeoAssert.graphsAreEquivalent(gInsert, gTest);
		}
		{
			HashMap<String, String> prefixMap = new HashMap<>();
			prefixMap.put(fnorp_prefix, fnorp_base);
			GrafeoImpl gInsert = new GrafeoImpl();
			gInsert.addTriple(fnorp_res1, fnorp_res2, fnorp_res3);
			GrafeoImpl gTest = new GrafeoImpl();
			SparqlUpdate sparul = new SparqlUpdate.Builder()
				.insert("quux:res1 quux:res2 quux:res3")
				.prefixes(prefixMap)
				.grafeo(gTest)
				.build();
			sparul.execute();
			assertThat(sparul.toString(), containsString("PREFIX " + fnorp_prefix +": <" + fnorp_base +">"));
			GrafeoAssert.graphsAreEquivalent(gInsert, gTest);
		}
	}
	
	@Test
	public void testStress() {
		
		long max = 50000;
		GrafeoImpl gTest = new GrafeoImpl();
		GrafeoImpl gLoaded = new GrafeoImpl();
		for (long i = 0; i < max ; i ++ ) {
			gTest.addTriple(BASE+"subj"+i, BASE+"prop"+i, BASE+"obj"+i);
		}
		
		new SparqlUpdate.Builder()
			.insert(gTest)
			.grafeo(gLoaded)
			.build()
			.execute();
		GrafeoAssert.graphsAreEquivalent(gTest, gLoaded);
	}
}
