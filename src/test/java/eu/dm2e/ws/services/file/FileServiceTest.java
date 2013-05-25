package eu.dm2e.ws.services.file;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

public class FileServiceTest {
	
	Logger log = Logger.getLogger(getClass().getName());

	private Client client;
	private static String URI_BASE = "http://localhost:9998";

	@Before
	public void setUp()
			throws Exception {
		client = new Client();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testFileOnly() {
		// fail("Not yet implemented");
		WebResource webResource = client.resource(URI_BASE + "/file");
		FormDataMultiPart mp = new FormDataMultiPart();
		FormDataBodyPart p = new FormDataBodyPart(FormDataContentDisposition
				.name("file")
				.fileName("file")
				.build(),
				"FILE-CONTENT");
		mp.bodyPart(p);
		String s = webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(String.class, mp);
		log.info(s);
        assert(1==1);
	}

}
