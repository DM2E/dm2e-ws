package eu.dm2e.ws.services.xslt;

import java.io.StringWriter;
import java.util.Map;

import javax.ws.rs.Path;

import eu.dm2e.utils.XsltUtils;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractTransformationService;
import eu.dm2e.ws.services.Client;

@Path("/service/xslt")
public class XsltService extends AbstractTransformationService {
	
	public static final String PARAM_XML_IN = "xmlInput";
	public static final String PARAM_XSLT_IN = "xslInput";
	public static final String PARAM_XSLT_PARAMETERS = "xslParams";
	public static final String PARAM_XML_OUT = "xmlOutput";

	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.setLabel("XSLT");

		ParameterPojo xsltInParam = ws.addInputParameter(PARAM_XSLT_IN);
		xsltInParam.setComment("XSLT input");
		xsltInParam.setIsRequired(true);
		xsltInParam.setParameterType("xsd:anyURI");

		ParameterPojo xmlInParam = ws.addInputParameter(PARAM_XML_IN);
		xmlInParam.setComment("XML input");
		xmlInParam.setIsRequired(true);
		xmlInParam.setParameterType("xsd:anyURI");
		
		ParameterPojo xsltParameterString = ws.addInputParameter(PARAM_XSLT_PARAMETERS);
		xsltParameterString.setComment("XSLT Parameters (one key-value pair per line, separated by '=', comments start with '#'");
		xsltParameterString.setIsRequired(false);
		xsltParameterString.setParameterType("xsd:string");

		ParameterPojo xmlOutParam = ws.addOutputParameter(PARAM_XML_OUT);
		xmlOutParam.setComment("XML output");
		
//		ParameterPojo fileServiceParam = ws.addInputParameter("fileServiceParam");
//		fileServiceParam.setIsRequired(false);
		
		return ws;
	}
	
	@Override
	public void run() {
		JobPojo jobPojo = getJobPojo();
		log.warn("Starting to handle XSLT transformation job");
		jobPojo.debug("Starting to handle XSLT transformation job");
		// update job status
		jobPojo.setStarted();
		String xmlUrl, xsltUrl;
		try {
			XsltUtils xsltUtils = new XsltUtils(client, jobPojo);
			
			// TODO this should be refactored to a validation routine in the JobPojo
			// PARAM_XML_IN
			xmlUrl = jobPojo.getWebserviceConfig().getParameterValueByName(PARAM_XML_IN);
			if (null == xmlUrl)
				throw new NullPointerException("xmlUrl is null");
			jobPojo.debug("XML URL: " + xmlUrl);
			// PARAM_XSLT_IN
			xsltUrl = jobPojo.getWebserviceConfig().getParameterValueByName(PARAM_XSLT_IN);
			if (null == xsltUrl)
				throw new NullPointerException("xsltUrl is null");
			jobPojo.debug("XSL URL: " + xsltUrl);
			// PARAM_XSLT_PARAMETERS
			String paramMapStr = jobPojo.getWebserviceConfig().getParameterValueByName(PARAM_XSLT_PARAMETERS);
			Map<String, String> paramMap;
			paramMap = xsltUtils.parseXsltParameters(paramMapStr);
			
			
			jobPojo.info("Starting transformation");

			StringWriter xslResultStrWriter = null;
			xslResultStrWriter = xsltUtils.transformXsltUrl(xmlUrl, xsltUrl, paramMap);
			assert(null != xslResultStrWriter);
			jobPojo.info("Getting the transformation result as a string.");
			String xslResultStr = "";
			
			xslResultStr = xslResultStrWriter.toString();
			if (xslResultStr.length() == 0) {
				throw new RuntimeException("No result from the transformation.");
			}
			else if (xslResultStr.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
				jobPojo.warn("XSLT transformation yielded in empty XML file.");
			}

			FilePojo fp = new FilePojo();
			fp.setWasGeneratedBy(jobPojo);
			String fileLocation = new Client().publishFile(xslResultStr, fp);

			jobPojo.info("Store result URI on the job (" + fileLocation + ").");
			jobPojo.addOutputParameterAssignment(PARAM_XML_OUT, fileLocation);

			// Update job status
			jobPojo.info("XSLT Transformation complete.");
			jobPojo.setFinished();
		} catch (Throwable t) {
			log.error("An error occured during XsltService.run: " + t);
			jobPojo.fatal("An error occured during XsltService.run: " + t);
			jobPojo.setFailed();
		} finally {
			jobPojo.publishToService();
		}
	}
}
