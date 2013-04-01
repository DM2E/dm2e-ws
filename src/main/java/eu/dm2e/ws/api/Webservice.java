package eu.dm2e.ws.api;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/30/13
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:Webservice")
public class Webservice {

    @RDFId(prefix="http://data.dm2e.eu/data/services/")
    private long id;

    @RDFProperty("omnom:helloWorld")
    private String hello;


    @RDFProperty("omnom:parameter")
    private Parameter parameter;

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }


    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
