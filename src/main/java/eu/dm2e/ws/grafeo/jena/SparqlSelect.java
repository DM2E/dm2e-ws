package eu.dm2e.ws.grafeo.jena;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

public class SparqlSelect {
	
	private Logger log = Logger.getLogger(getClass().getName());
	
	private String graph, endpoint, selectClause, orderBy, whereClause;
	private long limit = 0;
	private GrafeoImpl grafeo;
    private Map<String,String> prefixes = new HashMap<>();

	public static class Builder {
		private String graph, endpoint, selectClause, orderBy, whereClause;
		private long limit;
        private GrafeoImpl grafeo;
        private Map<String,String> prefixes = new HashMap<>();
		
		public Builder graph(String s)  	{ this.graph = s; return this; }
		public Builder graph(URI s)  	{ this.graph = s.toString(); return this; }
		
		public Builder endpoint(String s) 	{ this.endpoint = s; return this; }
		public Builder endpoint(URI s)  	{ this.endpoint = s.toString(); return this; }
		
		public Builder orderBy(String s) 	{ this.orderBy = s; return this; }
		public Builder limit(long n) 		{ this.limit = n; return this; }
		public Builder select(String s) 	{ this.selectClause = s; return this; }
        public Builder where(String s) 		{ this.whereClause = s; return this; }
        
        public Builder grafeo(GrafeoImpl g) { this.grafeo = g; return this; }
        
        public Builder prefixes(Map<String,String> prefixes)	{ this.prefixes.putAll(prefixes); return this; }
        public Builder prefix(String prefix, String value) 		{ this.prefixes.put(prefix, value); return this; }

        public SparqlSelect build() { return new SparqlSelect(this); }
	}

	public SparqlSelect(Builder builder) {
		if (null == builder.whereClause) 
			throw new IllegalArgumentException("Must set where for SELECT.");
		
		this.graph = builder.graph;
		this.orderBy = builder.orderBy;
		this.limit = builder.limit;
		this.whereClause = builder.whereClause;
		this.selectClause = builder.selectClause == null ? "*" : builder.selectClause;
        this.prefixes = builder.prefixes;
        
		if (null != builder.endpoint && null != builder.grafeo)
			throw new IllegalArgumentException("Must set endpoint or grafeo, not both.");
		else if (null != builder.endpoint) 
			this.endpoint = builder.endpoint;
		else if (null != builder.grafeo) {
			this.grafeo = builder.grafeo;
			this.prefixes.putAll(this.grafeo.getNamespacesUsed());
		} else 
			throw new IllegalArgumentException("Must set exactly one of endpoint or grafeo.");
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
        if (!prefixes.keySet().isEmpty()) {
            for (String prefix:prefixes.keySet()) {
                sb.append("PREFIX ").append(prefix).append(": <").append(prefixes.get(prefix)).append(">\n");
            }
            sb.append("\n");
        }
        sb.append("SELECT ");
		sb.append(String.format("%s", selectClause));
		sb.append(" WHERE { ");

		/**
		 * NOTE this is not really accurate. Could be that the WHERE clause
		 * contains un-GRAPH-ed statments. In that case, leave out the graph
		 * prop and set WHERE accordingly (kb)
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
		if (null != orderBy)	    sb.append(String.format(" ORDER BY %s ", orderBy));
		if (limit > 0) 			sb.append(String.format(" LIMIT %s", limit));
		return sb.toString();
	}
	
	public ResultSet execute() {
		String queryStr = toString();
        log.info("SELECT query: " + queryStr);
        Query query = QueryFactory.create(queryStr);
        QueryExecution exec;
        if (null != endpoint) {
        	exec = QueryExecutionFactory.createServiceRequest(endpoint, query);
        } else {
        	exec = QueryExecutionFactory.create(query, grafeo.getModel());
        }
        return exec.execSelect();
	}
}
