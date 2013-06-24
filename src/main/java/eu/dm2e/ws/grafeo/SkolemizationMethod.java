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
	RANDOM_UUID,
	SEQUENTIAL_ID,
	BY_RDFS_LABEL
}
