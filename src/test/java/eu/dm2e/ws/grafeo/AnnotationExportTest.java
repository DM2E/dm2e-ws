package eu.dm2e.ws.grafeo;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.logging.Logger;

import org.junit.Test;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomUnitTest;
import eu.dm2e.ws.grafeo.jena.GResourceImpl;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.junit.GrafeoAssert;
import eu.dm2e.ws.grafeo.test.IntegerPojo;
import eu.dm2e.ws.grafeo.test.LiteralList;
import eu.dm2e.ws.grafeo.test.ResourceListPojo;
import eu.dm2e.ws.grafeo.test.NestedSetPojo;
import eu.dm2e.ws.grafeo.test.SetPojo;

public class AnnotationExportTest extends OmnomUnitTest {
	
	Logger log = Logger.getLogger(getClass().getName());

	@Test
	public void testBlankLiteralList() {
		
		LiteralList list = new LiteralList();
		list.getLongList().add(5L);
		list.getLongList().add(6L);
		list.getLongList().add(7L);		log.info("Serializing");
		
		log.info("Serializing");
		Grafeo g = new GrafeoImpl();
		g.getObjectMapper().addObject(list);
		GrafeoAssert.numberOfResourceStatements(g, 3, null, NS.RDF.PROP_TYPE, NS.CO.CLASS_ITEM);
		log.info(g.getTerseTurtle());
		
		log.info("De-Serializing");
		GResource topBlank = g.findTopBlank(list.getRDFClass().value());
		LiteralList listPojo2 = g.getObjectMapper().getObject(LiteralList.class, topBlank);
		GrafeoAssert.numberOfResourceStatements(listPojo2.getGrafeo(), 3, null, NS.RDF.PROP_TYPE, NS.CO.CLASS_ITEM);
		log.info(listPojo2.getTerseTurtle());
		assertThat(list.getLongList(), is(listPojo2.getLongList()));
		GrafeoAssert.graphsAreEquivalent(list.getGrafeo(), listPojo2.getGrafeo());
	}

	
	@Test
	public void testBlankResourceList() {
		
		ResourceListPojo list = new ResourceListPojo();
		list.getIntegerResourceList().add(new IntegerPojo(5));
		list.getIntegerResourceList().add(new IntegerPojo(6));
		list.getIntegerResourceList().add(new IntegerPojo(7));
		
		log.info("Serializing");
		Grafeo g = new GrafeoImpl();
		g.getObjectMapper().addObject(list);
		GrafeoAssert.numberOfResourceStatements(g, 3, null, NS.RDF.PROP_TYPE, NS.CO.CLASS_ITEM);
		log.info(g.getTerseTurtle());
		
		log.info("De-Serializing");
		GResource topBlank = g.findTopBlank(list.getRDFClass().value());
		ResourceListPojo listPojo2 = g.getObjectMapper().getObject(ResourceListPojo.class, topBlank);
		GrafeoAssert.numberOfResourceStatements(listPojo2.getGrafeo(), 3, null, NS.RDF.PROP_TYPE, NS.CO.CLASS_ITEM);
		log.info(listPojo2.getTerseTurtle());
		assertThat(list.getIntegerResourceList(), is(listPojo2.getIntegerResourceList()));
		GrafeoAssert.graphsAreEquivalent(list.getGrafeo(), listPojo2.getGrafeo());
	}
	
	@Test
	public void testSetExport() {
		SetPojo pojo = new SetPojo();
		String pojo_uri = "http://foo";
		pojo.setIdURI(pojo_uri);
		pojo.getLiteralSet().add(5);
		pojo.getLiteralSet().add(44);
		pojo.getLiteralSet().add(333);
		pojo.getLiteralSet().add(2222);
		pojo.getLiteralSet().add(11111);
//		log.info(pojo.getLiteralSet().toString());
		Grafeo g = new GrafeoImpl();
		g.getObjectMapper().addObject(pojo);
		log.info(g.getTurtle());
//		log.info(""+g.size());
		assertTrue(g.containsStatementPattern(pojo_uri, "omnom:some_number", g.literal(5)));
		assertTrue(g.containsStatementPattern(pojo_uri, "omnom:some_number", g.literal(11111)));
	}
	
