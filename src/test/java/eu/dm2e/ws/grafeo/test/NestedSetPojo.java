package eu.dm2e.ws.grafeo.test;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.api.AbstractPersistentPojo;
import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass("omnom:NestedSet")
public class NestedSetPojo extends AbstractPersistentPojo<NestedSetPojo> {
	
	@RDFProperty("omnom:ze_numbaz")
	private Set<IntegerPojo> numbers = new HashSet<>();
	public Set<IntegerPojo> getNumbers() { return numbers; }
	public void setNumbers(Set<IntegerPojo> numbers) { this.numbers = numbers; }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result + ((numbers == null) ? 0 : numbers.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof NestedSetPojo)) return false;
		NestedSetPojo other = (NestedSetPojo) obj;
		if (numbers == null) {
			if (other.numbers != null) return false;
		} else if (!numbers.equals(other.numbers)) return false;
		return true;
	}
	
}
