package eu.dm2e.ws.grafeo.jena;



import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.junit.GrafeoAssert;

public class GrafeoImplITCase extends OmnomTestCase {
	
	public static final String BASE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDF_TYPE = BASE + "type";
	
	
	GrafeoImpl g = new GrafeoImpl();
	List<GResource> subjList = new ArrayList<>();
	List<GResource> predList = new ArrayList<>();
	List<GResource> objList = new ArrayList<>();
	List<String> graphList = new ArrayList<>();
	
	@Before
	public void setUpStuff() {
		g.empty();
		for (int i = 0 ; i < 5 ; i++) {
			subjList.add(g.resource(BASE + "subj" + i));
			predList.add(g.resource(BASE + "pred" + i));
			objList.add(g.resource(BASE + "obj" + i));
			graphList.add(BASE + "graph"+ i);
		}
	}

	@Test
	public void testReadTriplesFromEndpoint() throws Exception {
		g.addTriple(subjList.get(0), RDF_TYPE, objList.get(0));
		g.addTriple(subjList.get(1), RDF_TYPE, objList.get(0));
		g.addTriple(subjList.get(2), RDF_TYPE, objList.get(0));
		GrafeoImpl gCompare = (GrafeoImpl) g.copy();
		
		g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), graphList.get(0));
		
		
		GrafeoImpl gLoaded = new GrafeoImpl();
		gLoaded.readTriplesFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), null, RDF_TYPE, objList.get(0));
		
		GrafeoAssert.graphsAreEquivalent(gCompare, gLoaded);
		
		
	}
	
	

}
