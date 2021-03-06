package eu.dm2e.ws.tests.integration.services.xslt;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.dm2e.NS;
import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.xslt.XsltService;
import eu.dm2e.ws.tests.OmnomTestCase;
import eu.dm2e.ws.tests.OmnomTestResources;

/**
 * @author kb
 *
 */
public class XsltServiceITCase extends OmnomTestCase {
	
    private static final String PARAMS_NEWLINE   = "dataprovider=NOT-ub-ffm\nrepository=NOT-sammlungen\nDATAPROVIDER_ABB=NOT-ub-ffm\nREPOSITORY_ABB=NOT-sammlungen";
	private static final String PARAMS_SEMICOLON = "dataprovider=NOT-ub-ffm; repository=NOT-sammlungen; DATAPROVIDER_ABB=NOT-ub-ffm; REPOSITORY_ABB=NOT-sammlungen";
	private static final String PARAMS_SEMICOLON_ONLY_REPOSITORY = "repository=NOT-sammlungen;REPOSITORY_ABB=NOT-sammlungen";
	private String SERVICE_URI;
    private WebservicePojo SERVICE_POJO;
	private String metsXml;
	private String metsXslt;
	private String dtaXml;
	private String dtaXsltZip;
	private String paramListUriNewline;
	private String paramListUriSemicolon;
	private String paramListUriSemicolonOnlyRepository;

	@Before
    public void setUp() throws Exception {
        SERVICE_URI = URI_BASE + "service/xslt";
    	SERVICE_POJO = new XsltService().getWebServicePojo();

    	OmnomTestResources metsXmlRes = OmnomTestResources.METS_SINGLE_EXAMPLE;
    	metsXml = client.publishFile(configFile.get(metsXmlRes));
    	assertThat(metsXml, notNullValue());

    	OmnomTestResources dtaXmlRes = OmnomTestResources.XML_DTA_GRIMM;
    	dtaXml = client.publishFile(configFile.get(dtaXmlRes));
    	assertThat(dtaXml, notNullValue());

    	OmnomTestResources metsXsltRes = OmnomTestResources.METS2EDM;
    	FilePojo metsXsltFilePojo = new FilePojo();
    	metsXsltFilePojo.setFileType(NS.OMNOM_TYPES.XSLT);
    	metsXslt = client.publishFile(configFile.get(metsXsltRes), metsXsltFilePojo);
    	assertThat(metsXslt, notNullValue());

    	OmnomTestResources dtaXsltZipRes = OmnomTestResources.TEI2DM2E_20130605;
    	FilePojo dtaXsltZipFilePojo = new FilePojo();
    	dtaXsltZipFilePojo.setFileType(NS.OMNOM_TYPES.ZIP_XSLT);
    	dtaXsltZip = client.publishFile(configFile.get(dtaXsltZipRes), dtaXsltZipFilePojo);
    	assertThat(dtaXsltZip, notNullValue());

    	paramListUriNewline = client.publishFile(PARAMS_NEWLINE);
    	paramListUriSemicolon = client.publishFile(PARAMS_SEMICOLON);
    	paramListUriSemicolonOnlyRepository = client.publishFile(PARAMS_SEMICOLON_ONLY_REPOSITORY);
    }
	
	@Test
	public void testXsl() {
		{
			FilePojo fp = new FilePojo();
			fp.loadFromURI(dtaXsltZip);
			assertThat(fp.getFileType(), notNullValue());
			assertThat(fp.getFileType().toString(), is(NS.OMNOM_TYPES.ZIP_XSLT));
		}
		{
			FilePojo fp = new FilePojo();
			fp.loadFromURI(metsXslt);
			assertThat(fp.getFileType(), notNullValue());
			assertThat(fp.getFileType().toString(), is(NS.OMNOM_TYPES.XSLT));
		}
	}
	
