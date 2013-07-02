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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result + ((longList == null) ? 0 : longList.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof LiteralListPojo)) return false;
		LiteralListPojo other = (LiteralListPojo) obj;
		if (longList == null) {
			if (other.longList != null) return false;
		} else if (!longList.equals(other.longList)) return false;
		return true;
	}

}
