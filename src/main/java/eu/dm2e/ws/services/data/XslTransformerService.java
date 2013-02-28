package eu.dm2e.ws.services.data;

/*
 * GET /xslt?resource=URL
 * 		
 * POST /xslt
 * PUT /xslt
 * 		Body contains URL of a transformation specification which must be dereferenceable
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.impl.xpath.XPath.Step;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

@Path("/xslt")
public class XslTransformerService extends AbstractRDFService {

	@PUT
	public Response post(String body) {
		
		Logger log  = Logger.getLogger(getClass().getName());
		String xslUrl = null;
		String xmlUrl = null;
		
		Model transformationModel = ModelFactory.createDefaultModel();
		transformationModel.read(body, "http://foo.bar/", "TURTLE");
		StmtIterator iter = transformationModel.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.next();
			String predString = stmt.getPredicate().toString();
			String objString = stmt.getObject().toString();
			if ("http://onto.dm2e.eu/xmlSource".equals(predString)) {
				xmlUrl = objString;
			}
			else if ("http://onto.dm2e.eu/xslStyleSheet".equals(predString)) {
				xslUrl = objString;
			}
			else {
				log.warning("Unknown predicate: " + predString);
		}
		}
		if (null == xslUrl || null == xmlUrl) {
			return Response.ok("Not OK in fact...").build();
		}
        TransformerFactory tFactory = TransformerFactory.newInstance();  
        try {
        	
        	StreamSource xmlSource = new StreamSource(new URL(xmlUrl).openStream());
        	StreamSource xslSource = new StreamSource(new URL(xslUrl).openStream());
        	Transformer transformer = tFactory.newTransformer(xslSource);  

//        	StreamResult outStream = new StreamResult(new By);
        	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        	StreamResult xslResult = new StreamResult(outStream);

        	transformer.transform(xmlSource, xslResult);
        	log.warning(outStream.toString());
    		return Response.ok(outStream.toString()).build();
        } catch (Exception e) {  
        	e.printStackTrace();  
        }  
		return Response.ok(xmlUrl).build();
		
//		return Response.ok(new RDFOutput(transformationModel, new MediaType("text", "turtle"))).build();
		
	}
	
	@Override
	protected Model getRDF() {
        Logger log = Logger.getLogger(getClass().getName());
        log.log(Level.INFO, "FOO");
//        Model m = ModelFactory.createDefaultModel();
		Model m = ModelFactory.createDefaultModel();
        m.setNsPrefix("dct","http://purl.org/dc/terms/");
        Resource s = m.createResource("http://localhost/data");
        Property p = m.createProperty(m.expandPrefix("http://purl.org/dc/terms/creator"));
        Resource o = m.createResource("http://localhost/kai");
        m.add(m.createStatement(s, p, o));

		InputStream sampleDataStream = this.getClass().getResourceAsStream("/sample_data.ttl");
		if (null == sampleDataStream) {
			log.severe("Couldn't open sample_data.ttl...");
			return m;
		}
//		String sampleDataString = new Scanner(sampleDataStream).useDelimiter("\\A").next();
//		log.warning(sampleDataString);
		m.read(sampleDataStream, "http://example.com/", "TURTLE");
		
        return m;
	}

}
