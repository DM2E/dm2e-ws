package eu.dm2e.ws.services.xslt;

import java.io.StringWriter;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.Path;

import eu.dm2e.utils.XsltUtils;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractTransformationService;
import eu.dm2e.ws.services.Client;

@Path("/service/xslt-zip")
public class XsltZipService extends AbstractTransformationService {

	public static final String PARAM_XSLTZIP_IN = "xsltZipInput";
	public static final String PARAM_XML_IN = "xmlInput";
	public static final String PARAM_XML_OUT = "xmlOutput";
	public static final String PARAM_XSLT_PARAMETERS = "xslParams";
	public static final String PARAM_PROVIDER_ID_VALUE = "provider-id";
	public static final String PARAM_DATASET_ID_VALUE = "dataset-id";
	public static final String PARAM_PROVIDER_ID_KEY = "provider-id-param";
	public static final String PARAM_DATASET_ID_KEY = "dataset-id-param";

	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();

		ParameterPojo xsltInParam = ws.addInputParameter(PARAM_XML_IN);
		xsltInParam.setComment("XML input");
		xsltInParam.setIsRequired(true);
		xsltInParam.setParameterType("xsd:anyURI");

		ParameterPojo xmlInParam = ws.addInputParameter(PARAM_XSLTZIP_IN);
		xmlInParam.setComment("XML ZIP input");
		xmlInParam.setIsRequired(true);
		xmlInParam.setParameterType("xsd:anyURI");
		
		ParameterPojo xsltParameterString = ws.addInputParameter(PARAM_XSLT_PARAMETERS);
		xsltParameterString.setComment("XSLT Parameters (one key-value pair per line, separated by '=', comments start with '#'");
		xsltParameterString.setIsRequired(false);
		xsltParameterString.setParameterType("xsd:string");
		
		ParameterPojo providerIdKeyParam = ws.addInputParameter(PARAM_PROVIDER_ID_KEY);
		providerIdKeyParam.setDefaultValue(XsltUtils.DEFAULT_PARAM_NAMES.PROVIDER_ID_KEY);
		providerIdKeyParam.setComment("Provider ID Parameter used in XSLT");

		ParameterPojo providerIdValueParam = ws.addInputParameter(PARAM_PROVIDER_ID_VALUE);
		providerIdValueParam.setDefaultValue(XsltUtils.DEFAULT_PARAM_NAMES.PROVIDER_ID_VALUE);
		providerIdValueParam.setComment("Provider ID");

		ParameterPojo datasetIdParam = ws.addInputParameter(PARAM_DATASET_ID_KEY);
		datasetIdParam.setDefaultValue(XsltUtils.DEFAULT_PARAM_NAMES.DATASET_ID_KEY);
		datasetIdParam.setComment("Dataset ID Parameter used in XSLT");
		
		ParameterPojo datasetId = ws.addInputParameter(PARAM_DATASET_ID_VALUE);
		providerIdKeyParam.setDefaultValue(XsltUtils.DEFAULT_PARAM_NAMES.DATASET_ID_VALUE);
		datasetId.setComment("Dataset ID");

		ParameterPojo xmlOutParam = ws.addOutputParameter(PARAM_XML_OUT);
		xmlOutParam.setComment("XML output");

