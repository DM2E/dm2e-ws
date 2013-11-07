package eu.dm2e.ws.tests;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.grafeo.json.GrafeoJsonSerializer;
import eu.dm2e.utils.TemplateEngine;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.*;
import eu.dm2e.ws.services.Client;
import eu.dm2e.ws.wsmanager.ManageService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class OmnomTestCase extends OmnomUnitTest{
	
	protected static String URI_BASE;
	static {
		URI_BASE = "http://localhost:9998/api/";
//		System.setProperty("dm2e-ws.test.properties_file", "dm2e-ws.test.properties");
		Config.set(ConfigProp.ENDPOINT_QUERY, "http://localhost:9997/test/sparql");
		Config.set(ConfigProp.ENDPOINT_UPDATE, "http://localhost:9997/test/update");
		Config.set(ConfigProp.BASE_URI, URI_BASE);
	}
	protected static Client client = new Client();
	
//	@Before
//	public void setUp() throws Exception {
	public OmnomTestCase() {
        GrafeoJsonSerializer.registerType(JobPojo.class);
        GrafeoJsonSerializer.registerType(FilePojo.class);
        GrafeoJsonSerializer.registerType(LogEntryPojo.class);
        GrafeoJsonSerializer.registerType(ParameterAssignmentPojo.class);
        GrafeoJsonSerializer.registerType(ParameterConnectorPojo.class);
        GrafeoJsonSerializer.registerType(ParameterPojo.class);
        GrafeoJsonSerializer.registerType(UserPojo.class);
        GrafeoJsonSerializer.registerType(VersionedDatasetPojo.class);
        GrafeoJsonSerializer.registerType(WebserviceConfigPojo.class);
        GrafeoJsonSerializer.registerType(WebservicePojo.class);
        GrafeoJsonSerializer.registerType(WorkflowPojo.class);
        GrafeoJsonSerializer.registerType(WorkflowPositionPojo.class);

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
