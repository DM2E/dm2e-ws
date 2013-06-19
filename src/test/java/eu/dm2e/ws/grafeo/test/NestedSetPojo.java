package eu.dm2e.ws.grafeo.test;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass("omnom:NestedSet")
public class NestedSetPojo extends SerializablePojo<NestedSetPojo> {
	
	@RDFProperty("omnom:ze_numbaz")
	private Set<IntegerPojo> numbers = new HashSet<>();
	public Set<IntegerPojo> getNumbers() { return numbers; }
	public void setNumbers(Set<IntegerPojo> numbers) { this.numbers = numbers; }
	
}
