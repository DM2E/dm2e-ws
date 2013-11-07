package eu.dm2e.ws;

import java.io.IOException;

import org.apache.jena.fuseki.server.SPARQLServer;
import org.glassfish.grizzly.http.server.HttpServer;

import eu.dm2e.utils.GuiConsole;
import eu.dm2e.ws.wsmanager.ManageService;

/**
 * @deprecated
 * @see GuiConsole
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
