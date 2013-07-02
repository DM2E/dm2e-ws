package eu.dm2e.ws.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.test.IntegerPojo;

@RDFClass("omnom:SetAndList")
public class SetAndList extends SerializablePojo<SetAndList>{
	
	@RDFProperty("omnom:someList")
	private List<IntegerPojo> list = new ArrayList<>();
	public List<IntegerPojo> getList() { return list; }
	public void setList(List<IntegerPojo> list) { this.list = list; }
	
	@RDFProperty("omnom:someSet")
	private Set<IntegerPojo> set = new HashSet<>();
	public Set<IntegerPojo> getSet() { return set; }
	public void setSet(Set<IntegerPojo> set) { this.set = set; }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		result = prime * result + ((set == null) ? 0 : set.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof SetAndList)) return false;
		SetAndList other = (SetAndList) obj;
		if (list == null) {
			if (other.list != null) return false;
		} else if (!list.equals(other.list)) return false;
		if (set == null) {
			if (other.set != null) return false;
		} else if (!set.equals(other.set)) return false;
		return true;
	}

}
