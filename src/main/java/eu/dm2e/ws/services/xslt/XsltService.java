package eu.dm2e.ws.services.xslt;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;

import org.joda.time.DateTime;

import eu.dm2e.NS;
import eu.dm2e.utils.FileUtils;
import eu.dm2e.utils.XsltUtils;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractTransformationService;
import eu.dm2e.ws.services.Client;

/**
 * Service for transforming XML to (RDF)XML using a self-contained XSLT script
 *
 * @author Konstantin Baierer
 */
@Path("/service/xslt")
public class XsltService extends AbstractTransformationService {
	
	public static final String PARAM_XML_IN = "xmlInput";
	public static final String PARAM_XSLT_IN = "xslInput";
	public static final String PARAM_XSLT_PARAMETER_STRING = "xslParams";
	public static final String PARAM_XSLT_PARAMETER_RESOURCE = "xslParamsResource";
	public static final String PARAM_XML_OUT = "xmlOutput";
	
	// XSLT Parameters
	public static final String PARAM_XSLT_PARAM_DATAPROVIDER = "xslParam_" + XsltUtils.PARAMETER_DEFAULTS.DATAPROVIDER_KEY;
	public static final String PARAM_XSLT_PARAM_DATASET = "xslParam_" + XsltUtils.PARAMETER_DEFAULTS.DATASET_KEY;
//	public static final String PARAM_XSLT_PARAM_DM2E_BASEURI = "xslParam_" + XsltUtils.PARAMETER_DEFAULTS.DM2E_BASEURI_KEY;

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
		
		ParameterPojo xsltParameterString = ws.addInputParameter(PARAM_XSLT_PARAMETER_STRING);
		xsltParameterString.setComment("XSLT Parameters (key-value pairs separated by either newline or semicolon, key and value separated by '=', comments start with '#'");
		xsltParameterString.setIsRequired(false);
		xsltParameterString.setParameterType("xsd:string");

		ParameterPojo xsltParameterResource = ws.addInputParameter(PARAM_XSLT_PARAMETER_RESOURCE);
		xsltParameterResource.setComment("Resource containing XSLT Parameters (key-value pairs separated by either newline or semicolon, key and value separated by '=', comments start with '#'");
		xsltParameterResource.setIsRequired(false);
		xsltParameterResource.setParameterType("xsd:anyURI");

		ParameterPojo xsltParameter_dataprovider = ws.addInputParameter(PARAM_XSLT_PARAM_DATAPROVIDER);
		xsltParameter_dataprovider.setComment("Shortcut for the data provider. No spaces!");
		xsltParameter_dataprovider.setIsRequired(false);
//		xsltParameter_dataprovider.setIsRequired(true);
//		xsltParameter_dataprovider.setDefaultValue(XsltUtils.PARAMETER_DEFAULTS.DATAPROVIDER_VALUE);
		xsltParameter_dataprovider.setParameterType("xsd:string");

		ParameterPojo xsltParameter_dataset = ws.addInputParameter(PARAM_XSLT_PARAM_DATASET);
		xsltParameter_dataset.setComment("Shortcut for the dataset. No spaces!");
		xsltParameter_dataset.setIsRequired(false);
//		xsltParameter_dataset.setIsRequired(true);
//		xsltParameter_dataset.setDefaultValue(XsltUtils.PARAMETER_DEFAULTS.DATASET_VALUE);
		xsltParameter_dataset.setParameterType("xsd:string");

//		ParameterPojo xsltParameter_dm2e_baseuri = ws.addInputParameter(PARAM_XSLT_PARAM_DM2E_BASEURI);
//		xsltParameter_dm2e_baseuri.setComment("Base URI of the DM2E data namespace (Default: http://data.dm2e.eu/data/).");
//		xsltParameter_dm2e_baseuri.setIsRequired(false);
//		xsltParameter_dm2e_baseuri.setDefaultValue(XsltUtils.PARAMETER_DEFAULTS.DM2E_BASEURI_VALUE);
//		xsltParameter_dm2e_baseuri.setParameterType("xsd:anyURI");

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
            FileUtils fileUtils = new FileUtils(client, jobPojo);
			
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
			// determine whether this is an XSLT-ZIP or an XSLT
			FilePojo fpXslt = new FilePojo();
			log.debug("Load XSLT file pojo from "+ fpXslt);
			fpXslt.loadFromURI(xsltUrl);
			
			// The type of xslt script, either a single large script or a zipped set of scripts
			String xsltType = NS.OMNOM_TYPES.XSLT;
			if (null != fpXslt.getFileType() && (fpXslt.getFileType().toString().equals(NS.OMNOM_TYPES.XSLT) || fpXslt.getFileType().toString().equals(NS.OMNOM_TYPES.ZIP_XSLT))) {
				xsltType = fpXslt.getFileType().toString();
			} else {
				log.error("ITS AN Unknown filetype " + fpXslt.getFileType().toString() + ", defaulting to " + NS.OMNOM_TYPES.XSLT);
			}
			log.debug("Treating XSL input as " + xsltType);
			
