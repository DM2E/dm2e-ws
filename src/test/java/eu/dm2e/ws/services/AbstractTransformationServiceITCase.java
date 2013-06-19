package eu.dm2e.ws.services;

//import static org.junit.Assert.*;

import javax.ws.rs.Path;

import org.junit.Before;
import org.junit.Test;

import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;

public class AbstractTransformationServiceITCase {
	
	private MockService mockInstance;
	
	@Path("/mock")
	private class MockService extends AbstractTransformationService {

		@Override
		public void run() { } 
		
		@Override
		public WebservicePojo getWebServicePojo() {
			WebservicePojo ws = super.getWebServicePojo();
			ws.addInputParameter("foo").setIsRequired(true);
			ws.addOutputParameter("bar");
			return ws;
		}
	}

	@Before
	public void setUp() throws Exception {
		mockInstance = new MockService();
	}
	
	@Test
	public void validateConfig() throws Exception {
		WebserviceConfigPojo conf = new WebserviceConfigPojo();
		conf.setWebservice(mockInstance.getWebServicePojo());
		conf.addParameterAssignment("foo", "quux");
		conf.validate();
	}

}
