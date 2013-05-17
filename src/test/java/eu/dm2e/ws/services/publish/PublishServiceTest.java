package eu.dm2e.ws.services.publish;

import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;

public class PublishServiceTest {
	
	public static WebservicePojo getWebService() {
		
		WebservicePojo ws;
		ParameterPojo targetEndpointParam
		, targetGraphParam
		, rdfInputParam;
		
		String wsUri = "http://localhost:9998/service/publish";
		ws = new WebservicePojo();
		ws.setId(wsUri);
		
		targetEndpointParam = new ParameterPojo();
		targetEndpointParam.setId(wsUri + "/param/targetEndpointParam");
		targetEndpointParam.setIsRequired(true);
		targetEndpointParam.setTitle("URI of the SPARQL endpoint to publish to.");
		ws.getInputParams().add(targetEndpointParam);
		
		targetGraphParam = new ParameterPojo();
		targetGraphParam.setId(wsUri + "/param/targetGraphParam");
		targetGraphParam.setIsRequired(true);
		targetGraphParam.setTitle("URI of the graph to publish to.");
		ws.getInputParams().add(targetGraphParam);
		
		rdfInputParam = new ParameterPojo();
		rdfInputParam.setId(wsUri + "/param/rdfInputParam");
		rdfInputParam.setIsRequired(true);
		rdfInputParam.setTitle("URI of the serialized RDF data to publish.");
		ws.getInputParams().add(rdfInputParam);
		
		return ws;
	}

}
