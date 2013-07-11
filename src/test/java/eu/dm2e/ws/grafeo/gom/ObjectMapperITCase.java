package eu.dm2e.ws.grafeo.gom;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
import eu.dm2e.ws.api.pojos.IntegerPojo;
import eu.dm2e.ws.api.pojos.LiteralListPojo;
import eu.dm2e.ws.api.pojos.NestedSetPojo;
import eu.dm2e.ws.api.pojos.ResourceListPojo;
import eu.dm2e.ws.api.pojos.SetAndListPojo;
import eu.dm2e.ws.api.pojos.SetPojo;
import eu.dm2e.ws.api.pojos.UriResourcePojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GResourceImpl;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.junit.GrafeoAssert;

public class ObjectMapperITCase extends OmnomTestCase {
	
	@Test
	public void testBlankLiteralList() {
		
		LiteralListPojo list = new LiteralListPojo();
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
		LiteralListPojo listPojo2 = g.getObjectMapper().getObject(LiteralListPojo.class, topBlank);
		GrafeoAssert.numberOfResourceStatements(listPojo2.getGrafeo(), 3, null, NS.RDF.PROP_TYPE, NS.CO.CLASS_ITEM);
		log.info(listPojo2.getTerseTurtle());
		assertThat(list.getLongList(), is(listPojo2.getLongList()));
		GrafeoAssert.graphsAreEquivalent(list.getGrafeo(), listPojo2.getGrafeo());
	}
	
