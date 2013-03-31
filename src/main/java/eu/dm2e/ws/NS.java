package eu.dm2e.ws;


public final class NS {
	
	public static final String 
			  DM2E = Config.getString("dm2e.ns.dm2e")
			, RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			, DM2ELOG = "http://onto.dm2e.eu/logging#"
			, ENDPOINT = Config.getString("dm2e.ws.sparql_endpoint")
			, ENDPOINT_STATEMENTS = Config.getString("dm2e.ws.sparql_endpoint_statements")
			;
}
