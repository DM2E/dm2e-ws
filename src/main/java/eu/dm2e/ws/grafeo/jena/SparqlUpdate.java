package eu.dm2e.ws.grafeo.jena;

import java.net.URI;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import eu.dm2e.ws.grafeo.Grafeo;

public class SparqlUpdate {
	
	private Logger log = Logger.getLogger(getClass().getName());
	
	private String graph, endpoint, deleteClause, insertClause, whereClause;

	public static class Builder {
		private String graph, endpoint, deleteClause, insertClause, whereClause;
		
		public Builder graph(URI s)     	{ this.graph = s.toString(); return this; }
		public Builder graph(String s)  	{ this.graph = s; return this; }
		
		public Builder endpoint(String s) 	{ this.endpoint = s; return this; }
		
		public Builder delete(String s) 	{ this.deleteClause = s; return this; }
		public Builder delete(Grafeo s) 	{ this.deleteClause = s.getNTriples(); return this; }
		
		public Builder insert(String s) 	{ this.insertClause = s; return this; }
		public Builder insert(Grafeo s) 	{ this.insertClause = s.getNTriples(); return this; }
		
		public Builder where(String s) 		{ this.whereClause = s; return this; }
		
		public SparqlUpdate build() 		{ return new SparqlUpdate(this); } 
	}

	public SparqlUpdate(Builder builder) {
		this.graph = builder.graph;
		this.endpoint = builder.endpoint;
		this.deleteClause = builder.deleteClause;
		this.insertClause = builder.insertClause;
		this.whereClause = builder.whereClause;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (null != graph) 			sb.append(String.format("WITH <%s>", graph));
		if (null != deleteClause)	sb.append(String.format(" DELETE { %s }", deleteClause));
		if (null != insertClause) 	sb.append(String.format(" INSERT { %s }", insertClause));
		
		if (null != whereClause)	sb.append(String.format(" WHERE { %s }", whereClause));
		else if (null != deleteClause)	sb.append(String.format(" WHERE { %s }", deleteClause));
		else sb.append("WHERE {}");
		
		return sb.toString();
	}
	
	public void execute() {
		if (null == endpoint)
			throw new IllegalArgumentException("Must set endpoint to perform query.");
        log.info("Modify query: " + toString());
        UpdateRequest update = UpdateFactory.create();
        for (Entry<String, String> namespaceMapping : new GrafeoImpl().namespaces.entrySet()) {
        	update.setPrefix(namespaceMapping.getKey(), namespaceMapping.getValue());
        }
        update.add(toString());
        UpdateProcessor exec = UpdateExecutionFactory.createRemoteForm(update, endpoint);
        exec.execute();
	}
}
