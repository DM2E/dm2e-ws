package eu.dm2e.ws.api;

import eu.dm2e.ws.grafeo.GValue;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;


@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:ParameterAssignment")
public class ParameterAssignmentPojo {

	@RDFId
	private String id;
	
	@RDFProperty("omnom:forParam")
	private ParameterPojo forParam;
	
	// TODO this is a GValue because a parameter can be a literal or a link
	@RDFProperty("omnom:parameterValue")
	private GValue parameterValue;
	
	/******************
	 * GETTERS/SETTERS
	 *****************/

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public ParameterPojo getForParam() { return forParam; }
	public void setForParam(ParameterPojo forParam) { this.forParam = forParam; }
	
	public GValue getParameterValue() { return parameterValue; }
	public void setParameterValue(GValue parameterValue) { this.parameterValue = parameterValue; }
	public void setParameterValue(String parameterValue) { 
		GrafeoImpl g = new GrafeoImpl();
		this.parameterValue = g.literal(parameterValue); 
	}

}