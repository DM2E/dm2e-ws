package eu.dm2e.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * This engine renders a subset of the syntax of the Mustache template system.
 * 
 * String tplStr = "X{{ foo }}X"; String outStr = TemplateEngine.render(tplStr,
 * "foo", "bar"); assertEquals("XbarX", outStr);
 * 
 * @author kb
 * 
 */
public class TemplateEngine {

	public static String render(String tmpl, String... params) {
		if (params.length % 2 > 0) {
			throw new RuntimeException("Odd number of arguments.");
		}
		Map<String, String> map = new HashMap<String, String>();
		for (Iterator<String> iter = Arrays.asList(params).iterator(); iter.hasNext();) {
			map.put(iter.next(), iter.next());
		}
		return render(tmpl, map);
	}

	public static String render(String tmpl, Map<String, String> map) {
		if (null == tmpl) {
			throw new RuntimeException("Can't render a null string");
		}
		String outStr = tmpl;
		for (Entry<String, String> entry : map.entrySet()) {
			outStr = outStr.replaceAll("\\{\\{\\s*" + entry.getKey() + "\\s*\\}\\}", entry
				.getValue());
		}
		return outStr;
	}

	public static void main(String[] args) {
		String fileName = args[0];
		System.out.println("Hi");
		
		if (args.length % 2 == 0) {
			throw new RuntimeException("Odd number of arguments.");
		}
		Map<String, String> map = new HashMap<String, String>();
		Iterator<String> iter = Arrays.asList(args).iterator();
		iter.next();
		while (iter.hasNext()) {
			map.put(iter.next(), iter.next());
		}
		String tmplStr;
		try {
			 tmplStr = FileUtils.readFileToString( new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		String outStr = render(tmplStr, map);
		System.out.println(outStr);
	}
	
	@Test
	public void testTemplateEngine() {
		String tplStr = "X{{ foo }}X";
		String outStr = TemplateEngine.render(tplStr, "foo", "bar");
		Assert.assertEquals("XbarX", outStr);
	}
}
