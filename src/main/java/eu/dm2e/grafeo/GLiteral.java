package eu.dm2e.grafeo;



/**
 * A RDF literal.
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
