package eu.dm2e.ws.services.xslt;

import eu.dm2e.ws.api.WebservicePojo;

public class XsltServiceITCase {
	
	public static WebservicePojo getWebService() {
		XsltService ws = new XsltService();
		return ws.getWebServicePojo();
//		return XsltService.
	}
		
}
