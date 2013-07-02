package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/"})
@RDFClass(NS.OMNOM.CLASS_WORKFLOW_POSITION)
public class WorkflowPositionPojo extends SerializablePojo<WorkflowPositionPojo> implements IValidatable{
	
	@Override
	public void validate() throws Exception {
		if (null == workflow)
			throw new AssertionError(this + " has no workflow");
		if (null == webserviceConfig)
			throw new AssertionError(this + " has no webserviceConfig");
		else
			webserviceConfig.validate();
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
	 *  Configuration of the workflow in this position as provided
	 */
	@RDFProperty(NS.OMNOM.PROP_WEBSERVICE_CONFIG)
	private WebserviceConfigPojo webserviceConfig;
	public WebserviceConfigPojo getWebserviceConfig() { return this.webserviceConfig; }
	public void setWebserviceConfig(WebserviceConfigPojo webServiceConfig) { this.webserviceConfig = webServiceConfig; }

	/**
	 * The workflow this position belongs to
	 */
	@RDFProperty(NS.OMNOM.PROP_IN_WORKFLOW)
	private WorkflowPojo workflow;
	public WorkflowPojo getWorkflow() { return workflow; }
	public void setWorkflow(WorkflowPojo workflow) { this.workflow = workflow; }

}