		return ws;
	}

	@Override
	public void run() {
		JobPojo jobPojo = getJobPojo();
		log.warning("Starting to handle XSLT transformation job");
		jobPojo.debug("Starting to handle XSLT transformation job");
		String xsltZipUrl, xmlUrl, providerId, datasetId, providerIdKey, datasetIdKey;
		try {
			
			XsltUtils xsltUtils = new XsltUtils(client, jobPojo);
			Map<String, String> paramMap;
			try {
				// XSLTZIP_IN_PARAM_NAME
				xsltZipUrl = jobPojo.getWebserviceConfig().getParameterValueByName(PARAM_XSLTZIP_IN);
				if (null == xsltZipUrl) throw new NullPointerException(PARAM_XSLTZIP_IN + " is null");
				jobPojo.debug(PARAM_XSLTZIP_IN + " : " + xsltZipUrl);
				
				// PARAM_XML_IN
				xmlUrl = jobPojo.getWebserviceConfig().getParameterValueByName(PARAM_XML_IN);
				if (null == xmlUrl) throw new NullPointerException(PARAM_XML_IN + " is null");
				jobPojo.debug(PARAM_XML_IN + " : " + xmlUrl);
				
			} catch (Exception e) {
				throw new InvalidParameterException("Parameter error: " + e);
			}
			// PARAM_XSLT_PARAMETERS
			String paramMapStr = jobPojo.getParameterValueByName(PARAM_XSLT_PARAMETERS);
			paramMap = xsltUtils.parseXsltParameters(paramMapStr);
			
			// PROVIDER_ID
			providerId = jobPojo.getParameterValueByName(PARAM_PROVIDER_ID_VALUE);
			// PARAM_PROVIDER_ID_KEY
			providerIdKey = jobPojo.getParameterValueByName(PARAM_PROVIDER_ID_KEY);
			jobPojo.debug("Provider XSLT Param: " + providerIdKey + ": "+ providerId);
			paramMap.put(providerIdKey, providerId);
			
			// PARAM_DATASET_ID_KEY
			datasetIdKey = jobPojo.getParameterValueByName(PARAM_DATASET_ID_KEY);
			// PARAM_DATASET_ID_VALUE
			datasetId = jobPojo.getParameterValueByName(PARAM_DATASET_ID_VALUE);
			jobPojo.debug("Dataset ID XSLT Param: " + providerIdKey + ": "+ providerId);
			paramMap.put(datasetIdKey, datasetId);
			
			jobPojo.info("Preparing transformation");

			// download and extract zip
			jobPojo.debug("Downloading XML/ZIP");
			java.nio.file.Path zipdir = xsltUtils.downloadAndExtractZip(xsltZipUrl);
			jobPojo.debug("Done unzipping XSLTZIP.");

			jobPojo.debug("Determining root stylesheet.");
			String rootStyleSheet = xsltUtils.grepRootStylesheet(zipdir);
			jobPojo.debug("Determined root stylesheet: " + rootStyleSheet);

			// update job status
			jobPojo.info("Starting transformation");
			jobPojo.setStarted();
			StringWriter xslResultStrWriter;
			try {
				xslResultStrWriter = xsltUtils.transformXsltFile(xmlUrl, rootStyleSheet, paramMap);
			} catch (Throwable t) {
				throw new RuntimeException("Error during XSLT transformation: " + t);
			}

			jobPojo.info("Getting the transformation result as a string.");
			String xslResultStr = "";
			try {
				xslResultStr = xslResultStrWriter.toString();
				if (xslResultStr.length() == 0) {
					throw new RuntimeException("No result from the transformation.");
				} else if (xslResultStr.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
					jobPojo.warn("XSLT transformation yielded in empty XML file.");
				}
			} catch (Exception e) {
				jobPojo.debug(e);
				jobPojo.setFailed();
			}

			FilePojo fp = new FilePojo();
			fp.setGeneratorJob(jobPojo);
			String fileLocation = new Client().publishFile(xslResultStr, fp);

			jobPojo.info("Store result URI on the job (" + fileLocation + ").");
			jobPojo.addOutputParameterAssignment(PARAM_XML_OUT, fileLocation);
			client.publishPojoToJobService(jobPojo);

			// Update job status
			jobPojo.info("XSLT Transformation complete.");
			jobPojo.setFinished();
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Exception during publishing: " + t, t);
			jobPojo.fatal(t);
			jobPojo.setFailed();
		}
		finally {
			jobPojo.publishToService();
		}
	}

}
