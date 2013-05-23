package eu.dm2e.ws.services.demo;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebServiceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractRDFService;

@Path("/service/demo")
public class DemoService extends AbstractRDFService {
	
	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.addInputParameter("sleepTime");
		return ws;
	}
	
	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	public Response runDemoService(String configURI) {
		
		/*	
		 * Resolve configURI to WebServiceConfigPojo
		 */
//		WebServiceConfigPojo wsConf = resolveWebSerivceConfigPojo(configURI);
		// TODO not very elegant
		WebServiceConfigPojo wsConfDummy = new WebServiceConfigPojo();
		wsConfDummy.setId(configURI);
		WebServiceConfigPojo wsConf = wsConfDummy.readFromEndpoint();
		
		/*	
		 * Build JobPojo
		 * */
		JobPojo job = new JobPojo();
		// TODO the job probably doesn't even need a webservice reference since it's in the conf already
		job.setWebService(wsConf.getWebservice());
		job.setWebServiceConfig(wsConf);
		job.publish();
		
		/*
		 * Let the asynchronous worker handle the job
		 */
		DemoExecutorService.INSTANCE.handleJob(job);
		
		/*
		 * Return JobPojo
		 */
		return Response
				.ok()
				.entity(getResponseEntity(job.getGrafeo()))
				.location(job.getIdAsURI())
				.build();
	}
	
	@POST
	@Consumes(MediaType.WILDCARD)
	public Response postDemoService(String rdfString) {
		WebServiceConfigPojo conf = new WebServiceConfigPojo().constructFromRdfString(rdfString);
		conf.publish();
		return this.runDemoService(conf.getId());
	}

}
