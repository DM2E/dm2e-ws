package eu.dm2e.ws.grafeo.jena;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;

public class SparqlUpdate {
	
	private Logger log = Logger.getLogger(getClass().getName());
	
	private String graph, endpoint, deleteClause, insertClause, whereClause;
	private GrafeoImpl grafeo;
	private Map<String, String> prefixes;
	private boolean insertDataFlag, deleteDataFlag;

	public static class Builder {
		private String graph, endpoint, deleteClause, insertClause, whereClause;
		private GrafeoImpl grafeo;
        private Map<String,String> prefixes = new HashMap<>();
        private boolean insertDataFlag, deleteDataFlag;
		
		public Builder graph(String s)  	{ this.graph = s; return this; }
		public Builder graph(URI s)     	{ this.graph = s.toString(); return this; }
		public Builder graph(GResource s)  	{ this.graph = s.getUri(); return this; }
		
		public Builder endpoint(String s) 	{ this.endpoint = s; return this; }
		public Builder endpoint(URI s)     	{ this.endpoint = s.toString(); return this; }
		
		public Builder delete(String s) 	{ this.deleteClause = s; return this; }
		public Builder delete(Grafeo s) 	{ this.deleteClause = s.getNTriples(); deleteDataFlag = true; return this; }
		
		public Builder insert(String s) 	{ this.insertClause = s; return this; }
		public Builder insert(Grafeo s) 	{ this.insertClause = s.getNTriples(); insertDataFlag = true; return this; }
		
		public Builder where(String s) 		{ this.whereClause = s; return this; }
		
		public Builder grafeo(GrafeoImpl g) { this.grafeo = g; return this; }
		
        public Builder prefixes(Map<String,String> prefixes)	{ this.prefixes.putAll(prefixes); return this; }
        public Builder prefix(String prefix, String value) 		{ this.prefixes.put(prefix, value); return this; }
		
		public SparqlUpdate build() 		{ return new SparqlUpdate(this); } 
	}

	public SparqlUpdate(Builder builder) {
		
		if (null == builder.deleteClause && null == builder.insertClause)
			throw new IllegalArgumentException("UPDATE query requires insert or delete or both.");
		
		this.graph = builder.graph;
		this.prefixes = builder.prefixes;
		this.deleteClause = builder.deleteClause;
		this.insertClause = builder.insertClause;
		this.whereClause = builder.whereClause;
		this.insertDataFlag = builder.insertDataFlag;
		this.deleteDataFlag = builder.deleteDataFlag;
		
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
                sb.append("PREFIX ")
                  .append(prefix)
                  .append(": <")
                  .append(prefixes.get(prefix))
                  .append(">\n");
            }
            sb.append("\n");
        }
		if (null != graph) 			sb.append(String.format("WITH <%s>", graph));
		if (null != deleteClause)	{
//			if (insertDataFlag) {
//				sb.append(String.format(" DELETE DATA { %s }", deleteClause));
//			} else {
				sb.append(String.format(" DELETE { %s }", deleteClause));
//			}
		}
		if (null != insertClause) {
//			if (insertDataFlag) {
//				sb.append(String.format(" INSERT DATA { %s }", insertClause));
//			} else {
				sb.append(String.format(" INSERT { %s }", insertClause));
//			}
		}
		
		if (null != whereClause)	sb.append(String.format(" WHERE { %s }", whereClause));
		else if (null != deleteClause)	sb.append(String.format(" WHERE { %s }", deleteClause));
		else sb.append("WHERE {}");
		
		return sb.toString();
	}
	
	public void execute() {
        log.info("UPDATE query: " + toString());
		long startTime = System.currentTimeMillis();
        UpdateRequest update = UpdateFactory.create();
        for (Entry<String, String> namespaceMapping : new GrafeoImpl().getNamespacesUsed().entrySet()) {
        	update.setPrefix(namespaceMapping.getKey(), namespaceMapping.getValue());
        }
        update.add(toString());
        UpdateProcessor exec;
		if (null != endpoint) {
			exec = UpdateExecutionFactory.createRemoteForm(update, endpoint);
		} else {
			GraphStore gs = GraphStoreFactory.create(grafeo.getModel());
			exec = UpdateExecutionFactory.create(update, gs);
		}
        exec.execute();
        long estimatedTime = System.currentTimeMillis() - startTime;
        if (estimatedTime < 700) {
	        log.info("UPDATE took " + estimatedTime +"ms");
        } else { 
	        log.severe("UPDATE took " + estimatedTime +"ms: " + toString());
        }
	}
}
