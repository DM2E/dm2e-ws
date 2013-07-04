package eu.dm2e.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dm2e.ws.wsmanager.ManageService;

public class GuiConsole {
	
	static boolean isRunning = false;
	
	private static void startServer() {
		Logger log = LoggerFactory.getLogger(GuiConsole.class.getName());
		log.info("Starting Servers.");
		if (isRunning) {
			log.error("Servers Already running.");
			return;
		}
		ManageService.startAll();
		System.out.println("Started Servers.");
		isRunning = true;
	}
	private static void stopServer() {
//		Logger log = LoggerFactory.getLogger(GuiConsole.class.getName());
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
//		Logger log = LoggerFactory.getLogger(GuiConsole.class.getName());
		System.out.println("Restarting Servers.");
		stopServer();
		startServer();
		System.out.println("Restarted Servers.");
	}
	
	public static void main(String[] args) throws SecurityException, IOException {
		
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
