package eu.dm2e.ws.api;

/**
 * Interface for classes that support validation
 *  
 */
public interface IValidatable {

	/**
	 * Validate a Pojo and return a validation report on it.
	 * @return the {@link ValidationReport} for this Pojo
	 * @throws Exception
	 */
	public abstract ValidationReport validate() throws Exception;

}
