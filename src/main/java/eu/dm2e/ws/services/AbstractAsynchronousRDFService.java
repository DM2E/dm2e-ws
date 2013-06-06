package eu.dm2e.ws.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public abstract class AbstractAsynchronousRDFService extends AbstractRDFService implements Runnable {

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	public abstract Response startService(String configURI);

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
	public abstract Response postConfig(String rdfString);

}