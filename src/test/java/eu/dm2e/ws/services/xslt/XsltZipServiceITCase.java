package eu.dm2e.ws.services.xslt;

import static org.junit.Assert.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;

public class XsltZipServiceITCase extends OmnomTestCase {
	
    private String SERVICE_URI;
//    private WebservicePojo SERVICE_POJO;
    private String XSLTZIP_URI_1;
	private String XML_URI_1;

	@Before
    public void setUp() throws Exception {
    	SERVICE_URI = URI_BASE + "service/xslt-zip";
//    	SERVICE_POJO = new XsltZipService().getWebServicePojo();
    	XSLTZIP_URI_1 = client.publishFile(configFile.get(OmnomTestResources.TEI2DM2E_20130605));
    	if (null == XSLTZIP_URI_1) { fail("Couldn't store test file."); }
    	log.info("XSLTZIP_URI_1: " + XSLTZIP_URI_1);
    	XML_URI_1 = client.publishFile(configFile.get(OmnomTestResources.XML_DTA_GRIMM));
    	if (null == XML_URI_1) { fail("Couldn't store test file."); }
    	log.info("XML_URI_1: " + XML_URI_1);
    }

	@Test
	public void testGetWebServicePojo() {
//		fail("Not yet implemented");
	}

	@Test
	public void testRun() throws Exception {
		
//		Map<String, String> templMap = new HashMap<String,String>();
//		templMap.put("xmlInput", XML_URI_1);
//		templMap.put("xsltZipInput", XSLTZIP_URI_1);
//		WebserviceConfigPojo conf = renderAndLoadPojo(
//				configString.get(OmnomTestResources.TEMPLATE_BLANK_XSLTZIP), 
//				templMap,
//				client.getConfigWebTarget(),
//				WebserviceConfigPojo.class);
		WebserviceConfigPojo conf = new WebserviceConfigPojo();
		conf.setWebservice(new XsltZipService().getWebServicePojo());
//		conf.publishToService();
		conf.addParameterAssignment(XsltZipService.PARAM_XSLTZIP_IN, XSLTZIP_URI_1);
		conf.addParameterAssignment(XsltZipService.PARAM_XML_IN, XML_URI_1); 
//		conf.publishToService();
		conf.addParameterAssignment(XsltZipService.PARAM_PROVIDER_ID_VALUE, "my-provider");
		conf.addParameterAssignment(XsltZipService.PARAM_DATASET_ID_VALUE, "my-dataset");
		conf.publishToService(client.getConfigWebTarget());
		
		
		Response confGETresp = client.target(conf.getId()).request().get();
		assertEquals(200, confGETresp.getStatus());
//		GrafeoImpl g = new GrafeoImpl(confGETresp.getEntityInputStream());
//		assertEquals(g.getCanonicalNTriples(), conf.getCanonicalNTriples());
//		assertTrue(g.isGraphEquivalent(conf.getGrafeo()));
		
		Response resp = client.target(SERVICE_URI)
			.request(DM2E_MediaType.TEXT_TURTLE)
			.put(Entity.text(conf.getId()));
		log.info(resp.readEntity(String.class));
		assertEquals(202, resp.getStatus());
		log.info("JOB uri: " + resp.getLocation());
		
		JobPojo jobPojo = new JobPojo();
		
		try {
			do {
				jobPojo.loadFromURI(resp.getLocation()) ;
				log.info(jobPojo.toLogString());
				Thread.sleep(2000);
			} while (jobPojo.isStillRunning());
		} catch (Exception e) {
			log.error("Could reload job pojo." + e);
			throw e;
		}
		log.info(jobPojo.toLogString());
		assertEquals("FINISHED", jobPojo.getStatus());
	}

}
