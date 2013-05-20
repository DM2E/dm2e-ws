package eu.dm2e.ws.grafeo.test;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass("omnom:SetPojoTest")
public class SetPojo {
	
	@RDFId
	private String idURI;
	
	@RDFProperty("omnom:some_number")
	private Set<Integer> literalSet = new HashSet<Integer>();
	
	public String getIdURI() { return idURI; }
	public void setIdURI(String idURI) { this.idURI = idURI; }
	public Set<Integer> getLiteralSet() { return literalSet; }
	public void setLiteralSet(Set<Integer> literalSet) { this.literalSet = literalSet; }

}