			//
			// parse XSLT parameters
			//
			// Priority:
			// * Defaults in the XSLT (case0)
			// * Parameters from the web service (case1)
			// * Linked parameters (in a file containg key-vale pairs) (PARAM_XSLT_PARAMETER_RESOURCE, case2)
			// * Explicit parameters as web service parameter (PARAM_XSLT_PARAMETER_STRING, case3)
			Map<String, String> paramMap = new HashMap<String, String>();

			// #case1
			String dp = jobPojo.getWebserviceConfig().getParameterValueByName(PARAM_XSLT_PARAM_DATAPROVIDER);
			if (null != dp && ! dp.matches("^\\s*$")) {
				paramMap.put("DATAPROVIDER_ABB", dp); // SBB
				paramMap.put("dataprovider", dp); // UBFFM
			}
			String ds = jobPojo.getWebserviceConfig().getParameterValueByName(PARAM_XSLT_PARAM_DATASET);
			if (null != ds && ! ds.matches("^\\s*$")) {
				paramMap.put("REPOSITORY_ABB", ds); // SBB
				paramMap.put("repository", ds); // UBFFM
			}

			// PARAM_XSLT_PARAMETER_RESOURCE #case2
			String paramMapResourceUri = jobPojo.getWebserviceConfig().getParameterValueByName(PARAM_XSLT_PARAMETER_RESOURCE);
			if (null != paramMapResourceUri && ! paramMapResourceUri.matches("^\\s*$")) {
				String paramMapStr;
				try {
					paramMapStr = client.getJerseyClient().target(paramMapResourceUri).request().get(String.class);
				} catch (ProcessingException e) {
					throw new RuntimeException("Parameter Resource is invalid URI: " + paramMapResourceUri, e);
				}
				paramMap.putAll(xsltUtils.parseXsltParameters(paramMapStr));
			}


			// PARAM_XSLT_PARAMETER_STRING #case3
			String paramMapStr = jobPojo.getWebserviceConfig().getParameterValueByName(PARAM_XSLT_PARAMETER_STRING);
			paramMap.putAll(xsltUtils.parseXsltParameters(paramMapStr));
			log.debug("Parameters: ");
			for (Entry<String, String> kvPair : paramMap.entrySet()) {
				log.debug(" * " + kvPair.getKey() + " : " + kvPair.getValue());
			}
			
			//
			// Actual transformation
			//
			jobPojo.info("Starting transformation");

			StringWriter xslResultStrWriter;
			
			if (xsltType.equals(NS.OMNOM_TYPES.XSLT)) {
				
				// Simple XSLT transformation
				try {
					xslResultStrWriter = xsltUtils.transformXsltUrl(xmlUrl, xsltUrl, paramMap);
				} catch (Throwable t) {
					throw new RuntimeException("Error during XSLT transformation: " + t);
				}
			} else {

				// XSLT-ZIP transformation
				jobPojo.debug("Downloading XML/ZIP");
				String zipdir;
				try {
					zipdir = fileUtils.downloadAndExtractArchive(xsltUrl, "zip");
				} catch (Exception e) {
					jobPojo.debug(e);
					throw e;
				}
				jobPojo.debug("Done unzipping XSLTZIP.");

				jobPojo.debug("Determining root stylesheet.");
				String rootStyleSheet = xsltUtils.grepRootStylesheet(zipdir);
				jobPojo.debug("Determined root stylesheet: " + rootStyleSheet);
				try {
					xslResultStrWriter = xsltUtils.transformXsltFile(xmlUrl, rootStyleSheet, paramMap);
				} catch (Throwable t) {
					throw new RuntimeException("Error during XSLT transformation: " + t);
				}
			}
			assert(null != xslResultStrWriter);
			jobPojo.info("Getting the transformation result as a string.");
			String xslResultStr = xslResultStrWriter.toString();
			if (xslResultStr.length() == 0) {
				throw new RuntimeException("No result from the transformation.");
			}
			else if (xslResultStr.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
				jobPojo.warn("XSLT transformation yielded an empty XML file.");
			}

			FilePojo fp = new FilePojo();
			fp.setExtent(xslResultStr.length());
			fp.setOriginalName("generated_" + DateTime.now().toString() + ".xml");
			fp.setModified(DateTime.now());
			fp.setFileType(NS.OMNOM_TYPES.XML);
			fp.setWasGeneratedBy(jobPojo);
			String fileLocation = new Client().publishFile(xslResultStr, fp);

			jobPojo.info("Store result URI on the job (" + fileLocation + ").");
			jobPojo.addOutputParameterAssignment(PARAM_XML_OUT, fileLocation);

			// Update job status
			jobPojo.info("XSLT Transformation complete.");
			jobPojo.setFinished();
		} catch (Throwable t) {
			log.error("An error occured during XsltService.run: ", t);
			t.printStackTrace();
			jobPojo.fatal("An error occured during XsltService.run: " + t);
			jobPojo.setFailed();
		} finally {
			// jobPojo.publishToService();
		}
	}
}
