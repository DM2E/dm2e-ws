package eu.dm2e.utils;
import static ch.qos.logback.core.pattern.color.ANSIConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dm2e.ws.wsmanager.ManageService;

public class GuiConsole {
	
	static Logger log;
	static Client client;
	static WebTarget manageResource;
	static int[] ports = { 9997, 9998 };
	final private static String SET_DEFAULT_COLOR = ESC_START+"0;"+DEFAULT_FG+ESC_END;

	
	static {
		System.setProperty("logback.configurationFile", "logback-console.xml");
		// Don't resolve external URLs
		System.setProperty("eu.dm2e.ws.grafeo.no_external_url", "true");
		log = LoggerFactory.getLogger(GuiConsole.class.getName());
		
		client = ClientBuilder.newClient();
		manageResource = client.target("http://localhost:9990/manage");
		
		try {
			ManageService.startManageServer();
		} catch (RuntimeException e) {
			log.warn("Manage server already running!", e);
		} catch (BindException e) {
			log.warn("Manage server already running!", e);
		}
	}
	
	
	private static void sendStartCommand() {
		manageResource.path("start").request().get();
	}
	private static void sendStopCommand() {
		manageResource.path("stop").request().get();
	}
	private static String sendPortcheckCommand(int port) {
		String resp = manageResource.path("port").path("" + port).request().get(String.class);
		return resp;
	}
	
	private static void startServer() {
		log.info("Starting Servers.");
		if (isRunning()) {
			log.warn("Servers ALREADY running.");
//			return;
		}
		sendStartCommand();
		log.info("Started Servers.");
	}
	private static void stopServer() {
		log.info("Stopping Servers.");
		if (! isRunning()) {
			log.warn("Servers are NOT running.");
//			return;
		}
		sendStopCommand();
		log.info("Stopped Servers.");
	}
	
	private static void restartServer() {
		log.info("Restarting Servers.");
		stopServer();
		startServer();
		if (isRunning()) 
			log.info("Restarted Servers.");
		else 
			log.warn("Restart failed.");
	}
	
	private static String getStatusString() {
		if (isRunning()) {
			return ESC_START + BOLD + CYAN_FG + ESC_END + "STARTED" + SET_DEFAULT_COLOR ;
		} else {
			return ESC_START + BOLD + RED_FG + ESC_END + "STOPPED" + SET_DEFAULT_COLOR;
		}	
					
	}
	private static boolean isRunning() {
		for (int port : ports) {
			if (Boolean.parseBoolean(sendPortcheckCommand(port)))
				return true;
		}
		return false;
	}
	
	public static void main(String[] args) throws SecurityException, IOException {
		
		startServer();
		
		while (true) {
			
		    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		    String input;
		    System.out.print("[ " + getStatusString() + " ] > ");
		    try {
				 input = bufferRead.readLine();
			} catch (IOException e) {
				break;
			}
			if (null == input) continue;
			if (input.matches("^q.*")) {
				break;
			} else if (input.matches("^r.*")) {
				restartServer();
			} else if (input.matches("^start.*")) {
				startServer();
			} else if (input.matches("^stop.*")) {
				stopServer();
			} else if (input.matches("^status.*")) {
				log.info("[{}]", getStatusString());
			} else {
				log.warn("Default to to nothing.");
//				restartServer();
			}
		}
		
		stopServer();
		
		log.info("GuiConsole Finished");
	}

}
