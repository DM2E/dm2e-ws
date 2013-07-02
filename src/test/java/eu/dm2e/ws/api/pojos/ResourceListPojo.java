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
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result
				+ ((integerResourceList == null) ? 0 : integerResourceList.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof ResourceListPojo)) return false;
		ResourceListPojo other = (ResourceListPojo) obj;
		if (integerResourceList == null) {
			if (other.integerResourceList != null) return false;
		} else if (!integerResourceList.equals(other.integerResourceList)) return false;
		return true;
	}

}
