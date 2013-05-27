package eu.dm2e.ws.wsmanager;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.container.grizzly2.GrizzlyWebContainerFactory;
import eu.dm2e.ws.Config;
import org.apache.jena.fuseki.server.DatasetRef;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 5/27/13
 * Time: 9:01 AM
 * To change this template use File | Settings | File Templates.
 */
@Path("/manage")
public class ManageService {

    static HttpServer httpServer;
    static HttpServer manageServer;
    static SPARQLServer sparqlServer;

    @GET
    @Path("/stop")
    public String stop() {
        if (httpServer!=null) httpServer.stop();
        if (sparqlServer!=null) sparqlServer.stop();
        // if (manageServer!=null) manageServer.stop();
        return "STOPPED";
    }


    public static URI getBaseURI() {
        return UriBuilder.fromUri(
                Config.config.getString("dm2e.ws.base_uri", "http://localhost:9998/")).build();
    }
    public static void startServer() {
        final Map<String, String> initParams = new HashMap<String, String>();
        final Map<String, String> initParams2 = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages", "eu.dm2e.ws.services");
        initParams2.put("com.sun.jersey.config.property.packages", "eu.dm2e.ws.wsmanager");

        System.out.println("Starting grizzly2...");
        try {
            manageServer = GrizzlyWebContainerFactory.create(UriBuilder.fromUri("http://localhost:9990/").build(), initParams2);
            httpServer =  GrizzlyWebContainerFactory.create(getBaseURI(), initParams);
        } catch (IOException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }

    }


    public static void startFuseki() {
        ServerConfig config = new ServerConfig();
        config.port = 8000;
        config.pagesPort = 8000;
        DatasetRef ds = new DatasetRef();
        ds.name = "test";
        config.services = new ArrayList<>();
        config.services.add(ds);
        config.pages = "test";
        ds.allowDatasetUpdate = true;
        DatasetGraph dsg = DatasetGraphFactory.createMem();
        ds.dataset = dsg;
        ds.updateEP.add("update");
        ds.queryEP.add("sparql");
        SPARQLServer server = new SPARQLServer(config);
        server.start();
        sparqlServer = server;

    }

    public static void startAll() {
        startServer();
        startFuseki();
    }

    public static void stopAll() {

        Client client = new Client();
        String response = client.resource("http://localhost:9990/manage/stop").get(String.class);
        System.out.println("Stopped all servers. Last response: " + response);
    }
}
