package eu.dm2e.ws.wsmanager;

import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;

import org.apache.jena.fuseki.server.DatasetRef;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;

import eu.dm2e.grafeo.jaxrs.GrafeoMessageBodyWriter;
import eu.dm2e.grafeo.json.GrafeoJsonSerializer;
import eu.dm2e.ws.SerializablePojoListMessageBodyWriter;
import eu.dm2e.ws.SerializablePojoProvider;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.LogEntryPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.ParameterConnectorPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.UserPojo;
import eu.dm2e.ws.api.VersionedDatasetPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;
//import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * JAX-RS service to start the DM2E services -- for testing only.
 * <p>
 * Relevant changes here must be ported to {@link eu.dm2e.ws.OmnomApplication}.
 * </p>
 */
@Path("/manage")
public class ManageService {
	
	static Logger log = LoggerFactory.getLogger(ManageService.class.getName());
	
	static {
		// Set to true to tell the user service to return a dummy user name
		System.setProperty("dm2e-ws.isTestRun", "true");
		System.setProperty("dm2e-ws.test.properties_file", "dm2e-ws.test.properties");
		 // Optionally remove existing handlers attached to j.u.l root logger
		 SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)

		 // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
		 // the initialization phase of your application
		 SLF4JBridgeHandler.install();

		 GrafeoJsonSerializer.registerType(JobPojo.class);
		 GrafeoJsonSerializer.registerType(FilePojo.class);
		 GrafeoJsonSerializer.registerType(LogEntryPojo.class);
		 GrafeoJsonSerializer.registerType(ParameterAssignmentPojo.class);
		 GrafeoJsonSerializer.registerType(ParameterConnectorPojo.class);
		 GrafeoJsonSerializer.registerType(ParameterPojo.class);
		 GrafeoJsonSerializer.registerType(UserPojo.class);
		 GrafeoJsonSerializer.registerType(VersionedDatasetPojo.class);
		 GrafeoJsonSerializer.registerType(WebserviceConfigPojo.class);
		 GrafeoJsonSerializer.registerType(WebservicePojo.class);
		 GrafeoJsonSerializer.registerType(WorkflowPojo.class);
		 GrafeoJsonSerializer.registerType(WorkflowPositionPojo.class);
	}
	
	private static final int FUSEKI_PORT = 9997;
	private static final int MANAGE_PORT = 9990;
	private static final int OMNOM_PORT = 9998;

    private static HttpServer httpServer;
    @SuppressWarnings("unused")
	private static HttpServer manageServer;
    private static SPARQLServer sparqlServer;

    @GET
    @Path("/start")
    public String start(@QueryParam("webappPath") String webappPathParam) {
		if (null == httpServer) {
			startHttpServer(webappPathParam);
	    }
		if (null == sparqlServer) {
			startFuseki();
	    }
    	return "Tried to start";
    }

    @GET
    @Path("/stop")
    public String stop() {
    	stopAll();
    	return "Tried to stop";
    }

    public static void startManageServer() throws BindException {
//        final Map<String, String> initParams = new HashMap<>();
        
//        initParams.put("com.sun.jersey.config.property.packages", "eu.dm2e.ws.wsmanager");
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig()
	        .packages("eu.dm2e.ws.wsmanager");

        
        System.out.println("Starting manageServer");
        manageServer = GrizzlyHttpServerFactory.createHttpServer(UriBuilder.fromUri("http://localhost:" + MANAGE_PORT + "/").build(), rc);

    }
    public static void startHttpServer() {
    	startHttpServer("WebContent");
    }

    public static void startHttpServer(String webappPath) {
    	if (null == webappPath) {
    		log.warn("webappPath is NULLLLLLL");
    		startHttpServer();
    	}
    	if (isPortInUse(OMNOM_PORT)) {
    		log.error(OMNOM_PORT + " is already in use");
    		return;
    	}
    	
//        final Map<String, String> initParams = new HashMap<>();
//        initParams.put("com.sun.jersey.config.property.packages", "eu.dm2e.ws.services");
    	final ResourceConfig resourceConfig = new ResourceConfig()
        	.packages("eu.dm2e.ws.services")
        	// multipart/form-data
	        .register(MultiPartFeature.class)
	        // setting pojos as response entity
	        .register(SerializablePojoProvider.class)
	        // setting a list of pojos as response entity
	        .register(SerializablePojoListMessageBodyWriter.class)
	        // setting Grafeos as response entity
	        .register(GrafeoMessageBodyWriter.class)
	        // Log Jersey-internal server communication
	        .register(LoggingFilter.class);

        
        log.info("Starting httpServer");
        
        httpServer = GrizzlyHttpServerFactory.createHttpServer(
        		UriBuilder.fromUri("http://localhost:" + OMNOM_PORT + "/api").build(), resourceConfig);
        httpServer.getServerConfiguration().addHttpHandler(new StaticHttpHandler(webappPath), "/");
        // disable caching for the static files
        for (NetworkListener l : httpServer.getListeners()) {
        	l.getFileCache().setEnabled(false);
        }

    }
    public static void stopHttpServer() {
        if (httpServer!=null) {
        	httpServer.stop();
        	if (isPortInUse(OMNOM_PORT)) {
        		log.error("Could not stop httpServer!");
        	}
        	httpServer = null;
        }
    }
    public static void startFuseki() {
    	if (isPortInUse(FUSEKI_PORT)) {
    		log.error(FUSEKI_PORT + " is already in use");
    		return;
    	}
    	// TODO doesnt work
//    	System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize", "-1");
        ServerConfig config = new ServerConfig();
        config.port = 9997;
        config.pagesPort = 9997;
    	// TODO doesnt work
//        config.jettyConfigFile = "src/main/resources/fuseki-jetty.xml";
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
        		log.error("Could not stop sparqlServer!");
        	}
        	sparqlServer = null;
        }
    }


    
    @GET
    @Path("/port/{port}")
    public static String isPortInUse(@PathParam("port") String portStr) {
    	int port = Integer.parseInt(portStr);
    	boolean resp = isPortInUse(port);
    	return Boolean.toString(resp);
    }
    
    public static boolean isPortInUse(int port) {
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
    	
    	log.info("Stopping Http Server.");
    	stopHttpServer();
    	log.info("Stopping Fuseki Server.");
    	stopFusekiServer();

//        Client client = new Client();
//        String response = client.resource("http://localhost:" + MANAGE_PORT + "/manage/stop").get(String.class);
//        System.out.println("Stopped all servers. Last response: " + response);
    }
}
