package eu.dm2e.ws.api.pojos;

import java.util.ArrayList;
import java.util.List;

import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass("omnom:SomeList")
public class ResourceListPojo extends SerializablePojo<ResourceListPojo> {
	
	@RDFProperty("omnom:some_number")
	private List<IntegerPojo> integerResourceList = new ArrayList<>();
	
	public List<IntegerPojo> getIntegerResourceList() { return integerResourceList; }
	public void setIntegerResourceList(List<IntegerPojo> integerResourceList) { this.integerResourceList = integerResourceList; }

}
