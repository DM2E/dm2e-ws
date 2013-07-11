package eu.dm2e.ws;

public enum ConfigProp {

	  ENDPOINT_QUERY("dm2e.ws.sparql_endpoint")
	, ENDPOINT_UPDATE("dm2e.ws.sparql_endpoint_statements")
	, CONFIG_BASEURI("dm2e.service.config.base_uri")
	, FILE_BASEURI("dm2e.service.file.base_uri")
	, JOB_BASEURI("dm2e.service.job.base_uri")

	;

	private String propertiesName;
	public String getPropertiesName() { return propertiesName; }
	public void setPropertiesName(String propertiesName) { this.propertiesName = propertiesName; }
	
	ConfigProp(String propertiesName) {
		this.setPropertiesName(propertiesName);
	}

}
