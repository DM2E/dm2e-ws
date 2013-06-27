package eu.dm2e.ws.grafeo.test;

import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

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
		return ""+this.getSomeNumber();
	}
	
	@Override
	public boolean equals(Object that) {
		if (that == null) return false;
		if (that == this) return false;
		if (!(that instanceof IntegerPojo)) return false;
	    IntegerPojo thatIntegerPojo = (IntegerPojo) that;
	    if (this.hasId() && thatIntegerPojo.hasId()
	    		&&
    		! this.getId().equals(thatIntegerPojo.getId())) {
	    	return false;
	    }
	    return this.getSomeNumber() == thatIntegerPojo.getSomeNumber();
	}

	
}

