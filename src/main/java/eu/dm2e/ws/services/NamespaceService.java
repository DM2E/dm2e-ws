package eu.dm2e.ws.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import eu.dm2e.utils.NSExporter;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.services.file.FileStatus;

/**
 * Service that returns constants and enums used in the backend to clients.
 *
 * @see eu.dm2e.utils.NSExporter
 * @see eu.dm2e.ws.NS
 *
 * @author Konstantin Baierer
 */
@Path("/constants")
public class NamespaceService {
	
	@GET
	@Produces("application/json")
	@Path("rdfns")
	public String getNsAsJson() {
		
		return NSExporter.exportInnerClassConstantsToJSON(NS.class);
	}
	
	@GET
	@Produces("application/json")
	@Path("/jobStatus")
	public String getJobStatus() {
		return NSExporter.exportEnumToJSON(JobStatus.class);
	}
	
	@GET
	@Produces("application/json")
	@Path("/fileStatus")
	public String getFileStatus() {
		return NSExporter.exportEnumToJSON(FileStatus.class);
	}
	
}
