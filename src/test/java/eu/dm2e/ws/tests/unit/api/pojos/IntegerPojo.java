package eu.dm2e.ws.tests.unit.api.pojos;

import eu.dm2e.grafeo.gom.SerializablePojo;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;

@RDFClass("omnom:IntegerPojo")
public class IntegerPojo extends SerializablePojo<IntegerPojo> {
	
	public static final String CLASS_NAME = "omnom:IntegerPojo";
	public static final String PROP_SOME_NUMBER = "omnom:some_number";
	
	@RDFProperty(PROP_SOME_NUMBER)
	private int someNumber = 5;
	public int getSomeNumber() { return someNumber; }
	public void setSomeNumber(int some_number) { this.someNumber = some_number; }
	
	public IntegerPojo() { }
	
	public IntegerPojo(String id, int num) {
		this.setId(id);
		this.someNumber = num;
	}
	public IntegerPojo(int num) {
		this.someNumber = num;
	}
	
	@Override
	public String toString() {
//		return super.toString();
		return "IntegerPojo#"+this.getSomeNumber();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + someNumber;
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof IntegerPojo)) return false;
		IntegerPojo other = (IntegerPojo) obj;
		if (someNumber != other.someNumber) return false;
		return true;
	}
	
}

