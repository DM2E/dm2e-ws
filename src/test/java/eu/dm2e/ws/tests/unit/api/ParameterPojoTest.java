package eu.dm2e.ws.tests.unit.api;



import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashSet;

import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;
import org.junit.Ignore;
import org.junit.Test;

import eu.dm2e.ws.tests.OmnomTestCase;
import eu.dm2e.ws.services.xslt.XsltService;

public class ParameterPojoTest extends OmnomTestCase {
	
	@Ignore("This isn't working")
	@Test
	public void testEqualsSet() {
		
		HashSet<ParameterPojo> set1 = new HashSet<>();
		HashSet<ParameterPojo> set2 = new HashSet<>();
		ParameterPojo p1 = new ParameterPojo();
		ParameterPojo p2 = new ParameterPojo();
		ParameterPojo p3 = new ParameterPojo();
		ParameterPojo p4 = new ParameterPojo();
		set1.add(p1);
		set2.add(p2);
		p1.setDefaultValue("5");
		p2.setDefaultValue("5");
		p3.setDefaultValue("5");
		p4.setDefaultValue("5");
		
//		assertEquals(set1, set2);
		assertEquals(p1, p2);
//		assertEquals(set1, set2);
		p1.setWebservice(new WebservicePojo());
		p2.setWebservice(new XsltService().getWebServicePojo());
//		assertEquals(set1, set2);
		
		
	}
	
	@Ignore
	@Test
	public void testEquals() {
		ParameterPojo p1 = new ParameterPojo();
		ParameterPojo p2 = new ParameterPojo();
		assertEquals(p1, p2);
		{
			p1.setDefaultValue("5");
			assertThat(p2, not(p1));
			p2.setDefaultValue("5");
			assertThat(p2, is(p1));
			assertThat(p1, is(p2));
		}
		{
			p1.setComment("foo comment");
			assertThat(p2, not(p1));
			p2.setComment("foo comment");
			assertThat(p2, is(p1));
			assertThat(p1, is(p2));
		}
		{
			p1.setIsRequired(true);
			assertThat(p2, not(p1));
			p2.setIsRequired(true);
			assertThat(p2, is(p1));
			assertThat(p1, is(p2));
		}
		{
			p1.setParameterType("footype");
			assertThat(p2, not(p1));
			p2.setParameterType("footype");
			assertThat(p2, is(p1));
			assertThat(p1, is(p2));
		}
	}

}
