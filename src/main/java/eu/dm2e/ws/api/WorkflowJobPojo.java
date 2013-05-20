package eu.dm2e.ws.api;

import java.util.HashSet;
import java.util.Set;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@RDFClass("omnom:WorkflowJob")
public class WorkflowJobPojo extends JobPojo {
	
	@RDFProperty("omnom:hasSlotAssignment")
	private Set<ParameterSlotAssignmentPojo> slotAssignments = new HashSet<ParameterSlotAssignmentPojo>();
	
    @RDFProperty("omnom:hasWebService")
    private WebservicePojo webService;

	public Set<ParameterSlotAssignmentPojo> getSlotAssignments() { return slotAssignments; }
	public void setSlotAssignments(Set<ParameterSlotAssignmentPojo> slotAssignments) { this.slotAssignments = slotAssignments; }

}
