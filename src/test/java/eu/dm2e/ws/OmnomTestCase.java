package eu.dm2e.ws;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.junit.Before;

import eu.dm2e.utils.TemplateEngine;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.Client;
import eu.dm2e.ws.wsmanager.ManageService;

public class OmnomTestCase extends OmnomUnitTest{
	
	static {
		Config.config.setProperty("dm2e.ws.sparql_endpoint", "http://localhost:9997/test/sparql");
		Config.config.setProperty("dm2e.ws.sparql_endpoint_statements", "http://localhost:9997/test/update");
		Config.config.setProperty("dm2e.ws.base_uri", "http://localhost:9998/");
	}
	protected static Client client = new Client();
	protected static String URI_BASE;
	protected static Map<OmnomTestResources, String> configString = new HashMap<>();
	protected Map<OmnomTestResources, File> configFile = new HashMap<>();;
	
//	@Before
//	public void setUp() throws Exception {
	public OmnomTestCase() {
		
		URI_BASE = Config.config.getString("dm2e.ws.base_uri");
		
		for (OmnomTestResources res : OmnomTestResources.values()) { 
			URL testConfigURL = OmnomTestCase.class.getResource(res.getPath());
			try {
				configFile.put(res, new File(testConfigURL.getFile()));
				configString.put(res, IOUtils.toString(testConfigURL.openStream()));
			} catch (Exception e) {
				org.junit.Assert.fail(res + " not found: " + e.toString());
			}
		}
	}
	
	@Before
	public void setUpBase() throws Exception {
//        System.setProperty("http.keepAlive", "false");
        ManageService.startAll();
	}
	
	/**
	 * @param templ
	 * @param templMap
	 * @param webTarget
	 * @param class1
	 * @return
	 */
	protected WebserviceConfigPojo renderAndLoadPojo(String templ, Map<String, String> templMap, WebTarget webTarget, Class<WebserviceConfigPojo> class1) {
		String templStr = TemplateEngine.render(templ, templMap);
		Response resp1 = webTarget
				.request()
				.post(Entity.entity(templStr, DM2E_MediaType.TEXT_TURTLE));
		URI loc = resp1.getLocation();
		Grafeo g = new GrafeoImpl(resp1.readEntity(InputStream.class));
		return g.getObjectMapper().getObject(class1, loc);
	}

}
