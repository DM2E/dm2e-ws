package eu.dm2e.ws.grafeo.gom;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.dm2e.ws.api.AbstractPersistentPojo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.test.IntegerPojo;

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
	
}
