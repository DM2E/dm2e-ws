package eu.dm2e.ws.tests.unit.api.pojos;

import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFId;
import eu.dm2e.grafeo.annotations.RDFProperty;

import java.util.HashSet;
import java.util.Set;

@RDFClass("omnom:SetPojoTest")
public class SetPojo {
	
	@RDFId
	private String idURI;
	
	@RDFProperty("omnom:some_number")
	private Set<Integer> literalSet = new HashSet<>();
	
	public String getIdURI() { return idURI; }
	public void setIdURI(String idURI) { this.idURI = idURI; }
	public Set<Integer> getLiteralSet() { return literalSet; }
	public void setLiteralSet(Set<Integer> literalSet) { this.literalSet = literalSet; }

}
