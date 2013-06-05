package eu.dm2e.ws;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.dm2e.utils.TemplateEngine;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.Client;
import eu.dm2e.ws.wsmanager.ManageService;

public class OmnomTestCase {
	protected Logger log = Logger.getLogger(getClass().getName());
	protected Client client = new Client();
	protected String URI_BASE = "http://localhost:9998/";
	protected Map<OmnomTestResources, String> configString = new HashMap<>();
	protected Map<OmnomTestResources, File> configFile = new HashMap<>();;
	
//	@Before
//	public void setUp() throws Exception {
	public OmnomTestCase() {
		for (OmnomTestResources res : OmnomTestResources.values()) { 
			URL testConfigURL = this.getClass().getResource(res.getPath());
			try {
				configFile.put(res, new File(testConfigURL.getFile()));
				configString.put(res, IOUtils.toString(testConfigURL.openStream()));
			} catch (Exception e) {
				org.junit.Assert.fail(res + " not found: " + e.toString());
			}
		}
	}
	
	@BeforeClass
	public static void setUpClass() {
        System.setProperty("http.keepAlive", "false");
        ManageService.startAll();
	}
	
	/**
	 * @param templ
	 * @param templMap
	 * @param webResource
	 * @param class1
	 * @return
	 */
	protected WebserviceConfigPojo renderAndLoadPojo(String templ, Map<String, String> templMap, WebResource webResource, Class<WebserviceConfigPojo> class1) {
		String templStr = TemplateEngine.render(templ, templMap);
		ClientResponse resp1 = webResource
				.type(DM2E_MediaType.TEXT_TURTLE)
				.post(ClientResponse.class,
				templStr);
		URI loc = resp1.getLocation();
		Grafeo g = new GrafeoImpl(resp1.getEntityInputStream());
		return g.getObjectMapper().getObject(class1, loc);
	}

}
