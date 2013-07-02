package eu.dm2e.ws.api.pojos;

import java.util.ArrayList;
import java.util.List;

import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass("omnom:LiteralListClass")
public class LiteralListPojo extends SerializablePojo<LiteralListPojo>{
	
	@RDFProperty("omnom:someLiteral")
	private List<Long> longList = new ArrayList<>();
	public List<Long> getLongList() { return longList; }
	public void setLongList(List<Long> longList) { this.longList = longList; }
	

}
