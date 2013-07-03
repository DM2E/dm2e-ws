package eu.dm2e.ws.services.mint;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * A wrapper for Jersey Client that
 * a) can store cookies
 * b) does *not* follow redirects
 * 
 * @author Konstantin Baierer
 *
 */
public class MintClient {
	
	Logger log = Logger.getLogger(getClass().getName());
	
	private com.sun.jersey.api.client.Client jerseyClient = null;
	public com.sun.jersey.api.client.Client getJerseyClient() {
    	if (null == this.jerseyClient) { 
    		Client jClient = new com.sun.jersey.api.client.Client();
    		jClient.setFollowRedirects(false);
    		this.jerseyClient = jClient;
    	}
		return this.jerseyClient;
	}
	
	protected Set<Cookie> cookies = new HashSet<>();
	
	public void addCookie(Cookie cookie) { this.cookies.add(cookie); }
	public void addCookies(List<NewCookie> theseCookies) {
		log.fine("Adding cookies: " + theseCookies);
		for (Cookie thisCookie : theseCookies)
			this.addCookie(thisCookie);
	}
	public void clearCookies() {
		log.info("Clear cookies.");
		this.cookies.clear(); 
	}
	
	public Builder resource(URI uri) {
		return this.resource(uri.toString());
	}

	public Builder resource(String uri) {
		Builder reqB = getJerseyClient().resource(uri).getRequestBuilder();
		for (Cookie cookie : cookies) {
			log.info("Adding cookie to web resource: " + cookie);
			reqB.cookie(cookie);
		}
		reqB.header("User-Agent", "omnom/mint-client");
		return reqB;
	}
	
}
