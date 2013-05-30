package eu.dm2e.ws.services.data;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
 */
public class DataServiceITCase {

        Logger log = Logger.getLogger(getClass().getName());

        private Client client;

        @Before
        public void setUp()
                throws Exception {
            client = new Client();
        }

        @After
        public void tearDown() {
        }

        @Test
        public void testData() {
            // fail("Not yet implemented");
            String URI_BASE = "http://localhost:9998";
            WebResource webResource = client.resource(URI_BASE + "/data/configurations");
            ClientResponse response = webResource.post(ClientResponse.class, "[] <http://purl.org/dc/terms/creator> <http://localhost/kai>; <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://onto.dm2e.eu/omnom/WebServiceConfig> .");
            Grafeo g = new GrafeoImpl();
            g.readHeuristically(response.getEntity(String.class));
            Grafeo g2 = new GrafeoImpl();
            g2.addTriple(response.getLocation().toString(), "http://purl.org/dc/terms/creator", g.resource("http://localhost/kai"));
            g2.addTriple(response.getLocation().toString(), "rdf:type", g.resource("omnom:WebServiceConfig"));
            assert(g.isGraphEquivalent(g2));

            g2 = new GrafeoImpl();
            g2.addTriple(response.getLocation().toString(), "http://doesnotexist.org/bla", g.resource("http://localhost/kai"));
            g2.addTriple(response.getLocation().toString(), "rdf:type", g.resource("omnom:WebServiceConfig"));
            assertFalse(g.isGraphEquivalent(g2));

        }

}
