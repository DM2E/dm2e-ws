package eu.dm2e.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;

/**
 * Utility methods for handling URIs.
 * @author Konstantin Baierer
 */
public class UriUtils {
	
	/**
	 * Return the last path segment of a URI.
	 * @param uri the URI
	 * @return the last path segment
	 */
	public static String lastUriSegment(String uri) {
		String[] uriRev = uri.split("/");
		return uriRev[uriRev.length - 1];
	}
	
	/**
	 * Cleans up input for use in web services.
	 * 
	 * <p>
	 * TODO / BUG: Since we don't discern URI parameters from String parameters cleanly
	 * ATM, we cannot just URL-encode the String (because that would break URLs).
	 * Instead we cover the most important cases here.
	 * </p>
	 * 
	 * <p>
	 * TODO: Investigate whether and how risky this is.
	 * </p>
	 * 
	 * <p>
	 * TODO (Wed Jan  8 01:05:00 CET 2014) this breaks parameter parsing since newline is translated to '_' if we replace all '\\s'
	 * </p>
	 * 
	 * @param uri
	 * @return the sanitized input
	 */
	public static String sanitizeInput(String uri) {
//		uri = uri.replaceAll("\\s", "_");
		return uri;
	}
	
	/**
	 * URL-encode a String.
	 * @param uri the String to URL encode
	 * @return the URL-encoded String
	 * @throws {@link RuntimeException} if the system doesn't support UTF-8.
	 */
	public static String uriEncode(String uri) {
        try {
            return URLEncoder.encode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }

	/**
	 * URL-decode a String.
	 * @param uri the String to URL decode
	 * @return the URL-decoded String
	 * @throws {@link RuntimeException} if the system doesn't support UTF-8.
	 */
    public static String uriDecode(String uri) {
        try {
            return URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }

    /**
     * URL encodes the path segments of a path, excluding the slashes.
     * @param path the path to URL-encode
     * @return the URL-encoded path
     */
    public static String uriEncodePathElements(String path) {
        StringTokenizer st = new StringTokenizer(path, "/",true);
        StringBuilder res = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ("/".equals(token)) res.append("/");
            else res.append(uriEncode(token));
        }
        return res.toString();
    }



}
