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
	, TEI2DM2E_20130605("/mappings/xslt-zip/TEI2DM2E_xslt_20130605.zip")
	, TEMPLATE_BLANK_XSLTZIP("/webserviceConfig/xslt_zip_config.ttl.mustache")
    , PUBLISH_RDF("/publish/to-publish-test1.rdf")
    , INGESTION_XML("/workflow/Ms-155_OA.xml")
    , INGESTION_XSL("/workflow/2013-05-21-JI.xsl")
    , METS2EDM("/mappings/xslt/METS2DM2Ev2.xsl")
    , METS_EXAMPLES("/provider-examples/ffm/msma-1-10.xml")
    , MINT_MAPPING_LIST_JSON("/mint-api/mappings-list.json")
    , MINT_MAPPING_SINGLE_JSON("/mint-api/single-mapping.json")
    , MINT_MAPPING_SINGLE_NO_UPLOAD_JSON("/mint-api/single-mapping-no-uploadid.json")
    , MINT_UPLOAD_LIST_JSON("/mint-api/dataset-list.json")
    , MINT_DATASET_SINGLE_DATAUPLOAD_JSON_XML("/mint-api/single-dataupload-xml.json")
    , MINT_DATASET_SINGLE_DATAUPLOAD_JSON_ZIPXML("/mint-api/single-dataupload-zipxml.json")
    , MINT_DATASET_SINGLE_DATAUPLOAD_JSON_TGZXML("/mint-api/single-dataupload-tgzxml.json")
    ;

	final String path;
	OmnomTestResources(String path) { this.path = path; }
	public String getPath() { return path; }
}
