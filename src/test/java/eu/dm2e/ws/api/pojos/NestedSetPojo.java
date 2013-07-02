package eu.dm2e.ws.api.pojos;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.api.AbstractPersistentPojo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass("omnom:NestedSet")
public class NestedSetPojo extends AbstractPersistentPojo<NestedSetPojo> {
	
	@RDFProperty("omnom:ze_numbaz")
	private Set<IntegerPojo> numbers = new HashSet<>();
	public Set<IntegerPojo> getNumbers() { return numbers; }
	public void setNumbers(Set<IntegerPojo> numbers) { this.numbers = numbers; }
	
}
