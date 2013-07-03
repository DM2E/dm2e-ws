package eu.dm2e.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import eu.dm2e.ws.wsmanager.ManageService;

public class GuiConsole {
	
	static boolean isRunning = false;
	
	private static void startServer() {
		Logger log = Logger.getLogger(GuiConsole.class.getName());
		log.info("Starting Servers.");
		if (isRunning) {
			log.severe("Servers Already running.");
			return;
		}
		ManageService.startAll();
		System.out.println("Started Servers.");
		isRunning = true;
	}
	private static void stopServer() {
		Logger log = Logger.getLogger(GuiConsole.class.getName());
		System.out.println("Stopping Servers.");
		if (! isRunning) {
			System.out.println("ERROR: Not running.");
			return;
		}
		ManageService.stopAll();
		System.out.println("Stopped Servers.");
		isRunning = false;
	}
	
	private static void restartServer() {
		Logger log = Logger.getLogger(GuiConsole.class.getName());
		System.out.println("Restarting Servers.");
		stopServer();
		startServer();
		System.out.println("Restarted Servers.");
	}
	
	public static void main(String[] args) throws SecurityException, IOException {
		
		Logger log = Logger.getLogger(GuiConsole.class.getName());
		log.info("Logging Handlers: " + Arrays.asList(log.getHandlers()));
		log.info(System.getProperty("java.util.logging.config.file"));
		
		LogManager.getLogManager().readConfiguration();
		log.info(System.getProperty("java.util.logging.config.file"));
//		System.setProperty("java.util.logging.config.file", "/home/kb/Dropbox/workspace/dm2e-ws/target/classes/logging.properties");
		System.setProperty("java.util.logging.config.file", "logging.properties");
		LogManager.getLogManager().readConfiguration();
		log = Logger.getLogger(GuiConsole.class.getName());
		log.info(System.getProperty("java.util.logging.config.file"));
		log.info(LogManager.getLogManager().getProperty("java.util.logging.ConsoleHandler.formatter"));
		log.info("Logging Handlers: " + Arrays.asList(log.getHandlers()));
		if (true) return;
		
		startServer();
		
		while (true) {
			
		    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		    String input;
		    try {
				 input = bufferRead.readLine();
			} catch (IOException e) {
				break;
			}
			if (null == input) continue;
			if (input.matches("^q.*")) {
				stopServer();
				break;
			} else if (input.matches("^r.*")) {
				restartServer();
			} else if (input.matches("^start.*")) {
				startServer();
			} else if (input.matches("^stop.*")) {
				stopServer();
			} else if (input.matches("^status.*")) {
				if (isRunning) {
					System.out.println("[RUNNING]");
				} else {
					System.out.println("[STOPPED]");
				}
			} else {
				System.out.println("Huh?");
			}

		}
		System.out.println("Finished");
	}

}
