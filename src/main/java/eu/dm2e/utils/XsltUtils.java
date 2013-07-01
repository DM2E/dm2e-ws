package eu.dm2e.utils;

import java.io.StringWriter;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Collection of tools for XSLT transformation
 * 
 * @author Konstantin Baierer
 */
public class XsltUtils {

	/**
	 * Parse a string of parameter/value pairs to a Map
	 * 
	 * @param str The string to parse
	 * @return {@link Map} of key-value-pairs
	 * @throws ParseException
	 */
	public static Map<String,String> parseXsltParameters(String str) throws ParseException {
		HashMap<String, String> map = new HashMap<>();
		if (null == str) return map;
		int offset = 0,
			newOffset = 0;
		for (String line : str.split("\\n")) {
			newOffset = 1 + offset + line.length();
			// remove comments
			line.replaceAll("#.*$", "");
			// strip trailing/leading whitespace
			line.replaceAll("^\\s*", "");
			line.replaceAll("\\s*$", "");
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
	 * Transform 
	 * @param xmlUrl
	 * @param xsltUrl
	 * @param paramMap
	 * @return
	 * @throws TransformerFactoryConfigurationError
	 * @throws Throwable
	 */
	public static StringWriter transformXslt(String xmlUrl, String xsltUrl, Map<String,String> paramMap)
			throws TransformerFactoryConfigurationError, Throwable {
		StringWriter xslResultStrWriter = new StringWriter();
		TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			StreamSource xmlSource = new StreamSource(new URL(xmlUrl).openStream());
			StreamSource xslSource = new StreamSource(new URL(xsltUrl).openStream());
			Transformer transformer = tFactory.newTransformer(xslSource);
			
			for (Entry<String,String> e: paramMap.entrySet())
				transformer.setParameter(e.getKey(), e.getValue());
	
			StreamResult xslResult = new StreamResult(xslResultStrWriter);
	
			transformer.transform(xmlSource, xslResult);
		} catch (Throwable t) {
			throw(t);
		}
		return xslResultStrWriter;
	}
	
}
