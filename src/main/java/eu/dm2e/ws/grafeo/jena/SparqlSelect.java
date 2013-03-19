package eu.dm2e.ws.grafeo.jena;

import java.util.logging.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

public class SparqlSelect {
	
	Logger log = Logger.getLogger(getClass().getName());
	
	private String graph, endpoint, selectClause, orderBy, limit, whereClause;

	public static class Builder {
		private String graph, endpoint, selectClause, orderBy, limit, whereClause;
		
		public Builder graph(String s)  	{ this.graph = s; return this; }
		public Builder endpoint(String s) 	{ this.endpoint = s; return this; }
		public Builder orderBy(String s) 	{ this.orderBy = s; return this; }
		public Builder limit(String s) 		{ this.limit = s; return this; }
		public Builder select(String s) 	{ this.selectClause = s; return this; }
		public Builder where(String s) 		{ this.whereClause = s; return this; }
		
		public SparqlSelect build() { return new SparqlSelect(this); } 
	}

	public SparqlSelect(Builder builder) {
		if (null == builder.selectClause || null == builder.whereClause) 
			throw new IllegalArgumentException("Must set constructClause and whereClause in Query Builder.");
		this.graph = builder.graph;
		this.endpoint = builder.endpoint;
		this.orderBy = builder.orderBy;
		this.limit = builder.limit;
		this.whereClause = builder.whereClause;
		this.selectClause = builder.selectClause;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("SELECT %s", selectClause));
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
		if (null != orderBy)	    sb.append(String.format(" ORDER BY %s ", orderBy));
		if (null != limit) 			sb.append(String.format(" LIMIT %s", limit));
		return sb.toString();
	}
	
	public ResultSet execute() {
		if (null == endpoint)
			throw new IllegalArgumentException("Must set endpoint to perform query.");
		String queryStr = toString();
        log.info("SELECT query: " + queryStr);
        Query query = QueryFactory.create(queryStr);
        QueryExecution exec = QueryExecutionFactory.createServiceRequest(endpoint, query);
        return exec.execSelect();
	}
}
