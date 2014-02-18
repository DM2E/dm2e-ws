package eu.dm2e.ws;

/**
 * Configuration Properties.
 * 
 * @author Konstantin Baierer
 */
public enum ConfigProp {

	  BASE_URI("dm2e.ws.base_uri")
	  
	// ENDPOINTS
	, ENDPOINT_QUERY("dm2e.ws.sparql_endpoint")
	, ENDPOINT_UPDATE("dm2e.ws.sparql_endpoint_statements")
	
	// CONFIG
	, CONFIG_BASEURI("dm2e.service.config.base_uri")
	
	// FILE
	, FILE_BASEURI("dm2e.service.file.base_uri")
	, FILE_STOREDIR("dm2e.service.file.store_directory")
	
	// JOB
	, JOB_BASEURI("dm2e.service.job.base_uri")
	
	// WORKFLOW
	, WORKFLOW_BASEURI("dm2e.service.workflow.base_uri")
	
	// PUBLISH
	, PUBLISH_BASEURI("dm2e.service.publish.base_uri")
	, PUBLISH_GRAPH_PREFIX("dm2e.service.publish.graph_prefix")
	
	// MINT
	, MINT_BASE_URI("dm2e.service.mint-file.base_uri")
	, MINT_REMOTE_BASE_URI("dm2e.service.mint-file.mint_base")
	, MINT_USERNAME("dm2e.service.mint-file.username")
	, MINT_PASSWORD("dm2e.service.mint-file.password")
	
	;
	  
//	public enum WORKFLOW {
//		BASE_URI("dm2e.service.workflow.base_uri")
//		;
//		private String propertiesName;
//		public String getPropertiesName() { return propertiesName; }
//		public void setPropertiesName(String propertiesName) { this.propertiesName = propertiesName; }
//
//		WORKFLOW_BASEURI(String propertiesName) {
//			this.setPropertiesName(propertiesName);
//		}
//	}

	private String propertiesName;
	public String getPropertiesName() { return propertiesName; }
	public void setPropertiesName(String propertiesName) { this.propertiesName = propertiesName; }
	
	ConfigProp(String propertiesName) {
		this.setPropertiesName(propertiesName);
	}

}
