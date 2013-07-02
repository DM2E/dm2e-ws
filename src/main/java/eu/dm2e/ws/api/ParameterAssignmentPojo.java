package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;


@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass(NS.OMNOM.CLASS_PARAMETER_ASSIGNMENT)
public class ParameterAssignmentPojo  extends AbstractPersistentPojo<ParameterAssignmentPojo> {

	/******************
	 * GETTERS/SETTERS
	 *****************/
	
	@RDFProperty(NS.OMNOM.PROP_FOR_PARAM)
	private ParameterPojo forParam;
	public ParameterPojo getForParam() { return forParam; }
	public void setForParam(ParameterPojo forParam) { this.forParam = forParam; }
	public boolean hasForParam() { return this.forParam != null; }
	
	@RDFProperty(NS.OMNOM.PROP_PARAMETER_VALUE)
	private String parameterValue;
	public String getParameterValue() { return parameterValue; }
	public void setParameterValue(String parameterValue) { this.parameterValue = parameterValue; }
	public boolean hasParameterValue() { return this.parameterValue != null; }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result + ((forParam == null) ? 0 : forParam.hashCode());
		result = prime * result + ((parameterValue == null) ? 0 : parameterValue.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof ParameterAssignmentPojo)) return false;
		ParameterAssignmentPojo other = (ParameterAssignmentPojo) obj;
		if (forParam == null) {
			if (other.forParam != null) return false;
		} else if (!forParam.equals(other.forParam)) return false;
		if (parameterValue == null) {
			if (other.parameterValue != null) return false;
		} else if (!parameterValue.equals(other.parameterValue)) return false;
		return true;
	}
	

}