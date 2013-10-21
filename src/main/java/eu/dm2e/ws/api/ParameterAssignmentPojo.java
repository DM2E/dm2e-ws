package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;

/** Pojo for a parameter assignment */
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

}
