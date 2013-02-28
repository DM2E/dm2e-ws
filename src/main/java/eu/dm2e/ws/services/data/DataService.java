
package eu.dm2e.ws.services.data;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import javax.ws.rs.Path;


@Path("/data")
public class DataService extends AbstractRDFService {


    @Override
    protected Model getRDF() {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefix("dct","http://purl.org/dc/terms/");
        Resource s = m.createResource("http://localhost/data");
        Property p = m.createProperty(m.expandPrefix("http://purl.org/dc/terms/creator"));
        Resource o = m.createResource("http://localhost/kai");
        m.add(m.createStatement(s, p, o));

        return m;
    }
}