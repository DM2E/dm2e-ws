package eu.dm2e.ws.services.demo;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractRDFService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
		 * Resolve configURI to WebserviceConfigPojo
		 */
//		WebserviceConfigPojo wsConf = resolveWebSerivceConfigPojo(configURI);
		// TODO not very elegant
		WebserviceConfigPojo wsConfDummy = new WebserviceConfigPojo();
		wsConfDummy.setId(configURI);
		WebserviceConfigPojo wsConf = wsConfDummy.readFromEndpoint();
		
		/*	
		 * Build JobPojo
		 * */
		JobPojo job = new JobPojo();
		// TODO the job probably doesn't even need a webservice reference since it's in the conf already
		job.setWebService(wsConf.getWebservice());
		job.setWebserviceConfig(wsConf);
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
		WebserviceConfigPojo conf = new WebserviceConfigPojo().constructFromRdfString(rdfString);
		conf.publish();
		return this.runDemoService(conf.getId());
	}

}
