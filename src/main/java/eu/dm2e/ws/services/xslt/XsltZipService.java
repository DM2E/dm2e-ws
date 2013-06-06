package eu.dm2e.ws.services.xslt;

import static org.grep4j.core.Grep4j.constantExpression;
import static org.grep4j.core.Grep4j.grep;
import static org.grep4j.core.fluent.Dictionary.on;
import static org.grep4j.core.fluent.Dictionary.option;
import static org.grep4j.core.fluent.Dictionary.with;
import static org.grep4j.core.options.Option.filesMatching;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.Level;

import javax.ws.rs.Path;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.IOUtils;
import org.grep4j.core.model.Profile;
import org.grep4j.core.model.ProfileBuilder;
import org.grep4j.core.result.GrepResult;
import org.grep4j.core.result.GrepResults;

import com.sun.jersey.api.client.ClientResponse;

import eu.dm2e.ws.api.AbstractJobPojo;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractTransformationService;
import eu.dm2e.ws.services.Client;

@Path("/service/xslt-zip")
public class XsltZipService extends AbstractTransformationService {
	
	public static final String XSLTZIP_IN_PARAM_NAME = "xsltZipInput";
	public static final String XML_IN_PARAM_NAME = "xmlInput";
	public static final String XML_OUT_PARAM_NAME = "xmlOutput";

	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();

		ParameterPojo xsltInParam = ws.addInputParameter(XML_IN_PARAM_NAME);
		xsltInParam.setTitle("XML input");
		xsltInParam.setIsRequired(true);
		xsltInParam.setParameterType("xsd:anyURI");

		ParameterPojo xmlInParam = ws.addInputParameter(XSLTZIP_IN_PARAM_NAME);
		xmlInParam.setTitle("XML ZIP input");
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
		try {
			log.warning("Starting to handle XSLT transformation job");
			jobPojo.debug("Starting to handle XSLT transformation job");
			String xsltZipUrl, xmlUrl;
			try {
				// TODO this should be refactored to a validation routine in the JobPojo
				xsltZipUrl = jobPojo.getWebserviceConfig().getParameterValueByName(XSLTZIP_IN_PARAM_NAME);
				if (null == xsltZipUrl) {
					throw new NullPointerException("xmlUrl is null");
				}
				jobPojo.debug("XML URL: " + xsltZipUrl);
				xmlUrl = jobPojo.getWebserviceConfig().getParameterValueByName(XML_IN_PARAM_NAME);
				if (null == xmlUrl) {
					throw new NullPointerException("xsltUrl is null");
				}
				jobPojo.debug("XSL URL: " + xmlUrl);
			} catch (Exception e) {
				jobPojo.fatal("Parameter error: " + e);
				jobPojo.setFailed();
				return;
			}
			jobPojo.info("Preparing transformation");
			
			// download and extract zip
			jobPojo.debug("Downloading XML/ZIP");
			java.nio.file.Path zipdir = downloadAndExtractZip(xsltZipUrl, jobPojo);
			jobPojo.debug("Done unzipping XSLTZIP.");
			
			jobPojo.debug("Determining root stylesheet.");
			String rootStyleSheet = grepRootStylesheet(zipdir, jobPojo);
			jobPojo.debug("Determined root stylesheet: " + rootStyleSheet);
			
			
			// update job status
			jobPojo.info("Starting transformation");
			jobPojo.setStarted();
			TransformerFactory tFactory = TransformerFactory.newInstance();
			try {
				jobPojo.debug("Instantiating custom error listener");
				OmnomErrorListener errL;
				try {
//				errL = new OmnomErrorListener(Logger.getLogger(getClass().getName()));
					errL = new OmnomErrorListener(jobPojo);
				} catch (Exception e) {
					jobPojo.fatal(e);
					jobPojo.setFailed();
					return;
				}
				jobPojo.debug("Adding custom error listener");
				tFactory.setErrorListener(errL);
				jobPojo.debug("Done Adding custom error listener");
			} catch (Exception e1) {
				jobPojo.fatal(e1);
				jobPojo.setFailed();
				return;
			}
			jobPojo.debug("YAY Done Adding custom error listener");
			StringWriter xslResultStrWriter = new StringWriter();
			try {
				StreamSource xslSource = new StreamSource(new File(rootStyleSheet));
				xslSource.setSystemId(new File(rootStyleSheet));
				Transformer transformer = tFactory.newTransformer(xslSource);

				StreamResult xslResult = new StreamResult(xslResultStrWriter);

				StreamSource xmlSource = new StreamSource(new URL(xmlUrl).openStream());
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
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Exception during publishing: " + t, t);
            jobPojo.fatal(t);
            jobPojo.setFailed();
            throw t;
        }
	}

