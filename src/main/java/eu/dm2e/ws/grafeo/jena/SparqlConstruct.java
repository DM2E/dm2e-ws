package eu.dm2e.ws.grafeo.jena;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;

public class SparqlConstruct {
	
	private Logger log = Logger.getLogger(getClass().getName());
	
	private String graph, endpoint, constructClause, whereClause;
	private GrafeoImpl grafeo;
	private Map<String, String> prefixes;

	public static class Builder {
		private String graph, endpoint, constructClause, whereClause;
		private GrafeoImpl grafeo;
		private Map<String, String> prefixes = new HashMap<>();
		
		public Builder grafeo(GrafeoImpl s)	{ this.grafeo = s; return this; }
		public Builder graph(String s)  	{ this.graph = s; return this; }
		public Builder endpoint(String s) 	{ this.endpoint = s; return this; }
		public Builder construct(String s) 	{ this.constructClause = s; return this; }
		public Builder where(String s) 		{ this.whereClause = s; return this; }
		
        public Builder prefixes(Map<String,String> prefixes)	{ this.prefixes.putAll(prefixes); return this; }
        public Builder prefix(String prefix, String value) 		{ this.prefixes.put(prefix, value); return this; }
		
		public SparqlConstruct build() { return new SparqlConstruct(this); } 
	}

	public SparqlConstruct(Builder builder) {
		if (null == builder.constructClause) 
			throw new IllegalArgumentException("Must set constructClause in Query Builder.");
		
		this.prefixes = builder.prefixes;
		this.graph = builder.graph;
		this.constructClause = builder.constructClause;
		this.whereClause = builder.whereClause;
		if (null == whereClause) 
			whereClause = constructClause;
		
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
		sb.append(String.format("CONSTRUCT { %s }", constructClause));
		sb.append(" WHERE { ");

		/**
		 * NOTE this is not really accurate. Could be that the WHERE clause
		 * contains un-GRAPH-ed statments. In that case, leave out the graph
		 * prop and set WHERE accordingly
		 */
		if (null != graph) {
			sb.append(String.format(" GRAPH <%s> { ", graph));
			sb.append(whereClause);
			sb.append(" } .");
		}
		else {
			sb.append(whereClause);
		}
		sb.append(" } ");
		return sb.toString();
	}
	public void execute() {
		if (null == grafeo)
			throw new IllegalArgumentException("Must set grafeo in the builder or use execute(GrafeoImpl g).");
        log.fine("CONSTRUCT query (built): " + toString());
        Query query = QueryFactory.create(toString());
        log.fine("CONSTRUCT query (Jena): " + query);
        QueryExecution exec = QueryExecutionFactory.create(query, grafeo.getModel());
        exec.execConstruct(grafeo.getModel());
	}
	
	public void execute(GrafeoImpl g) {
        Query query = QueryFactory.create(toString());
        log.info("CONSTRUCT query: " + query);
        QueryExecution exec;
		if (null != endpoint) {
			exec = QueryExecutionFactory.createServiceRequest(endpoint, query);
		} else {
			exec = QueryExecutionFactory.create(query, grafeo.getModel());
		}
        exec.execConstruct(g.getModel());
	}
}
