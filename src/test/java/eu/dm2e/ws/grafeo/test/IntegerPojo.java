package eu.dm2e.ws.grafeo.test;

import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass("omnom:IntegerPojo")
public class IntegerPojo {
	
	@RDFId
	private String idURI;
	
	@RDFProperty("omnom:some_number")
	private int some_number = 5;
	
	public IntegerPojo() {
		
	}
	
	public IntegerPojo(String id, int num) {
		this.idURI = id;
		this.some_number = num;
	}

	public String getIdURI() {
		return idURI;
	}

	public void setIdURI(String idURI) {
		this.idURI = idURI;
	}

	public int getSome_number() {
		return some_number;
	}

	public void setSome_number(int some_number) {
		this.some_number = some_number;
	}
	
}
