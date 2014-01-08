package eu.dm2e.utils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.StringWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Test;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.tests.OmnomTestCase;
import eu.dm2e.ws.tests.OmnomTestResources;

public class XsltUtilsTest extends OmnomTestCase {
	
	XsltUtils xu;
	
	public XsltUtilsTest() {
		JobPojo job = new JobPojo();
		this.xu = new XsltUtils(client, job);
	}
	
	@Test
	public void testParseParams() throws ParseException {
		StringBuilder paramStr = new StringBuilder();
		paramStr.append("\nrepository   =NOT-sammlungen \n   ");
		paramStr.append("dataprovider=   NOT-ub-ffm\n");
		log.debug(paramStr.toString());
		Map<String, String> paramMap = xu.parseXsltParameters(paramStr.toString());
		assertThat(paramMap.get("repository"), notNullValue());
		assertThat(paramMap.get("repository"), is("NOT-sammlungen"));
		assertThat(paramMap.get("dataprovider"), notNullValue());
		assertThat(paramMap.get("dataprovider"), is("NOT-ub-ffm"));
	}
	
	@Test
	public void testTransformXsltUrl() throws TransformerFactoryConfigurationError, Throwable {
		final String testXml = OmnomTestResources.METS_SINGLE_EXAMPLE.getURL().toString();
		final String testXslt = OmnomTestResources.METS2EDM.getURL().toString();
		{
			log.debug("Explicit params");
			final HashMap<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("repository", "NOT-sammlungen");
			paramMap.put("dataprovider", "NOT-ub-ffm");
			StringWriter sw = xu.transformXsltUrl(testXml, testXslt, paramMap);
			String transformedXml = sw.toString();
			assertThat(transformedXml, containsString("NOT-sammlungen"));
			assertThat(transformedXml, containsString("NOT-ub-ffm"));
		}
		{
			log.debug("Newline separated");
			StringBuilder paramStr = new StringBuilder();
			paramStr.append("repository   =NOT-sammlungen \n   ");
			paramStr.append("dataprovider=   NOT-ub-ffm\n");
			Map<String, String> paramMap = xu.parseXsltParameters(paramStr.toString());
			StringWriter sw = xu.transformXsltUrl(testXml, testXslt, paramMap);
			String transformedXml = sw.toString();
			assertThat(transformedXml, containsString("NOT-sammlungen"));
			assertThat(transformedXml, containsString("NOT-ub-ffm"));
		}
		{
			log.debug("Semicolon separated");
			StringBuilder paramStr = new StringBuilder();
			paramStr.append(" repository=NOT-sammlungen;  ");
			paramStr.append("dataprovider= NOT-ub-ffm  ;");
			Map<String, String> paramMap = xu.parseXsltParameters(paramStr.toString());
			StringWriter sw = xu.transformXsltUrl(testXml, testXslt, paramMap);
			String transformedXml = sw.toString();
			assertThat(transformedXml, containsString("NOT-sammlungen"));
			assertThat(transformedXml, containsString("NOT-ub-ffm"));
		}
	}
}