	@Test
	public void testCombinedSetAndListRename() {
		SetAndListPojo pojo = new SetAndListPojo();
		pojo.getList().add(new IntegerPojo(0));
		pojo.getList().add(new IntegerPojo(1));
		pojo.getList().add(new IntegerPojo(2));
		
		pojo.getSet().add(new IntegerPojo(3));
		pojo.getSet().add(new IntegerPojo(4));
		pojo.getSet().add(new IntegerPojo(5));
		Grafeo g1 = new GrafeoImpl();
		g1.getObjectMapper().addObject(pojo);
		
		Grafeo g2 = pojo.getGrafeo();
		
		GrafeoAssert.graphsAreEquivalent(g1, g2);
		
		log.info(g1.getTerseTurtle());
		g1.findTopBlank("co:List").rename("omnom:FORK");
		log.info(g2.getTerseTurtle());
		
		GrafeoAssert.sizeEquals(g1, g2);
		GrafeoAssert.graphsAreStructurallyEquivalent(g1, g2);
	}

	
	@Test
	public void testBlankResourceList() {
		
		ResourceListPojo list = new ResourceListPojo();
		list.getIntegerResourceList().add(new IntegerPojo(5));
		list.getIntegerResourceList().add(new IntegerPojo(6));
		list.getIntegerResourceList().add(new IntegerPojo(7));
		
		log.info("Serializing manually");
		Grafeo g = new GrafeoImpl();
		g.getObjectMapper().addObject(list);
		GrafeoAssert.numberOfResourceStatements(g, 3, null, NS.RDF.PROP_TYPE, NS.CO.CLASS_ITEM);
		log.info(g.getTerseTurtle());
		
		log.info("Serializing from pojo");
		Grafeo g2 = list.getGrafeo();
		GrafeoAssert.graphsAreEquivalent(g, g2);
		
		log.info("De-Serializing");
		GResource topBlank = g.findTopBlank(list.getRDFClass().value());
		ResourceListPojo listPojo2 = g.getObjectMapper().getObject(ResourceListPojo.class, topBlank);
		GrafeoAssert.numberOfResourceStatements(listPojo2.getGrafeo(), 3, null, NS.RDF.PROP_TYPE, NS.CO.CLASS_ITEM);
		assertThat(listPojo2.getIntegerResourceList(), is(list.getIntegerResourceList()));
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
		assertTrue(g.containsTriple(pojo_uri, "omnom:some_number", g.literal(5)));
		assertTrue(g.containsTriple(pojo_uri, "omnom:some_number", g.literal(11111)));
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
//		log.warn(g2.getCanonicalNTriples());
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
	
	@Test
	@Ignore
	public void infiniteRecursion() throws Exception {
		WorkflowPojo wf = new WorkflowPojo();
		final long initialSize;
		{
			WorkflowPositionPojo pos1 = new WorkflowPositionPojo();
			wf.addPosition(pos1);
			
			wf.publishToService(client.getWorkflowWebTarget());
			assertNotNull(wf.getId());
			initialSize = wf.getGrafeo().size();
		}
		{
			WorkflowPojo wf2 = wf.getGrafeo().getObjectMapper().getObject(WorkflowPojo.class, wf.getId());
			assertNotNull(wf2.getId());
			List<String> x = wf2.getGrafeo().diffUnskolemizedNTriples(wf.getGrafeo());
			log.info(x.get(0) + x.get(1));
			assertEquals(initialSize, wf2.getGrafeo().size());
		}
		{
			WorkflowPojo wf2 = client.loadPojoFromURI(WorkflowPojo.class, wf.getId());
			assertEquals(initialSize, wf2.getGrafeo().size());
		}
		{
			WorkflowPojo wf2 = new WorkflowPojo();
			wf2.loadFromURI(wf.getId());
			log.info(wf2.getTerseTurtle());
			assertEquals(initialSize, wf2.getGrafeo().size());
		}
		log.info(wf.getTerseTurtle());
	}
	
	@Test
	public void testURI() {
		
		String uriName1 = "http://foo";
		String uriName2 = "http://bar";
		
		UriResourcePojo uriRes = new UriResourcePojo();
		GrafeoImpl g = new GrafeoImpl();
		
		GResourceImpl pojoRes = g.createBlank();
		GResourceImpl integerPojoRes = g.createBlank();
		GResourceImpl listRes = g.createBlank();
		GResourceImpl itemRes1 = g.createBlank();
		GResourceImpl itemRes2 = g.createBlank();
		
		{
			pojoRes.set(UriResourcePojo.PROP_POJO_RESOURCE, integerPojoRes);
			pojoRes.set("rdf:type", UriResourcePojo.CLASS_NAME);
			integerPojoRes.set(IntegerPojo.PROP_SOME_NUMBER, g.literal(5));
			integerPojoRes.set("rdf:type", IntegerPojo.CLASS_NAME);
			
			uriRes.setPojoResource(new IntegerPojo(5));
			
			GrafeoAssert.graphsAreEquivalent(g, uriRes.getGrafeo());
		}
		{
			pojoRes.set(UriResourcePojo.PROP_URI_RESOURCE, uriName1);
			uriRes.setUriResource(URI.create(uriName1));
			
			GrafeoAssert.graphsAreEquivalent(g, uriRes.getGrafeo());
		}
		{
			pojoRes.set(UriResourcePojo.PROP_URI_RESOURCE_SET, uriName1);
			pojoRes.set(UriResourcePojo.PROP_URI_RESOURCE_SET, uriName2);
			uriRes.getUriResourceSet().add(URI.create(uriName1));
			uriRes.getUriResourceSet().add(URI.create(uriName2));
			
			GrafeoAssert.graphsAreEquivalent(g, uriRes.getGrafeo());
		}
		{
			pojoRes.set(UriResourcePojo.PROP_URI_RESOURCE_LIST, listRes);
			listRes.set("rdf:type", NS.CO.CLASS_LIST);
			listRes.set(NS.CO.PROP_FIRST_ITEM, itemRes1);
			listRes.set(NS.CO.PROP_SIZE, g.literal(2));
			
			itemRes1.set("rdf:type", NS.CO.CLASS_ITEM);
			itemRes1.set(NS.CO.PROP_INDEX, g.literal(0));
			itemRes1.set(NS.CO.PROP_ITEM_CONTENT, uriName1);
			itemRes1.set(NS.CO.PROP_NEXT_ITEM, itemRes2);
			
			listRes.set(NS.CO.PROP_LAST_ITEM, itemRes2);
			itemRes2.set("rdf:type", NS.CO.CLASS_ITEM);
			itemRes2.set(NS.CO.PROP_INDEX, g.literal(1));
			itemRes2.set(NS.CO.PROP_ITEM_CONTENT, uriName2);
			
			uriRes.getUriResourceList().add(URI.create(uriName1));
			uriRes.getUriResourceList().add(URI.create(uriName2));
			
			log.error(g.getTerseTurtle());
			GrafeoAssert.graphsAreEquivalent(g, uriRes.getGrafeo());
		}
		{
			log.info("De-serializing");
			GrafeoImpl g2 = new GrafeoImpl();
			g2.getObjectMapper().addObject(uriRes);
			
			GrafeoAssert.graphsAreEquivalent(g, g2);
		}
	}
}
