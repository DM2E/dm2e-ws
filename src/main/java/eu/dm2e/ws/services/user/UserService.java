package eu.dm2e.ws.services.user;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.UserPojo;
import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.AbstractRDFService;

/**
 * Service for managing users
 */
@Path("/user")
public class UserService extends AbstractRDFService {
	
	/**
	 * GET _username		Accept: * 		Content-Type: text/plain
	 *
	 * <p>
	 * Returns the name of the currently logged in user
	 * </p>
	 */
	@GET
	@Path("_username")
	@Produces({
		DM2E_MediaType.TEXT_PLAIN
	})
	public Response getUserId(@Context SecurityContext security) {
		// TODO handle test case
		if (null != System.getProperty("dm2e-ws.isTestRun") && System.getProperty("dm2e-ws.isTestRun").equals("true"))
			return Response.ok(appendPath(popPath(), "the-test-user").toString()).build();
		if (null == security.getUserPrincipal()) {
			return throwServiceError("NOT LOGGED IN", 403);
		}
		return Response.ok(appendPath(popPath(), security.getUserPrincipal().getName()).toString()).build();
	}
	
	@DELETE
	@Path("{id}")
	public Response deleteUser() {
		URI uri = getRequestUriWithoutQuery();
		Grafeo g = new GrafeoImpl();
		
		g.emptyGraph(Config.get(ConfigProp.ENDPOINT_UPDATE), uri);
		return Response.ok().build();
	}
	
	
	
	/**
	 * GET {id}		Accept: JSON, RDF
	 */
	@GET
	@Path("{id}")
	@Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE,
		MediaType.APPLICATION_JSON
	})
	public Response getUser() {
		
		URI uri = getRequestUriWithoutQuery();
		GrafeoImpl g = new GrafeoImpl();
		g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), uri);
		if (g.isEmpty())
			return Response.status(404).entity("No such user.").build();
		UserPojo userPojo = g.getObjectMapper().getObject(UserPojo.class, uri);
		return Response.ok(userPojo).build();
	}

	/**
	 * PUT {id}		Accept: JSON, RDF
	 * @param user the id of the user
	 */
	@PUT
	@Path("{id}")
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE,
		MediaType.APPLICATION_JSON
	})
	@Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE,
		MediaType.APPLICATION_JSON
	})
	public Response putUser(UserPojo user) {
		URI uri = getRequestUriWithoutQuery();
		user.setId(uri);
//		log.debug(user.getTurtle());
		user.getGrafeo().putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), uri);
//		log.debug("Stored");
		GrafeoImpl g2 = new GrafeoImpl();
		g2.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), uri);
//		log.debug(g2.getTurtle());
//		log.debug("Loaded");
		UserPojo userPut = g2.getObjectMapper().getObject(UserPojo.class, uri);
//		log.debug(userPut.getTurtle());
		return Response.ok().entity(userPut).build();
	}

}
