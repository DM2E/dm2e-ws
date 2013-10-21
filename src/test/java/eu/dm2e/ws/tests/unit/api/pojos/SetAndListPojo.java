package eu.dm2e.ws.tests.unit.api.pojos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.dm2e.grafeo.gom.SerializablePojo;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;

@RDFClass("omnom:SetAndList")
public class SetAndListPojo extends SerializablePojo<SetAndListPojo>{
	
	@RDFProperty("omnom:someList")
	private List<IntegerPojo> list = new ArrayList<>();
	public List<IntegerPojo> getList() { return list; }
	public void setList(List<IntegerPojo> list) { this.list = list; }
	
	@RDFProperty("omnom:someSet")
	private Set<IntegerPojo> set = new HashSet<>();
	public Set<IntegerPojo> getSet() { return set; }
	public void setSet(Set<IntegerPojo> set) { this.set = set; }

}
