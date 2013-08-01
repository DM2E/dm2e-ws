package eu.dm2e.ws.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * Abstract Base Class for services that are execute asynchronously
 */
public abstract class AbstractAsynchronousRDFService extends AbstractRDFService implements Runnable {

	/**
	 * Convenience method that accepts a configuration, publishes it
	 * directly to the ConfigurationService and then calls the TransformationService
	 * with the persistent URI.
	 *
	 * <del>Only to be used for development, not for production!</del>
	 * This is necessary for workflows. [kb, Jul 15, 2013 12:03:08 AM]
	 * 
	 * TODO test whether we can replace this with the MessageBodyWriter<Grafeo>
	 *
	 * @param rdfString
	 * @return
	 */
	@POST
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
		// , MediaType.TEXT_HTML
		// , DM2E_MediaType.TEXT_PLAIN,
		// , MediaType.APPLICATION_JSON
	})
	public Response postRDF(String rdfString) {
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
	

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	public abstract Response putConfigToService(String configURI);

	abstract public Response postGrafeo(Grafeo g);

}
