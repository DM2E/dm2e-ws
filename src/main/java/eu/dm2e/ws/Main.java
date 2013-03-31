package eu.dm2e.ws;

import com.sun.jersey.api.container.grizzly2.GrizzlyWebContainerFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

public class Main {

	static URI getBaseURI() {
		return UriBuilder.fromUri(
				Config.config.getString("dm2e.ws.base_uri", "http://localhost:9998/")).build();
	}

	protected static HttpServer startServer()
			throws IOException {
		final Map<String, String> initParams = new HashMap<String, String>();

		initParams.put("com.sun.jersey.config.property.packages", "eu.dm2e.ws.services");

		System.out.println("Starting grizzly2...");
		return GrizzlyWebContainerFactory.create(getBaseURI(), initParams);
	}

	public static void main(String[] args)
			throws IOException {

		if (null == Config.config) {
			System.err.println("No config was found. Create 'config.xml'.");
			System.exit(1);
		}

		// Grizzly 2 initialization
		HttpServer httpServer = startServer();
		System.out.println(String.format(
				"DM2E main services started (Data, Config, File). WADL at\n"
				+"%sapplication.wadl\n"
				+"Hit enter to stop",
				getBaseURI()));
		System.in.read();
		httpServer.stop();
	}
}
