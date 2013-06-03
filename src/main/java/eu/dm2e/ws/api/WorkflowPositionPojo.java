package eu.dm2e.ws.api;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass("omnom:WorkflowPosition")
public class WorkflowPositionPojo {
	
	@RDFProperty("omnom:webservice")
	private WebservicePojo webService;
	
	@RDFProperty("omnom:belongsToWorkflow")
	private WorkflowPojo workflow;
	

    /*********************
     * CONSTRUCTORS
     ********************/
	public WorkflowPositionPojo() {
		// to make BeanUtils happy
	}
    

    
    /*********************
     * GETTERS/SETTERS
     ********************/

	public WebservicePojo getWebService() { return webService; }
	public void setWebService(WebservicePojo webService) { this.webService = webService; }

	public WorkflowPojo getWorkflow() { return workflow; }
	public void setWorkflow(WorkflowPojo workflow) { this.workflow = workflow; }

}
