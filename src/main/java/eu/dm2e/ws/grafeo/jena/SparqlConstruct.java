package eu.dm2e.ws.grafeo.jena;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;

import java.util.logging.Logger;

public class SparqlConstruct {
	
	private Logger log = Logger.getLogger(getClass().getName());
	
	private String graph, endpoint, constructClause, whereClause;

	public static class Builder {
		private String graph, endpoint, constructClause, whereClause;
		
		public Builder graph(String s)  	{ this.graph = s; return this; }
		public Builder endpoint(String s) 	{ this.endpoint = s; return this; }
		public Builder construct(String s) 	{ this.constructClause = s; return this; }
		public Builder where(String s) 		{ this.whereClause = s; return this; }
		
		public SparqlConstruct build() { return new SparqlConstruct(this); } 
	}

	public SparqlConstruct(Builder builder) {
		if (null == builder.constructClause || null == builder.whereClause) 
			throw new IllegalArgumentException("Must set constructClause and whereClause in Query Builder.");
		this.graph = builder.graph;
		this.endpoint = builder.endpoint;
		this.whereClause = builder.whereClause;
		this.constructClause = builder.constructClause;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
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
	
	public void execute(GrafeoImpl g) {
		if (null == endpoint)
			throw new IllegalArgumentException("Must set endpoint to perform query.");
        log.info("CONSTRUCT query: " + toString());
        Query query = QueryFactory.create(toString());
        log.info("Query: " + toString());
        QueryExecution exec = QueryExecutionFactory.createServiceRequest(endpoint, query);
        exec.execConstruct(g.getModel());
	}
}
