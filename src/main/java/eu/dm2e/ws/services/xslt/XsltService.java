package eu.dm2e.ws.services.xslt;

import java.io.StringWriter;
import java.net.URL;

import javax.ws.rs.Path;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractTransformationService;
import eu.dm2e.ws.services.Client;

@Path("/service/xslt")
public class XsltService extends AbstractTransformationService {
	
	public static final String XML_IN_PARAM_NAME = "xmlInput";
	public static final String XSLT_IN_PARAM_NAME = "xslInput";
	public static final String XML_OUT_PARAM_NAME = "xmlOutput";

	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();

		ParameterPojo xsltInParam = ws.addInputParameter(XSLT_IN_PARAM_NAME);
		xsltInParam.setTitle("XSLT input");
		xsltInParam.setIsRequired(true);
		xsltInParam.setParameterType("xsd:anyURI");

		ParameterPojo xmlInParam = ws.addInputParameter(XML_IN_PARAM_NAME);
		xmlInParam.setTitle("XML input");
		xmlInParam.setIsRequired(true);
		xmlInParam.setParameterType("xsd:anyURI");

		ParameterPojo xmlOutParam = ws.addOutputParameter(XML_OUT_PARAM_NAME);
		xmlOutParam.setTitle("XML output");
		
//		ParameterPojo fileServiceParam = ws.addInputParameter("fileServiceParam");
//		fileServiceParam.setIsRequired(false);
		
		return ws;
	}

	@Override
	public void run() {
		JobPojo jobPojo = getJobPojo();
		log.warning("Starting to handle XSLT transformation job");
		jobPojo.debug("Starting to handle XSLT transformation job");
		String xmlUrl, xsltUrl;
		try {
			// TODO this should be refactored to a validation routine in the JobPojo
			xmlUrl = jobPojo.getWebserviceConfig().getParameterValueByName(XML_IN_PARAM_NAME);
			xsltUrl = jobPojo.getWebserviceConfig().getParameterValueByName(XSLT_IN_PARAM_NAME);
			if (null == xmlUrl) {
				throw new NullPointerException("xmlUrl is null");
			}
			if (null == xsltUrl) {
				throw new NullPointerException("xsltUrl is null");
			}
		} catch (Exception e) {
			jobPojo.fatal(e);
			jobPojo.setFailed();
			return;
		}
		jobPojo.debug("XML URL: " + xmlUrl);
		jobPojo.debug("XSL URL: " + xsltUrl);
		jobPojo.info("Starting transformation");

		// update job status
		jobPojo.setStarted();
		TransformerFactory tFactory = TransformerFactory.newInstance();
		StringWriter xslResultStrWriter = new StringWriter();
		try {
			StreamSource xmlSource = new StreamSource(new URL(xmlUrl).openStream());
			StreamSource xslSource = new StreamSource(new URL(xsltUrl).openStream());
			Transformer transformer = tFactory.newTransformer(xslSource);

			StreamResult xslResult = new StreamResult(xslResultStrWriter);

			transformer.transform(xmlSource, xslResult);

		} catch (Exception e) {
			jobPojo.fatal("Error during XSLT transformation: " + e);
			jobPojo.debug(e);
			jobPojo.setFailed();
			return;
		}
		jobPojo.info("Getting the transformation result as a string.");
		String xslResultStr = "";
		try {
			xslResultStr = xslResultStrWriter.toString();
			if (xslResultStr.length() == 0) {
				throw new RuntimeException("No result from the transformation.");
			}
			else if (xslResultStr.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
				jobPojo.warn("XSLT transformation yielded in empty XML file.");
			}
		} catch (Exception e) {
			jobPojo.debug(e);
			jobPojo.setFailed();
			return;
		}
		
		FilePojo fp = new FilePojo();
		fp.setGeneratorJob(jobPojo);
		String fileLocation = new Client().publishFile(xslResultStr, fp);

		jobPojo.info("Store result URI on the job (" + fileLocation + ").");
		jobPojo.addOutputParameterAssignment(XML_OUT_PARAM_NAME, fileLocation);
		client.publishPojoToJobService(jobPojo);

		// Update job status
		jobPojo.info("XSLT Transformation complete.");
		jobPojo.setFinished();
	}
}