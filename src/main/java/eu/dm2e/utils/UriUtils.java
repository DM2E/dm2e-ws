package eu.dm2e.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;

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
	 * TODO (Wed Jan  8 01:05:00 CET 2014) this breaks parameter parsing since newline is translated to '_' if we replace all '\\s'
	 * 
	 * @param uri
	 * @return
	 */
	public static String sanitizeInput(String uri) {
//		uri = uri.replaceAll("\\s", "_");
		return uri;
	}
	
	public static String uriEncode(String uri) {
        try {
            return URLEncoder.encode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }

    public static String uriDecode(String uri) {
        try {
            return URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }

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
