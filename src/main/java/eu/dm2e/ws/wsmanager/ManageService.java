package eu.dm2e.ws.wsmanager;

import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.container.grizzly2.GrizzlyWebContainerFactory;
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
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
@Path("/manage")
public class ManageService {

    private static HttpServer httpServer;
    private static HttpServer manageServer;
    private static SPARQLServer sparqlServer;

    @GET
    @Path("/stop")
    public String stop() {
        if (httpServer!=null) httpServer.stop();
        if (sparqlServer!=null) sparqlServer.stop();
        // if (manageServer!=null) manageServer.stop();
        return "STOPPED";
    }


    public static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:9998/").build();
    }
    public static void startServer() {
        final Map<String, String> initParams = new HashMap<>();
        final Map<String, String> initParams2 = new HashMap<>();

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
        config.port = 9997;
        config.pagesPort = 9997;
        DatasetRef ds = new DatasetRef();
        ds.name = "test";
        config.services = new ArrayList<>();
        config.services.add(ds);
        config.pages = "test";
        ds.allowDatasetUpdate = true;
        ds.dataset = DatasetGraphFactory.createMem();
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
