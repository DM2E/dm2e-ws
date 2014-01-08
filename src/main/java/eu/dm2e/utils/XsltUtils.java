package eu.dm2e.utils;

import static org.grep4j.core.Grep4j.*;
import static org.grep4j.core.fluent.Dictionary.*;
import static org.grep4j.core.options.Option.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.grep4j.core.model.Profile;
import org.grep4j.core.model.ProfileBuilder;
import org.grep4j.core.result.GrepResult;
import org.grep4j.core.result.GrepResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.services.Client;
import eu.dm2e.ws.services.xslt.OmnomErrorListener;

/**
 * Collection of tools for XSLT transformation
 * 
 * @author Konstantin Baierer
 */
public class XsltUtils {
	
	private JobPojo jobPojo;
	@SuppressWarnings("unused")
	private Client client;
	private Logger log;
	
	public XsltUtils(Client client, JobPojo job) {
		log = LoggerFactory.getLogger(getClass().getName());
		this.jobPojo = job;
		this.client = client;
	}
	
	/** Default XSLT parameter names and values for provider/dataset parameter */
	public static class PARAMETER_DEFAULTS {
		public static final String DATAPROVIDER_KEY = "dataprovider";
		public static final String DATAPROVIDER_VALUE = "WARNING-dataprovider-not-set";
		public static final String DATASET_KEY = "dataset";
		public static final String DATASET_VALUE = "WARNING-dataset-not-set";
		public static final String DM2E_BASEURI_KEY = "dm2e_baseuri";
		public static final String DM2E_BASEURI_VALUE = "http://data.dm2e.eu/data/";
	}

	/**
	 * Parse a string of parameter/value pairs to a Map
	 * 
	 * @param str The string to parse
	 * @return {@link Map} of key-value-pairs
	 * @throws ParseException
	 */
	public Map<String,String> parseXsltParameters(String str) throws ParseException {
		HashMap<String, String> map = new HashMap<>();
		if (null == str) return map;
		int offset = 0, newOffset = 0;
		
		log.debug("Parsing " + str);

		String lineSeparator = "\\n";
		// if the string contains semicolons, assume lines are separated by semicolon instead of newline
		if (str.contains(";")) {
			lineSeparator = ";";
		}
		log.debug("Line Separator determined to be '" + lineSeparator + "'.");
		for (String line : str.split(lineSeparator)) {
			newOffset = 1 + offset + line.length();
			// remove comments
			line = line.replaceAll("#.*$", "");
			// strip trailing/leading whitespace
			line = line.trim();
			if (line.equals("")) continue;
			String[] kvList = line.split("\\s*=\\s*");
			try {
				map.put(kvList[0], kvList[1]);
			} catch (Throwable t) {
				t.printStackTrace();
				throw new ParseException(str, offset);
			}
			offset = newOffset;
		}
		return map;
	}

	/**
	 * Transform an XML URL using an XSLT URL.
	 * 
	 * @param xmlUrl
	 * @param xsltUrl
	 * @param paramMap
	 * @return StreamResultWriter of the result
	 * @throws TransformerFactoryConfigurationError
	 * @throws Throwable
	 */
	public StringWriter transformXsltUrl(String xmlUrl, String xsltUrl, Map<String,String> paramMap)
			throws TransformerFactoryConfigurationError, Throwable {
		StreamSource xmlSource = new StreamSource(new URL(xmlUrl).openStream());
		StreamSource xslSource = new StreamSource(new URL(xsltUrl).openStream());
		
		return doTransformXslt(xslSource, xmlSource, paramMap);
	}
	
	public StringWriter transformXsltFile(String xmlUrl, String xsltPath, Map<String,String> paramMap) throws MalformedURLException, IOException, TransformerException {
		StreamSource xslSource = new StreamSource(new File(xsltPath));
		xslSource.setSystemId(new File(xsltPath));
		StreamSource xmlSource = new StreamSource(new URL(xmlUrl).openStream());
		
		return doTransformXslt(xslSource, xmlSource, paramMap);
		
	}

	/**
	 * @param xslSource
	 * @param xmlSource
	 * @param paramMap
	 * @return
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 */
	private StringWriter doTransformXslt(StreamSource xslSource,
			StreamSource xmlSource,
			Map<String, String> paramMap)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException,
			TransformerException {
		StringWriter xslResultStrWriter = new StringWriter();
		StreamResult xslResult = new StreamResult(xslResultStrWriter);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		addErrorListenerToTFactory(tFactory);
		Transformer transformer = tFactory.newTransformer(xslSource);
		
		for (Entry<String,String> e: paramMap.entrySet()) {
			jobPojo.debug(String.format("Setting XSLT parameter '%s' to '%s'", e.getKey(), e.getValue()));
			transformer.setParameter(e.getKey(), e.getValue());
		}

		transformer.transform(xmlSource, xslResult);
		return xslResultStrWriter;
	}
	
	


	/**
	 * @param zipdir
	 * @return
	 */
	public String grepRootStylesheet(String zipdir) {
		Profile xsltDirProfile = ProfileBuilder
			.newBuilder()
			.name("Files in XSLT directory")
			.filePath(zipdir + "/*")
			.onLocalhost()
			.build();
		GrepResults results = grep(constantExpression("xsl:template match=\"/\""),
				on(xsltDirProfile), with(option(filesMatching())));
		if (results.isEmpty()) {
			jobPojo.fatal("Could not find root stylesheet containing 'xsl:template match=\"/\"'");
			jobPojo.setFailed();
			throw new RuntimeException(
					"Could not find root stylesheet containing 'xsl:template match=\"/\"'");
		}
		// else if (results.size() >= 1) {
		// jobPojo.fatal("More than one stylesheet containing 'xsl:template match=\"/\"': "
		// + results.size());
		// jobPojo.setFailed();
		// throw new
		// RuntimeException("More than one stylesheet containing 'xsl:template match=\"/\"'");
		// }
		String rootStyleSheet = null;
		for (GrepResult result : results) {
			if (null != result.getText() && !"".equals(result.getText())) {
				rootStyleSheet = result.getFileName();
				break;
			}
		}
		return rootStyleSheet;
	}
	
	public void addErrorListenerToTFactory(TransformerFactory tFactory) {
		OmnomErrorListener errL;
		try {
			jobPojo.debug("Instantiating custom error listener");
			errL = new OmnomErrorListener(jobPojo);
		} catch (Exception e) {
			jobPojo.fatal(e);
			jobPojo.setFailed();
			return;
		}
		jobPojo.debug("Adding custom error listener");
		tFactory.setErrorListener(errL);
		jobPojo.debug("Done Adding custom error listener");
	}

}
