package eu.dm2e.ws;

public enum OmnomTestResources {
	ASCII_NOISE("/random/ascii_noise.txt")
	, DEMO_JOB("/job/demo_job.ttl")
	, DEMO_LOG("/job/log_entry.ttl")
	, DEMO_LOG_WITH_URI("/job/log_entry_uri.ttl")
	, DEMO_SERVICE_WORKING("/webserviceConfig/demo_config.ttl")
	, DEMO_SERVICE_NO_TOP_BLANK("/webserviceConfig/demo_config_no_top_blank.ttl")
	, DEMO_SERVICE_ILLEGAL_PARAMETER("/webserviceConfig/demo_config_illegal_sleeptime.ttl")
	, MINIMAL_FILE("/file/empty_file.ttl")
	, MINIMAL_FILE_WITH_URI("/file/uri_file.ttl")
	, ILLEGAL_EMPTY_FILE("/file/illegal_file.ttl")
	, XML_DTA_GRIMM("/provider-examples/dta/grimm_meistergesang_1811.TEI-P5.xml")
	, XSLT_KBA_BBAW_TO_EDM("/mappings/xslt/KBA_BBAW_TO_EDM.xsl")
	, TEI2EDM_20130129("/mappings/xslt-zip/TEI2EDM_xslt_20130129.zip")
	;
	
	final String path;
	OmnomTestResources(String path) { this.path = path; }
	public String getPath() { return path; }
}