	/**
	 * @param xsltZipUrl
	 * @param jobPojo 
	 * @return
	 */
	protected java.nio.file.Path downloadAndExtractZip(String xsltZipUrl, JobPojo jobPojo) {
		ClientResponse resp = client.resource(xsltZipUrl).get(ClientResponse.class);
		if (resp.getStatus() >= 400) {
			jobPojo.fatal("Could not download XML/ZIP: " + resp.getEntity(String.class));
			jobPojo.setFailed();
			return null;
		}
		jobPojo.debug("File is available.");
		
		jobPojo.debug("Create tempfile");
		java.nio.file.Path zipfile;
		FileOutputStream zipfile_fos;
		try {
			zipfile = Files.createTempFile("omnom_xsltzip_", ".zip");
		} catch (IOException e1) {
			jobPojo.fatal("Could not download XML/ZIP: " + resp.getEntity(String.class));
			jobPojo.setFailed();
			return null;
		}
		
		jobPojo.debug("XML/ZIP will be stored as " + zipfile);
		
		try {
			zipfile_fos = new FileOutputStream(zipfile.toFile());
		} catch (FileNotFoundException e1) {
			jobPojo.fatal("Could not write out XML/ZIP: " + resp.getEntity(String.class));
			jobPojo.setFailed();
			return null;
		}
		try {
			IOUtils.copy(resp.getEntityInputStream(), zipfile_fos);
		} catch (IOException e) {
			jobPojo.fatal("Could not store XML/ZIP: " + e);
			jobPojo.setFailed();
			return null;
		}
		finally {
		      IOUtils.closeQuietly(zipfile_fos);
		}
		jobPojo.debug("XML/ZIP was stored as " + zipfile);
		
		jobPojo.debug("Creating temp directory.");
		java.nio.file.Path zipdir;
		try {
			 zipdir = Files.createTempDirectory("omnom_xsltzip_");
		} catch (IOException e) {
			jobPojo.fatal(e);
			jobPojo.setFailed();
			return null;
		}
		jobPojo.debug("Temp directory is " + zipdir);
		
		jobPojo.debug("Unzipping " + zipfile + " to " + zipdir.toString() );
	    try {
	         ZipFile zipFile = new ZipFile(zipfile.toFile());
	         zipFile.extractAll(zipdir.toString());
	    } catch (ZipException e) {
			jobPojo.fatal(e);
			jobPojo.setFailed();
			return null;
	    }
		return zipdir;
	}

	/**
	 * @param zipdir
	 * @return
	 */
	protected String grepRootStylesheet(java.nio.file.Path zipdir, JobPojo jobPojo) {
		Profile xsltDirProfile = ProfileBuilder.newBuilder()
                .name("Files in XSLT directory")
                .filePath(zipdir.toString() + "/*")
                .onLocalhost()
                .build();	
		GrepResults results = grep(
				constantExpression("xsl:template match=\"/\""),
				on(xsltDirProfile),
				with(option(filesMatching()))
				);
		if (results.isEmpty()) {
			jobPojo.fatal("Could not find root stylesheet containing 'xsl:template match=\"/\"'");
			jobPojo.setFailed();
			throw new RuntimeException("Could not find root stylesheet containing 'xsl:template match=\"/\"'");
		}
//		else if (results.size() >= 1) {
//			jobPojo.fatal("More than one stylesheet containing 'xsl:template match=\"/\"': " + results.size());
//			jobPojo.setFailed();
//			throw new RuntimeException("More than one stylesheet containing 'xsl:template match=\"/\"'");
//		}
		String rootStyleSheet = null;
		for (GrepResult result : results ) {
			if (null != result.getText() && ! "".equals(result.getText())) {
				rootStyleSheet = result.getFileName();
				break;
			}
		}
		return rootStyleSheet;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}