package eu.dm2e.ws.services.xslt;

import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;

public class XsltServiceTest {
	
	public static WebservicePojo getWebService() {

		WebservicePojo ws;
		ParameterPojo xsltInParam
		, xmlInParam
		, xmlOutParam;

		String serviceUri = "http://data.dm2e.eu/data/services/xslt";

		ws = new WebservicePojo();
		ws.setId(serviceUri);

		xsltInParam = new ParameterPojo();
		xsltInParam.setTitle("XSLT input");
		xsltInParam.setIsRequired(true);
		xsltInParam.setWebservice(ws);
		xsltInParam.setId(serviceUri + "/xsltInParam");
		ws.getInputParams().add(xsltInParam);

		xmlInParam = new ParameterPojo();
		xmlInParam.setTitle("XML input");
		xmlInParam.setIsRequired(true); 
		xmlInParam.setWebservice(ws);
		xmlInParam.setId(serviceUri + "/xmlInParam");
		ws.getInputParams().add(xmlInParam);

		xmlOutParam = new ParameterPojo();
		xmlOutParam.setTitle("XML output");
		xmlOutParam.setWebservice(ws);
		xmlOutParam.setId(serviceUri + "/xmlOutParam");
		ws.getOutputParams().add(xmlOutParam);
		
		return ws;
	}
		
}
