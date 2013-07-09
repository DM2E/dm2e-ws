package eu.dm2e.ws.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import eu.dm2e.utils.NSExporter;
import eu.dm2e.ws.NS;

@Path("/ns")
public class NamespaceService {
	
	@GET
	@Produces("application/json")
	public String getNsAsJson() {
		
		return NSExporter.exportToJSON(NS.class);
	}

}
