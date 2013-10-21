package eu.dm2e.ws.api;

import eu.dm2e.grafeo.gom.SerializablePojo;
import eu.dm2e.ws.NS;
import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;

/**
 * Pojo representing an instance of a webservice within a workflow
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass(NS.OMNOM.CLASS_WORKFLOW_POSITION)
public class WorkflowPositionPojo extends SerializablePojo<WorkflowPositionPojo> implements IValidatable{
	
	@Override
	public void validate() throws Exception {
		if (null == workflow)
			throw new AssertionError(this + " has no workflow");
		if (null == webservice)
			throw new AssertionError(this + " has no webservice");
//		else
//			webservice.validate();
	}

    /*********************
     * CONSTRUCTORS
     ********************/
	public WorkflowPositionPojo() {
		// to make BeanUtils happy
	}
    

    
    /*********************
     * GETTERS/SETTERS
     ********************/

	/**
	 * TODO this shoudl be replace with a web service
	 *  Configuration of the workflow in this position as provided
	 */
	@RDFProperty( value = NS.OMNOM.PROP_WEBSERVICE, serializeAsURI = true)
	private WebservicePojo webservice;
	public WebservicePojo getWebservice() { return this.webservice; }
	public void setWebservice(WebservicePojo webService) { this.webservice = webService; }

	/**
	 * The workflow this position belongs to
	 */
	@RDFProperty( value = NS.OMNOM.PROP_IN_WORKFLOW, serializeAsURI = true)
	private WorkflowPojo workflow;
	public WorkflowPojo getWorkflow() { return workflow; }
	public void setWorkflow(WorkflowPojo workflow) { this.workflow = workflow; }

}
