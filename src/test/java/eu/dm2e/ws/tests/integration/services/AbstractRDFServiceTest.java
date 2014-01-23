package eu.dm2e.ws.tests.integration.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.dm2e.ws.services.AbstractRDFService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.tests.OmnomTestCase;
import eu.dm2e.ws.api.IWebservice;
import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.grafeo.jena.GrafeoMongoImpl;

public class AbstractRDFServiceTest extends OmnomTestCase{
	
	@Path("/mock")
	private class MockService extends AbstractRDFService {
	}
	MockService mockService = new MockService();
	private static final String MESSAGE = "FOO";
	private RuntimeException EXCEPTION = new RuntimeException(MESSAGE);
	private String EXCEPTION_DESCRIPTION = EXCEPTION.toString() + "\n" + ExceptionUtils.getStackTrace(EXCEPTION);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testThrowServiceErrorStringInt() {
		Response resp = mockService.throwServiceError(MESSAGE, 402);
		assertEquals(402, resp.getStatus());
		assertEquals(MESSAGE, resp.getEntity());
	}

	@Test
	public void testThrowServiceErrorString() {
		Response resp = mockService.throwServiceError(MESSAGE);
		assertEquals(400, resp.getStatus());
		assertEquals(MESSAGE, resp.getEntity());
	}
	
	@Test
	public void testThrowServiceErrorException() {
		Response resp = mockService.throwServiceError(EXCEPTION);
		assertEquals(400, resp.getStatus());
		assertEquals(EXCEPTION_DESCRIPTION, resp.getEntity());
	}
	
	@Test
	public void testThrowServiceErrorErrorMsg() {
		Response resp = mockService.throwServiceError(ErrorMsg.BAD_RDF);
		assertEquals(400, resp.getStatus());
		assertEquals(ErrorMsg.BAD_RDF.toString(), resp.getEntity());
	}

	@Test
	public void testThrowServiceErrorStringErrorMsg() {
		String badString = "SNOOZING";
		Response resp = mockService.throwServiceError(badString, ErrorMsg.INVALID_LOG_LEVEL);
		assertEquals(400, resp.getStatus());
		assertEquals(badString + ": " + ErrorMsg.INVALID_LOG_LEVEL.toString(), resp.getEntity());
	}

	@Test
	public void testGetUriForString() {
		Set<String> validUris = new HashSet<String>();
		Set<String> invalidUris = new HashSet<String>();
		validUris.add("http://foo.org/baz");
		validUris.add("http://foo.org/baz:zab");
		invalidUris.add("http://foo.schmorg/baz");
		invalidUris.add("/foo.org/baz");
		invalidUris.add("http://foo.schmorg/baz#foo#bar");
		for (String uriStr : validUris) {
			try { 
				mockService.getUriForString(uriStr);
				assertTrue("URI is valid", true);
			} catch (URISyntaxException e) {
				fail(uriStr + ": this should be an invalid URI");
			}
		}
		for (String uriStr : invalidUris) {
			try {
				mockService.getUriForString(uriStr);
				fail(uriStr + ": this should be an invalid URI");
			} catch (URISyntaxException e) {
				assertTrue("URI is invalid", true);
			}
		}
	}

	@Test
	public void testGetWebServicePojo() {
		IWebservice ws = mockService.getWebServicePojo();
		assertNotNull(ws);
	}

	@Ignore("This must be tested in an IT case")
	@Test
	public void testGetDescription() {
		Response resp = mockService.getDescription();
		assertEquals(200, resp.getStatus());
		Grafeo g1 = mockService.getWebServicePojo().getGrafeo();
		assertNotNull(resp.getEntity());
		Grafeo g2 = new GrafeoMongoImpl(IOUtils.toInputStream((String)resp.getEntity()));
		assertTrue("Equivalen grafeos", g1.isGraphEquivalent(g2));
	}
//
//	@Test
//	public void testGetParamDescription() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testValidateConfigRequest() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetGrafeoForUriWithContentNegotiationString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetGrafeoForUriWithContentNegotiationURI() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAbstractRDFService() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetResponseModel() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetResponseGrafeo() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetResponseEntityModel() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetResponseEntityGrafeo() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testNotValid() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCreateUniqueStr() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testValidateServiceInput() {
//		fail("Not yet implemented");
//	}
//
	@Test
	public void testAppendPath() throws URISyntaxException {
		URI uri = new URI("http://foo.org/bar");
		URI uri2 = mockService.appendPath(uri, "baz");
		assertEquals("http://foo.org/bar/baz", uri2.toString());
	}
	
	@Test
	public void testPopPathSegement()  {
		URI uri = URI.create("http://foo.org/bar/baz");
		assertEquals("http://foo.org/bar", mockService.popPath(uri, "baz").toString());
		assertEquals("http://foo.org/bar", mockService.popPath(uri, null).toString());
	}
//
//	@Test
//	public void testResolveWebServiceConfigPojo() {
//		fail("Not yet implemented");
//	}

}
