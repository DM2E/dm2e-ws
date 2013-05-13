package eu.dm2e.ws.api;

import eu.dm2e.ws.api.omnom.Webservice;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/31/13
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:Parameter")
public class Parameter {
    @RDFId(prefix="http://data.dm2e.eu/data/parameters/")
    private long id;

    @RDFProperty("omnom:helloWorld")
    private String hello;

    @RDFProperty("omnom:webservice")
    private Webservice webservice;

    public Webservice getWebservice() {
        return webservice;
    }

    public void setWebservice(Webservice webservice) {
        this.webservice = webservice;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }


}
