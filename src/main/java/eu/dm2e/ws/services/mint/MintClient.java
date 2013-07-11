package eu.dm2e.ws.services.mint;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for Jersey Client that
 * a) can store cookies
 * b) does *not* follow redirects
 * 
 * @author Konstantin Baierer
 *
 */
public class MintClient {
	
	Logger log = LoggerFactory.getLogger(getClass().getName());
	
	private javax.ws.rs.client.Client jerseyClient = null;
	public javax.ws.rs.client.Client getJerseyClient() {
    	if (null == this.jerseyClient) { 
    		Client jClient = ClientBuilder.newClient();
    		jClient.property(ClientProperties.FOLLOW_REDIRECTS, false);
    		this.jerseyClient = jClient;
    	}
		return this.jerseyClient;
	}
	
	protected final Set<Cookie> cookies = new HashSet<>();
	
	public void addCookie(Cookie cookie) { this.cookies.add(cookie); }
	public void addCookies(Collection<NewCookie> collection) {
		log.trace("Adding cookies: " + collection);
		for (Cookie thisCookie : collection)
			this.addCookie(thisCookie);
	}
	public void clearCookies() {
		log.debug("Clear cookies.");
		this.cookies.clear(); 
	}
	
	public Builder target(URI uri) {
		return this.target(uri.toString());
	}

	public Builder target(String uri) {
		javax.ws.rs.client.Invocation.Builder reqB = getJerseyClient().target(uri).request();
		for (Cookie cookie : cookies) {
			log.trace("Adding cookie to web resource: " + cookie);
			reqB.cookie(cookie);
		}
		reqB.header("User-Agent", "omnom/mint-client");
		log.trace("Built WebResource Builder for <{}>", uri);
		return reqB;
	}
	
}
