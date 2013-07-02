package eu.dm2e.ws.api.pojos;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.dm2e.ws.api.AbstractPersistentPojo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass("omnom:UriResourceThing")
public class UriResourcePojo extends AbstractPersistentPojo<UriResourcePojo> {
	
	public static final String CLASS_NAME = "omnom:UriResourceThing";
	public static final String PROP_POJO_RESOURCE = "omnom:pojoResource";
	public static final String PROP_URI_RESOURCE = "omnom:uriResource";
	public static final String PROP_URI_RESOURCE_SET = "omnom:uriResourceSet";
	public static final String PROP_URI_RESOURCE_LIST = "omnom:uriResourceLIST";
	
	@RDFProperty(PROP_POJO_RESOURCE)
	private IntegerPojo pojoResource;
	public IntegerPojo getPojoResource() { return pojoResource; }
	public void setPojoResource(IntegerPojo pojoResource) { this.pojoResource = pojoResource; }

	@RDFProperty(PROP_URI_RESOURCE)
	private URI uriResource;
	public URI getUriResource() { return uriResource; }
	public void setUriResource(URI uriResource) { this.uriResource = uriResource; }

	@RDFProperty(PROP_URI_RESOURCE_SET)
	private Set<URI> uriResourceSet = new HashSet<>();
	public Set<URI> getUriResourceSet() { return uriResourceSet; }
	public void setUriResourceSet(Set<URI> uriResourceSet) { this.uriResourceSet = uriResourceSet; }

	@RDFProperty(PROP_URI_RESOURCE_LIST)
	private List<URI> uriResourceList = new ArrayList<>();
	public List<URI> getUriResourceList() { return uriResourceList; }
	public void setUriResourceList(List<URI> uriResourceList) { this.uriResourceList = uriResourceList; }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result + ((pojoResource == null) ? 0 : pojoResource.hashCode());
		result = prime * result + ((uriResource == null) ? 0 : uriResource.hashCode());
		result = prime * result + ((uriResourceList == null) ? 0 : uriResourceList.hashCode());
		result = prime * result + ((uriResourceSet == null) ? 0 : uriResourceSet.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof UriResourcePojo)) return false;
		UriResourcePojo other = (UriResourcePojo) obj;
		if (pojoResource == null) {
			if (other.pojoResource != null) return false;
		} else if (!pojoResource.equals(other.pojoResource)) return false;
		if (uriResource == null) {
			if (other.uriResource != null) return false;
		} else if (!uriResource.equals(other.uriResource)) return false;
		if (uriResourceList == null) {
			if (other.uriResourceList != null) return false;
		} else if (!uriResourceList.equals(other.uriResourceList)) return false;
		if (uriResourceSet == null) {
			if (other.uriResourceSet != null) return false;
		} else if (!uriResourceSet.equals(other.uriResourceSet)) return false;
		return true;
	}
	
}
