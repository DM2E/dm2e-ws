package eu.dm2e.utils;

public class UriUtils {
	
	public static String lastUriSegment(String uri) {
		String[] uriRev = uri.split("/");
		return uriRev[uriRev.length - 1];
	}

}
