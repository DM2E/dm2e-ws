package eu.dm2e.ws.services.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import eu.dm2e.ws.OmnomTestCase;
import eu.dm2e.ws.OmnomTestResources;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public class FileServiceITCase extends OmnomTestCase {
	
	private String SERVICE_URI;

    @Before
	public void setUp() throws Exception {
    	SERVICE_URI  = URI_BASE + "/file";
    }

	@After
	public void tearDown() {
	}

	@Test
	public void testFileOnly() {
		// fail("Not yet implemented");
        String URI_BASE = "http://localhost:9998";
        WebResource webResource = client.resource(URI_BASE + "/file");
		String turtleString = "<a> <b> <c>.";
		FormDataMultiPart mp = new FormDataMultiPart();
		FormDataBodyPart p = new FormDataBodyPart(FormDataContentDisposition
				.name("file")
				.fileName("file")
				.build(),
				turtleString);
		mp.bodyPart(p);
		String s = webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(String.class, mp);
        GrafeoImpl g = new GrafeoImpl();
        g.readHeuristically(s);
        for (GResource r: g.findByClass("omnom:File")) {
            log.info("RESPONSE: " + r.getUri());
            log.info("RESPONSE: " + r.getUri());
            // WebResource wr = client.resource("http://localhost:8000/test/sparql?query=select%20%3Fs%20%3Fp%20%3Fo%20where%20%7B%3Fs%20%3Fp%20%3Fo%7D");
            WebResource wr = client.resource(r.getUri());
            String resp = wr.get(String.class);
            assertEquals(turtleString, resp);
            log.info("RESPONSE 2: " + resp);
            Grafeo g2 = new GrafeoImpl(r.getUri());
            log.info("RESPONSE 3: " + g2.getTurtle());
            assertNotNull(g2.get(r.getUri()));
        }

	}
	@Test
	public void testBinaryFile() {
		FormDataMultiPart fdmp = this.client.createFileFormDataMultiPart("", configFile.get(OmnomTestResources.TEI2EDM_20130129));
		String uri = client.publishFile(fdmp);
		assertNotNull(uri);
		log.info("File stored as: " + uri);
		String resp = client.resource(uri).get(String.class);
		assertEquals(configString.get(OmnomTestResources.TEI2EDM_20130129), resp);
	}
}
