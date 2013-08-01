package eu.dm2e.ws;

import eu.dm2e.ws.wsmanager.ManageService;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;

/**
 * @deprecated
 */
public class Main {

    static HttpServer httpServer;
    static HttpServer manageServer;
    static SPARQLServer sparqlServer;

	public static void main(String[] args)
			throws IOException {

		if (Config.isNull()) {
			System.err.println("No config was found. Create 'config.xml'.");
			System.exit(1);
		}

		// Grizzly 2 initialization
		ManageService.startHttpServer();
		System.out.println(String.format(
				"OMNOM main services started (Data, Config, File). WADL at\n"
				+"\n"
				+"Hit enter to stop"));
        ManageService.startFuseki();
		System.in.read();
		ManageService.stopAll();

	}
}
