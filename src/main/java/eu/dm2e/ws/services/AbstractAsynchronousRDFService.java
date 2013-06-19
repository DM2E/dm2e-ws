package eu.dm2e.ws.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public abstract class AbstractAsynchronousRDFService extends AbstractRDFService implements Runnable {

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	public abstract Response putConfigToService(String configURI);

	/**
	 * Convenience method that accepts a configuration, publishes it
	 * directly to the ConfigurationService and then calls the TransformationService
	 * with the persistent URI.
	 *
	 * Only to be used for development, not for production!
	 *
	 * @param rdfString
	 * @return
	 */
	@POST
	@Consumes(MediaType.WILDCARD)
	public Response postConfig(String rdfString) {
		Grafeo g = null;
		try {
			g = new GrafeoImpl();
			g.readHeuristically(rdfString);
			assert(g != null);
		}
		catch (Exception e) {
			throwServiceError(e);
		}
		return this.postGrafeo(g);
	}
	
	abstract public Response postGrafeo(Grafeo g);

}