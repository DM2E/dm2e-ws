/**
 * 
 */
package eu.dm2e.ws.grafeo;

/**
 * Constants representing methods for renaming blank nodes.
 * 
 * @author Konstantin Baierer
 *
 */
public enum SkolemizationMethod {
	/** Use a random UUID for skolemization */
	RANDOM_UUID,
	/** Use sequential numeric IDs for skolemization */
	SEQUENTIAL_ID,
	/** Use the rdfs:label of an object to name it */
	BY_RDFS_LABEL
}
