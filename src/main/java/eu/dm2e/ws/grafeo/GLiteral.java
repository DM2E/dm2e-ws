package eu.dm2e.ws.grafeo;



/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public interface GLiteral extends GValue {
    String getValue();

	/**
	 * Returns a string version of the typed literal
	 * 
	 * @return
	 */
	String getTypedValue();
}