	@Test
	public void testListExport() {
		ResourceListPojo pojo = new ResourceListPojo();
		String pojo_uri = "http://foo";
		pojo.setId(pojo_uri);
		pojo.getIntegerResourceList().add(new IntegerPojo("http://item1", 5));
		pojo.getIntegerResourceList().add(new IntegerPojo("http://item2", 44));
//		pojo.getIntegerResourceList().add(new IntegerPojo("http://item3", 333));
//		pojo.getIntegerResourceList().add(new IntegerPojo("http://item4", 2222));
//		pojo.getIntegerResourceList().add(new IntegerPojo("http://item5", 11111));
//		log.info(pojo.getLiteralSet().toString());
		Grafeo g = new GrafeoImpl();
		g.getObjectMapper().addObject(pojo);
		log.info(g.getNTriples());
//		log.info(""+g.size());
//		assertTrue(g.containsStatementPattern(pojo_uri, "omnom:some_number", g.literal(5)));
//		assertTrue(g.containsStatementPattern(pojo_uri, "omnom:some_number", g.literal(11111)));
	}
	
	@Test
	public void testSetImport() {
		GrafeoImpl g = new GrafeoImpl();
		String uri = "http://foo";
//		GResource res = g.resource(uri);
		g.addTriple(uri, "rdf:type", "omnom:SetPojoTest");
		g.addTriple(uri, "omnom:some_number", g.literal(5));
		g.addTriple(uri, "omnom:some_number", g.literal(100));
//		Set<GValue> set = res.getAll("omnom:some_number");
//		log.info("foo: " + set);
		SetPojo o = g.getObjectMapper().getObject(SetPojo.class, g.resource(uri));
		log.info("" + o.getLiteralSet());
		GrafeoImpl g2 = new GrafeoImpl();
		g2.getObjectMapper().addObject(o);
		assertEquals(g.getCanonicalNTriples(), g2.getCanonicalNTriples());
//		log.info(g2.getNTriples());
//		log.info(" " + o.getSome_number());
	}

	@Test
	public void testListImport() {
		GrafeoImpl g1 = new GrafeoImpl();
		String uri = "http://foo";
		GResource res = g1.resource(uri);
		ResourceListPojo pojo = new ResourceListPojo();
		pojo.setId(uri);
		pojo.getIntegerResourceList().add(new IntegerPojo(uri+"/x1", 1));
		pojo.getIntegerResourceList().add(new IntegerPojo(uri+"/x2", 2));
		g1.getObjectMapper().addObject(pojo);
		log.info(g1.getTurtle());
		
		GrafeoImpl g2 = new GrafeoImpl();
		ResourceListPojo inPojo = g1.getObjectMapper().getObject(ResourceListPojo.class, res);
		g2.getObjectMapper().addObject(inPojo);
//		log.warning(g2.getCanonicalNTriples());
		GrafeoAssert.graphsAreEquivalent(g1, g2);
//		assertEquals(g1.getCanonicalNTriples(), g2.getCanonicalNTriples());
	}
	
	@Test
	public void testBlankResourceSet() {
		NestedSetPojo nset = new NestedSetPojo();
		nset.getNumbers().add(new IntegerPojo(1));
		nset.getNumbers().add(new IntegerPojo(0));
		nset.getNumbers().add(new IntegerPojo(8));
		GrafeoImpl g = new GrafeoImpl();
		{
			log.info("Test addSetObject");
			g.getObjectMapper().addObject(nset);
			log.info(g.getTerseTurtle());
			GrafeoAssert.numberOfResourceStatements(g, 3, null, "rdf:type", "omnom:IntegerPojo");
		}
		{
			log.info("Test getSetObject");
			GResourceImpl blank1 = g.findTopBlank("omnom:NestedSet");
			NestedSetPojo nset2 = g.getObjectMapper().getObject(NestedSetPojo.class, blank1);
			log.info("AND BACK: " + nset2.getGrafeo().getTerseTurtle());
			GrafeoAssert.graphsAreEquivalent(g, nset2.getGrafeo());
		}
		{
			log.info("Verify with hand-made grafeo");
			GrafeoImpl g2 = new GrafeoImpl();
			GResource nsetRes = g2.createBlank();
			GResource itemRes1 = g2.createBlank();
			GResource itemRes2 = g2.createBlank();
			GResource itemRes3 = g2.createBlank();
			
			nsetRes.set("rdf:type", g2.resource("omnom:NestedSet"));
			for (GResource itemResN : Arrays.asList(itemRes1, itemRes2, itemRes3) ) {
				itemResN.set("rdf:type", g.resource("omnom:IntegerPojo"));
				nsetRes.set("omnom:ze_numbaz", itemResN);
			}
			itemRes1.set("omnom:some_number", g.literal(0));
			itemRes2.set("omnom:some_number", g.literal(1));
			itemRes3.set("omnom:some_number", g.literal(8));
			GrafeoAssert.graphsAreEquivalent(g, g2);
		}
	}
}
