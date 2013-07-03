package eu.dm2e.ws.wsmanager;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import org.apache.jena.fuseki.server.DatasetRef;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
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
	
	static Logger log = Logger.getLogger(ManageService.class.getName());
	
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
        if (sparqlServer!=null) {
        	sparqlServer.stop();
        	if (isPortInUse(FUSEKI_PORT)) {
        		return ("Could not stop httpServer!");
        	}
        	sparqlServer = null;
        }
//        if (manageServer!=null) {
//	        manageServer.stop();
//	        manageServer = null;
//        }
        return "STOPPED";
    }

    public static void startManageServer() {
        final Map<String, String> initParams = new HashMap<>();
        
        initParams.put("com.sun.jersey.config.property.packages", "eu.dm2e.ws.wsmanager");
        
        System.out.println("Starting manageServer");
        try {
            manageServer = GrizzlyWebContainerFactory.create(UriBuilder.fromUri("http://localhost:" + MANAGE_PORT + "/").build(), initParams);
        } catch (IOException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }

    public static void startHttpServer() {
    	if (isPortInUse(OMNOM_PORT)) {
    		log.severe(OMNOM_PORT + " is already in use");
    		return;
    	}
    	
        final Map<String, String> initParams = new HashMap<>();
        initParams.put("com.sun.jersey.config.property.packages", "eu.dm2e.ws.services");
        
        log.info("Starting httpServer");
        
        try {
            httpServer =  GrizzlyWebContainerFactory.create(UriBuilder.fromUri("http://localhost:" + OMNOM_PORT +"/").build(), initParams);
            httpServer.getServerConfiguration().addHttpHandler(new StaticHttpHandler("static"), "/static");
        } catch (IOException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }

    }
    public static void stopHttpServer() {
        if (httpServer!=null) {
        	httpServer.stop();
        	if (isPortInUse(OMNOM_PORT)) {
        		log.severe("Could not stop httpServer!");
        	}
        	httpServer = null;
        }
    }
    public static void startFuseki() {
    	if (isPortInUse(FUSEKI_PORT)) {
    		log.severe(FUSEKI_PORT + " is already in use");
    		return;
    	}
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
    public static void stopFusekiServer() {
        if (sparqlServer!=null) {
        	sparqlServer.stop();
        	if (isPortInUse(FUSEKI_PORT)) {
        		log.severe("Could not stop sparqlServer!");
        	}
        	sparqlServer = null;
        }
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
    	
		if (null == httpServer) {
			startHttpServer();
	    }
		if (null == sparqlServer) {
			startFuseki();
	    }
    }

    public static void stopAll() {
    	
    	stopHttpServer();
    	stopFusekiServer();

//        Client client = new Client();
//        String response = client.resource("http://localhost:" + MANAGE_PORT + "/manage/stop").get(String.class);
//        System.out.println("Stopped all servers. Last response: " + response);
    }
}
