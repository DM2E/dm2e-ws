package eu.dm2e.ws;


public final class NS {
	
	public static final class OMNOM {
		public static final String PROP_LOG_ENTRY = "omnom:hasLogEntry";
		public static final String PROP_JOB_STATUS = "omnom:status";
		public static final String PROP_OUTPUT_ASSIGNMENT = "omnom:assignment";
		public static final String PROP_WEBSERVICE = "omnom:webservice";
		public static final String PROP_WEBSERVICE_CONFIG = "omnom:webserviceConfig";
		
		public static final String PROP_HAS_POSITION = "omnom:hasPosition";
		public static final String PROP_PARAMETER_SLOT = "omnom:parameterSlot";
		public static final String PROP_SLOT_ASSIGNMENT = "omnom:slotAssignment";
		
		public static final String CLASS_WORKFLOW_JOB = "omnom:WorkflowJob";
	}
	
	public static final String
            NS_OMNOM = Config.getString("dm2e.ns.dm2e")
			, RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			, DM2ELOG = "http://onto.dm2e.eu/logging#"
			, ENDPOINT = Config.getString("dm2e.ws.sparql_endpoint")
			, ENDPOINT_STATEMENTS = Config.getString("dm2e.ws.sparql_endpoint_statements")
			;
}