	@Ignore("Focus on the other tests")
    @Test
    public void testDescription() {
    	log.info(SERVICE_URI);
    	Grafeo g = new GrafeoImpl(client.getJerseyClient()
    			.target(SERVICE_URI)
    			.request("text/turtle")
    			.get(InputStream.class));
    	log.info(g.getTurtle());
    	assertTrue(g.containsTriple(SERVICE_URI, "rdf:type", "omnom:Webservice"));
    	assertTrue(g.containsTriple(SERVICE_URI, "omnom:inputParam", SERVICE_URI + "/param/xmlInput"));
    	assertTrue(g.containsTriple(SERVICE_URI + "/param/xmlInput", "rdf:type", "omnom:Parameter"));
    	assertTrue(g.containsTriple(SERVICE_URI + "/param/xmlInput", "omnom:parameterType", g.literal(g.expand("xsd:anyURI"))));
    }
    @Test
    public void testTransformation_XSLTZIP_Semicolon_String() throws Exception {
    	log.info("XSLTZIP/Semicolon parameter string");
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.PARAM_XML_IN, dtaXml);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, dtaXsltZip);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_STRING, PARAMS_SEMICOLON);
    	String xmlContent = executeXsltConfig(tC);
    	assertThat(xmlContent, containsString("NOT-ub-ffm"));
    	assertThat(xmlContent, containsString("NOT-sammlungen"));
    }
    @Test
    public void testTransformation_XSLTZIP_Newline_String() throws Exception {
    	log.info("XSLTZIP Newline parameter string");
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.PARAM_XML_IN, dtaXml);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, dtaXsltZip);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_STRING, PARAMS_NEWLINE);
    	String xmlContent = executeXsltConfig(tC);
    	assertThat(xmlContent, containsString("NOT-ub-ffm"));
    	assertThat(xmlContent, containsString("NOT-sammlungen"));
    }
    @Test
    public void testTransformation_XSLTZIP_Semicolon_URI() throws Exception {
    	log.debug("XSLTZIP / Semicolon parameter URI");
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.PARAM_XML_IN, dtaXml);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, dtaXsltZip);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_RESOURCE, paramListUriSemicolon);
    	String xmlContent = executeXsltConfig(tC);
    	assertThat(xmlContent, containsString("NOT-ub-ffm"));
    	assertThat(xmlContent, containsString("NOT-sammlungen"));
    }
    public void testTransformation_XSLTZIP_Newline_URI() throws Exception {
    	log.debug("XSLTZIP / Newline parameter URI");
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.PARAM_XML_IN, metsXml);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, metsXslt);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_RESOURCE, paramListUriNewline);
    	String xmlContent = executeXsltConfig(tC);
    	assertThat(xmlContent, containsString("NOT-ub-ffm"));
    	assertThat(xmlContent, containsString("NOT-sammlungen"));
    }

    @Test
    public void testTransformation_XSLT_Semicolon_String() throws Exception {
    	log.info("XSLT Semicolon parameter String");
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.PARAM_XML_IN, metsXml);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, metsXslt);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_STRING, PARAMS_SEMICOLON);
    	String xmlContent = executeXsltConfig(tC);
    	assertThat(xmlContent, containsString("NOT-ub-ffm"));
    	assertThat(xmlContent, containsString("NOT-sammlungen"));
    }
    @Test
    public void testTransformation_XSLT_Newline_String() throws Exception {
    	log.info("Newline separated parameters");
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.PARAM_XML_IN, metsXml);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, metsXslt);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_STRING, PARAMS_NEWLINE);
    	String xmlContent = executeXsltConfig(tC);
    	assertThat(xmlContent, containsString("NOT-ub-ffm"));
    	assertThat(xmlContent, containsString("NOT-sammlungen"));
    }
    @Test
    public void testTransformation_XSLT_Semicolon_URI() throws Exception {
    	log.debug("XSLT Semicolon Parameter URI");
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.PARAM_XML_IN, metsXml);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, metsXslt);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_RESOURCE, paramListUriSemicolon);
    	String xmlContent = executeXsltConfig(tC);
    	assertThat(xmlContent, containsString("NOT-ub-ffm"));
    	assertThat(xmlContent, containsString("NOT-sammlungen"));
    }
    @Test
    public void testTransformation_XSLT_Newline_URI() throws Exception {
    	log.debug("XSLT Newline Parameter URI");
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.PARAM_XML_IN, metsXml);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, metsXslt);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_RESOURCE, paramListUriNewline);
    	String xmlContent = executeXsltConfig(tC);
    	assertThat(xmlContent, containsString("NOT-ub-ffm"));
    	assertThat(xmlContent, containsString("NOT-sammlungen"));
    }

    @Test
    public void testTransformation_XSLT_Semicolon_String_Param() throws Exception {
    	log.info("XSLT Semicolon parameter String and parameter");
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.PARAM_XML_IN, metsXml);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, metsXslt);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAM_DATAPROVIDER, "DATAPROVIDER_NEEDLE");
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_STRING, "repository=NOT-sammlungen");
    	String xmlContent = executeXsltConfig(tC);
    	assertThat(xmlContent, containsString("DATAPROVIDER_NEEDLE"));
    	assertThat(xmlContent, containsString("NOT-sammlungen"));
    }
    @Test
    public void testTransformation_XSLT_Semicolon_String_Param_URI() throws Exception {
    	log.info("XSLT Semicolon parameter String and parameter and URI");
    	WebserviceConfigPojo tC = new WebserviceConfigPojo();
    	tC.setWebservice(SERVICE_POJO);
    	tC.addParameterAssignment(XsltService.PARAM_XML_IN, metsXml);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, metsXslt);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_RESOURCE, paramListUriSemicolonOnlyRepository);
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAMETER_STRING, "repository=NOT-sammlungen");
    	tC.addParameterAssignment(XsltService.PARAM_XSLT_PARAM_DATAPROVIDER, "DATAPROVIDER_NEEDLE");
    	String xmlContent = executeXsltConfig(tC);
    	assertThat(xmlContent, containsString("DATAPROVIDER_NEEDLE"));
    	assertThat(xmlContent, containsString("NOT-sammlungen"));
    }

	private String executeXsltConfig(WebserviceConfigPojo tC) throws InterruptedException {
    	assertThat(tC.getId(), is(nullValue()));
    	log.debug("config uri: " + tC.getId());
    	tC.publishToService(client.getConfigWebTarget());
    	assertThat(tC.getId(), not(nullValue()));
		Response resp = client.putPojoToService(tC, SERVICE_URI);
//    	log.info(tC.getTurtle());
//    	log.info(resp.readEntity(String.class));
    	assertEquals(202, resp.getStatus());
    	assertNotNull(resp.getLocation());
    	URI jobUri = resp.getLocation();
    	
    	JobPojo job = new JobPojo();
    	job.loadFromURI(jobUri);
    	
    	assertNotNull(job.getLabel());
    	
    	int maxWait = 10;
    	int i = 0;
    	while (!(job.isFinished() || job.isFailed())) {
    		if (i++ == maxWait) {
    			break;
    		}
	    	job.loadFromURI(jobUri);
	    	log.info(job.toLogString());
	    	Thread.sleep(500);
    	}
    	job.loadFromURI(jobUri);
    	String resultUri = job.getOutputParameterValueByName(XsltService.PARAM_XML_OUT);
//    	Thread.sleep(999999999L);
    	assertNotNull(resultUri);
    	log.info("Job finished. Result is at " + resultUri );
//    	log.info(job.getTerseTurtle());
    	String xmlContent = client.target(resultUri).request().get(String.class);
    	return xmlContent;
	}
		
}
