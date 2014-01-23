package eu.dm2e.ws.util;

import static org.junit.Assert.*;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JerseyAssert {

	static Logger log = LoggerFactory.getLogger(JerseyAssert.class);
	private static Client defaultClient;
	static {
		defaultClient = ClientBuilder.newClient();
		defaultClient.property(ClientProperties.FOLLOW_REDIRECTS, true);
	}

//	public static void assertUriIsDereferenceable(Client client, URI uri) {
//		assertUriIsDereferenceable(client, uri.toString());
//	}

	public static void assertUriIsDereferenceable(Client client, Object uriObj) {
		URI uri = URI.create(uriObj.toString());
		if (null == client) client = defaultClient;
		
		log.info("Asserting that {} is dereferenceable.", uri);
		
		Boolean wasFollow = (Boolean) client.getConfiguration().getProperty(ClientProperties.FOLLOW_REDIRECTS);
		client.property(ClientProperties.FOLLOW_REDIRECTS, true);
		Response resp = client.target(uri).request().get();
		client.property(ClientProperties.FOLLOW_REDIRECTS, wasFollow);
		if (resp.getStatus() >= 400) {
			throw new AssertionError("URI threw error: " + resp);
		}
		log.info("Response: " + resp);
	}
	
	public static void assertUriReturnsType(Client client, Object uriObj, Object mediaTypeObj) {
		URI uri = URI.create(uriObj.toString());
		MediaType mediaType = MediaType.valueOf(mediaTypeObj.toString());
		if (null == client) client = defaultClient;
		
		log.info("Asserting that {} returns type {}.", uri, mediaType);
		
		Boolean wasFollow = (Boolean) client.getConfiguration().getProperty(ClientProperties.FOLLOW_REDIRECTS);
		client.property(ClientProperties.FOLLOW_REDIRECTS, true);
		Response resp = client.target(uri).request().get();
		client.property(ClientProperties.FOLLOW_REDIRECTS, wasFollow);
		if (resp.getStatus() >= 400) {
			throw new AssertionError(resp + ": " + resp.readEntity(String.class));
		}
		
		assertEquals(mediaType, resp.getMediaType());
	}
	

}
