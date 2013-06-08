package eu.dm2e.ws;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.ParameterSlotPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WorkflowConfigPojo;
import eu.dm2e.ws.api.WorkflowJobPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;



public final class NS {
	
	/**
	 * Single place to collect all OmNom property and class names. 
	 * 
	 * @author Konstantin Baierer
	 *
	 */
	public static final class OMNOM {
		/**
		 * Abstract Jobs
		 */
		public static final String PROP_LOG_ENTRY = "omnom:hasLogEntry";
		public static final String PROP_JOB_STATUS = "omnom:status";
		
		public static final String PROP_OUTPUT_ASSIGNMENT = "omnom:assignment";
		public static final String PROP_WEBSERVICE = "omnom:webservice";
		/**
		 * 
		 * @see JobPojo
		 * @see WebserviceConfigPojo
		 */
		public static final String PROP_WEBSERVICE_CONFIG = "omnom:webserviceConfig";
		
		/**
		 * 
		 * @see ParameterSlotPojo
		 * @see ParameterAssignmentPojo
		 */
		public static final String PROP_FOR_PARAM = "omnom:forParam";
		/**
		 * 
		 * @link WorkflowConfigPojo
		 */
		public static final String PROP_HAS_POSITION = "omnom:hasPosition";
		/**
		 * 
		 * @see WorkflowConfigPojo#getParameterSlots()
		 */
		public static final String PROP_PARAMETER_SLOT = "omnom:parameterSlot";
		/**
		 * 
		 * @see WorkflowJobPojo#getSlotAssignments()
		 */
		public static final String PROP_SLOT_ASSIGNMENT = "omnom:slotAssignment";
		
		/**
		 * 
		 * @see WorkflowPositionPojo
		 */
		public static final String PROP_IN_WORKFLOW = "omnom:inWorkflow";
		
		/**
		 * 
		 * @see ParameterAssignmentPojo
		 */
		public static final String PROP_PARAMETER_VALUE = "omnom:parameterValue";
		
		/**
		 * 
		 * @see WorkflowJobPojo
		 */
		public static final String CLASS_WORKFLOW_JOB = "omnom:WorkflowJob";
		
		/**
		 * 
		 * @see WorkflowConfigPojo
		 */
		public static final String CLASS_WORKFLOW = "omnom:Workflow";
		
		/**
		 * 
		 * @see WorkflowJobPojo#getWorkflowConfig()
		 */
		public static final String PROP_WORKFLOW_CONFIG = "omnom:workflowConfig";
		
		public static final String CLASS_WEBSERVICE = "omnom:Webservice";
		
		public static final String PROP_INPUT_PARAM = "omnom:inputParam";
		public static final String PROP_OUTPUT_PARAM = "omnom:outputParam";
		
		public static final String PROP_FROM_POSITION = "omnom:forPosition";
		public static final String PROP_TO_POSITION = "omnom:toPosition";
		
		public static final String PROP_FROM_PARAM = "omnom:fromParam";
		public static final String PROP_TO_PARAM = "omnom:toParam";
		public static final String PROP_FROM_STRING = "omnom:fromString";
	}
	
	public static final class RDFS {
		
		public static final String PROP_LABEL = "rdfs:label";
		
	}
	
	public static final class CO {
		
		public static final String PROP_FIRST = "co:first";
		public static final String PROP_LAST = "co:last";
		public static final String PROP_ITEM_CONTENT = "co:itemContent";
		public static final String CLASS_LIST = "co:List";
		public static final String PROP_SIZE = "co:size";
		public static final String PROP_INDEX = "co:index";
		public static final String PROP_NEXT = "co:next";
		
	}
	
	public static final class RDF {
		
		public static final String BASE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		
		public static final String PROP_TYPE = BASE + "type";
	}
		
	
	public static final String
            NS_OMNOM = Config.getString("dm2e.ns.dm2e")
//			, RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			, DM2ELOG = "http://onto.dm2e.eu/logging#"
			, ENDPOINT = Config.getString("dm2e.ws.sparql_endpoint")
			, ENDPOINT_STATEMENTS = Config.getString("dm2e.ws.sparql_endpoint_statements")
			;
}
