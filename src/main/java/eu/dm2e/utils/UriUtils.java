package eu.dm2e.utils;

import org.eclipse.jetty.util.UrlEncoded;

public class UriUtils {
	
	public static String lastUriSegment(String uri) {
		String[] uriRev = uri.split("/");
		return uriRev[uriRev.length - 1];
	}
	
	/**
	 * Cleans up input for use in web services.
	 * 
	 * TODO / BUG: Since we don't discern URI parameters from String parameters cleanly
	 * ATM, we cannot just URL-encode the String (because that would break URLs).
	 * Instead we cover the most important cases here.
	 * 
	 * TODO: Investigate whether and how risky this is.
	 * 
	 * @param uri
	 * @return
	 */
	public static String sanitizeInput(String uri) {
		uri = uri.replaceAll("\\s", "_");
		return uri;
	}
	
	public static String uriEncode(String uri) {
		return UrlEncoded.encodeString(uri);
	}

}
