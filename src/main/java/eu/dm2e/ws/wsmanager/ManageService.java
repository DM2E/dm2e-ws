package eu.dm2e.ws.wsmanager;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import org.apache.jena.fuseki.server.DatasetRef;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.container.grizzly2.GrizzlyWebContainerFactory;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
@Path("/manage")
public class ManageService {
	
	private static final int FUSEKI_PORT = 9997;
	private static final int MANAGE_PORT = 9990;
	private static final int OMNOM_PORT = 9998;

    private static HttpServer httpServer;
    @SuppressWarnings("unused")
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


    public static void startServer() {
        final Map<String, String> initParams = new HashMap<>();
        final Map<String, String> initParams2 = new HashMap<>();

        initParams.put("com.sun.jersey.config.property.packages", "eu.dm2e.ws.services");
        initParams2.put("com.sun.jersey.config.property.packages", "eu.dm2e.ws.wsmanager");

        System.out.println("Starting grizzly2...");
        try {
            manageServer = GrizzlyWebContainerFactory.create(UriBuilder.fromUri("http://localhost:" + MANAGE_PORT + "/").build(), initParams2);
            httpServer =  GrizzlyWebContainerFactory.create(UriBuilder.fromUri("http://localhost:" + OMNOM_PORT +"/").build(), initParams);
            httpServer.getServerConfiguration().addHttpHandler(new StaticHttpHandler("static"), "/static");
        } catch (IOException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }

    }


    public static void startFuseki() {
         System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize", "-1");
        ServerConfig config = new ServerConfig();
        config.port = 9997;
        config.pagesPort = 9997;
        config.jettyConfigFile = "src/main/resources/fuseki-jetty.xml";
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
    
    private static boolean isPortInUse(int port) {
    	ServerSocket socket;
		try {
			socket = new ServerSocket(port);
			try {
				socket.close();
			} catch (Exception e) {
				// do nothing
			}
		} catch (Exception e) {
			return true;
		}
		return false;
    }

    public static void startAll() {
    	
//    	Logger log = Logger.getLogger(ManageService.class.getName());
		if (null == httpServer && ! isPortInUse(OMNOM_PORT)) {
			startServer();
	    }
		if (null == sparqlServer && ! isPortInUse(FUSEKI_PORT)) {
			startFuseki();
	    }
    }

    public static void stopAll() {

        Client client = new Client();
        String response = client.resource("http://localhost:" + MANAGE_PORT + "/manage/stop").get(String.class);
        System.out.println("Stopped all servers. Last response: " + response);
    }
}
