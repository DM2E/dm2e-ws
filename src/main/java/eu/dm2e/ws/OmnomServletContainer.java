package eu.dm2e.ws;

import javax.servlet.ServletException;

import org.glassfish.jersey.servlet.ServletContainer;

import eu.dm2e.ws.wsmanager.ManageService;

public class OmnomServletContainer extends ServletContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public void init() throws ServletException {
//		ManageService.startFuseki();
		super.init();
	}
	
	@Override
	public void reload() {
//		ManageService.startFuseki();
		super.reload();
//		ManageService.stopFusekiServer();
	}
	
	@Override
	public void destroy() {
//		ManageService.stopFusekiServer();
		super.destroy();
	}

}
