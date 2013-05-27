package eu.dm2e.ws;

import eu.dm2e.ws.wsmanager.ManageService;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.Path;
import java.io.IOException;

@Path("manage")
public class Main {

    static HttpServer httpServer;
    static HttpServer manageServer;
    static SPARQLServer sparqlServer;

	public static void main(String[] args)
			throws IOException {

		if (null == Config.config) {
			System.err.println("No config was found. Create 'config.xml'.");
			System.exit(1);
		}

		// Grizzly 2 initialization
		ManageService.startServer();
		System.out.println(String.format(
				"DM2E main services started (Data, Config, File). WADL at\n"
				+"\n"
				+"Hit enter to stop"));
        ManageService.startFuseki();
		System.in.read();
		ManageService.stopAll();

	}
}
