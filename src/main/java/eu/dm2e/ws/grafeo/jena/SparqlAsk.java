package eu.dm2e.ws.grafeo.jena;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * Build a SPARQL ASK query
 * 
 * @author Konstantin Baierer
 *
 */
public class SparqlAsk {
	
	private Logger log = Logger.getLogger(getClass().getName());
	
	private String graph, endpoint, askClause;
	private GrafeoImpl grafeo;
	private Map<String, String> prefixes;
	private long timeout;

	public static class Builder {
		private String graph, endpoint, askClause;
		private GrafeoImpl grafeo;
		private Map<String, String> prefixes = new HashMap<>();
		private long timeoutMs = 5000;
		
		public Builder grafeo(GrafeoImpl s)  	{ this.grafeo = s; return this; }
		public Builder graph(String s)  	{ this.graph = s; return this; }
		public Builder endpoint(String s) 	{ this.endpoint = s; return this; }
		public Builder ask(String s) 		{ this.askClause = s; return this; }
		public Builder timeout(long t)     { this.timeoutMs = t; return this; }
		
        public Builder prefixes(Map<String,String> prefixes)	{ this.prefixes.putAll(prefixes); return this; }
        public Builder prefix(String prefix, String value) 		{ this.prefixes.put(prefix, value); return this; }
		
		public SparqlAsk build() { return new SparqlAsk(this); } 
	}

	public SparqlAsk(Builder builder) {
		if (null == builder.askClause) 
			throw new IllegalArgumentException("Must set askClause in Query Builder.");
		
		this.prefixes = builder.prefixes;
		this.askClause = builder.askClause;
		this.graph = builder.graph;
		this.timeout = builder.timeoutMs;
		
		if (null != grafeo && null != endpoint) {
			throw new IllegalArgumentException("Must set either grafeo or endpoint, not both.");
		} else if (null != builder.endpoint) {
			this.endpoint = builder.endpoint;
		} else if (null != builder.grafeo) {
			this.grafeo = builder.grafeo;
			this.prefixes.putAll(this.grafeo.getNamespacesUsed());
		} else {
			throw new IllegalArgumentException("Must set exactly one of grafeo or endpoint");
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
        if (!prefixes.keySet().isEmpty()) {
            for (String prefix:prefixes.keySet()) {
                sb.append("PREFIX ")
                  .append(prefix)
                  .append(": <")
                  .append(prefixes.get(prefix))
                  .append(">\n");
            }
            sb.append("\n");
        }
		sb.append("ASK { ");
		if (null != this.graph) {
			sb.append("GRAPH <");
			sb.append(this.graph);
			sb.append("> { ");
		}
		sb.append(askClause);
		if (null != this.graph) {
			sb.append(" }");
		}
		sb.append(" }");
		return sb.toString();
	}
	
	public boolean execute() {
        Query query = QueryFactory.create(this.toString());
        log.fine("ASK query: " + query.toString());
        QueryExecution qe = null;
		if (null != endpoint) {
	        qe = QueryExecutionFactory.createServiceRequest(endpoint, query);
		} else {
	        qe = QueryExecutionFactory.create(query, grafeo.getModel());
		}
		qe.setTimeout(timeout);
        return qe.execAsk();
	}
}
