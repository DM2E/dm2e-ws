package eu.dm2e.ws;



//CHECKSTYLE.OFF: JavadocVariable

/**
 * Central list of RDF entities.
 *
 * <p>
 * Every vocabulary is represented by a static final class, which must define
 * a BASE constant (the base URI of the vocabulary) and constants for all entities,
 * (by convention prefixed with CLASS_ for classes and PROP_ for properties).
 * </p>
 *
 * @author Konstantin Baierer
 *
 */
public final class NS {

	/**
	 * Single place to collect all OmNom property and class names.
	 *
	 * @author Konstantin Baierer
	 *
	 */
	public static final class OMNOM {

		public static final String BASE = "http://onto.dm2e.eu/omnom/";

        public static final String CLASS_FILE                 = BASE + "File";
        public static final String CLASS_JOB                  = BASE + "Job";
        public static final String CLASS_LOG_ENTRY            = BASE + "LogEntry";
        public static final String CLASS_PARAMETER            = BASE + "Parameter";
        public static final String CLASS_PARAMETER_ASSIGNMENT = BASE + "ParameterAssignment";
        public static final String CLASS_PARAMETER_CONNECTOR  = BASE + "ParameterConnector";
        public static final String CLASS_WEBSERVICE           = BASE + "Webservice";
        public static final String CLASS_WEBSERVICE_CONFIG    = BASE + "WebserviceConfig";
        public static final String CLASS_WORKFLOW             = BASE + "Workflow";
        public static final String CLASS_WORKFLOW_JOB         = BASE + "WorkflowJob";
        public static final String CLASS_WORKFLOW_POSITION    = BASE + "WorkflowPosition";

		public static final String PROP_ASSIGNMENT          = BASE + "assignment";
		public static final String PROP_DEFAULT_VALUE       = BASE + "defaultValue";
		public static final String PROP_FILE_EDIT_URI       = BASE + "fileEditURI";
		public static final String PROP_FILE_LOCATION       = BASE + "internalFileLocation";
		public static final String PROP_FILE_RETRIEVAL_URI  = BASE + "fileRetrievalURI";
		public static final String PROP_FILE_STATUS         = BASE + "fileStatus";
		public static final String PROP_FILE_TYPE           = BASE + "fileType";
		public static final String PROP_FINISHED_JOB        = BASE + "finishedJobs";
		public static final String PROP_FINISHED_POSITION   = BASE + "finishedPosition";
		public static final String PROP_FOR_PARAM           = BASE + "forParam";
		public static final String PROP_FOR_SLOT            = BASE + "forSlot";
		public static final String PROP_FROM_PARAM          = BASE + "fromParam";
		public static final String PROP_FROM_POSITION       = BASE + "fromPosition";
		public static final String PROP_FROM_WORKFLOW       = BASE + "fromWorkflow";
		public static final String PROP_INPUT_PARAM         = BASE + "inputParam";
		public static final String PROP_IN_WORKFLOW         = BASE + "inWorkflow";
		public static final String PROP_IS_REQUIRED         = BASE + "isRequired";
		public static final String PROP_ITEM_LABEL_XPATH    = BASE + "itemLabelXPath";
		public static final String PROP_ITEM_ROOT_XPATH     = BASE + "itemRootXPath";
		public static final String PROP_JOB_STATUS          = BASE + "status";
		public static final String PROP_LOG_ENTRY           = BASE + "logEntries";
		public static final String PROP_LOG_LEVEL           = BASE + "hasLogLevel";
		public static final String PROP_LOG_MESSAGE         = BASE + "hasLogMessage";
		public static final String PROP_MD5                 = BASE + "md5";
		public static final String PROP_ORIGINAL_NAME       = BASE + "originalName";
		public static final String PROP_OUTPUT_PARAM        = BASE + "outputParam";
		public static final String PROP_PARAMETER_CONNECTOR = BASE + "parameterConnector";
		public static final String PROP_PARAMETER_TYPE      = BASE + "parameterType";
		public static final String PROP_PARAMETER_VALUE     = BASE + "parameterValue";
		public static final String PROP_WORKFLOW_POSITION   = BASE + "workflowPosition";
		public static final String PROP_TO_PARAM            = BASE + "toParam";
		public static final String PROP_TO_POSITION         = BASE + "toPosition";
		public static final String PROP_TO_WORKFLOW         = BASE + "toWorkflow";
        public static final String PROP_WEBSERVICE          = BASE + "webservice";
        public static final String PROP_WEBSERVICE_ID          = BASE + "implementationID";
        public static final String PROP_EXEC_WEBSERVICE          = BASE + "isExecutableAt";
        public static final String PROP_WEBSERVICE_CONFIG   = BASE + "webserviceConfig";
		public static final String PROP_WORKFLOW            = BASE + "workflow";

		public static final String PROP_FILE_SERVICE = BASE + "fileservice";
		public static final String PROP_FILE_OWNER = BASE + "fileOwner";
		public static final String PROP_RUNNING_JOB = BASE + "runningJob";
//		public static final String PROP_FAILED_JOB = BASE + "failedJob";

		public static final String PROP_EXECUTES_POSITION = BASE + "executesPosition";

		public static final String PROP_PREFERRED_THEME     = BASE + "preferredTheme";
		public static final String PROP_GLOBAL_USER_FILTER     = BASE + "globalUserFilter";
	}

	/**
	 * Resource types for files, used for content-negotiating webservices, validation...
	 */
	public static final class OMNOM_TYPES {

		public static final String BASE    = "http://onto.dm2e.eu/omnom-types/";
		public static final String XSLT    = BASE + "XSLT";
		public static final String TGZ     = BASE + "TGZ";
		public static final String TGZ_XML = BASE + "TGZ-XML";
		public static final String ZIP_XML = BASE + "ZIP-XML";
		public static final String XML     = BASE + "XML";
	}

	/**
	 * RDFS.
	 */
	public static final class RDFS {
		public static final String BASE = "http://www.w3.org/2000/01/rdf-schema#";
		public static final String PROP_LABEL = BASE + "label";
		public static final String PROP_COMMENT = BASE + "comment";
	}

	/**
	 * Collections Ontology.
	 */
	public static final class CO {

		public static final String BASE = "http://purl.org/co/";

		public static final String PROP_ITEM_CONTENT = BASE + "itemContent";
		public static final String PROP_SIZE		   = BASE + "size";
		public static final String PROP_INDEX        = BASE + "index";
		public static final String PROP_ITEM  	   = BASE + "item";
		public static final String PROP_FIRST_ITEM   = BASE + "firstItem";
		public static final String PROP_NEXT_ITEM   = BASE + "nextItem";
		public static final String PROP_LAST_ITEM   = BASE + "lastItem";

		public static final String CLASS_LIST = BASE + "List";
		public static final String CLASS_ITEM = BASE + "Item";
	}

	/**
	 * RDF.
	 */
	public static final class RDF {

		public static final String BASE      = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		public static final String PROP_TYPE = BASE + "type";
	}

	/** SKOS. */
	public static final class SKOS {

		public static final String BASE       = "http://www.w3.org/2004/02/skos/core#";
		public static final String PROP_LABEL = BASE + "label";
	}

	/**
	 * Dublin Core Elements.
	 */
	public static final class DC {

		public static final String BASE       = "http://purl.org/dc/elements/1.1/";
		public static final String PROP_TITLE = BASE + "title";
		public static final String PROP_DATE  = BASE + "date";

	}

	/**
	 * Dublin Core Terms.
	 */
	public static final class DCTERMS {

		public static final String BASE          = "http://purl.org/dc/terms/";
		public static final String PROP_FORMAT   = BASE + "format";
		public static final String PROP_CREATOR   = BASE + "creator";
		public static final String PROP_EXTENT   = BASE + "extent";
		public static final String PROP_MODIFIED = BASE + "modified";
		public static final String PROP_CREATED  = BASE + "created";

	}

	/**
	 * PROV ontology.
	 */
	public static final class PROV {

		public static final String BASE                   = "http://www.w3.org/ns/prov#";
		public static final String PROP_WAS_GENERATED_BY  = BASE + "wasGeneratedBy";
		public static final String PROP_SPECIALIZATION_OF = BASE + "specializationOf";
		public static final String PROP_WAS_REVISION_OF   = BASE + "wasRevisionOf";

	}

	/**
	 * XML Schema.
	 */
	public static final class XSD {

		public static final String BASE = "http://www.w3.org/2001/XMLSchema#";
		public static final String INT  = BASE + "int";
	}

	/**
	 * VOID dataset description.
	 */
	public static final class VOID {

		public static final String BASE          = "http://rdfs.org/ns/void#";
		public static final String CLASS_DATASET = BASE + "Dataset";
	}

	/**
	 * FOAF Friend of a Friend.
	 */
	public static final class FOAF {

		public static final String BASE         = "http://xmlns.com/foaf/0.1/";
		public static final String CLASS_PERSON = BASE + "Person";
		public static final String PROP_NAME    = BASE + "name";
	}

//	public static final String
////            NS_OMNOM = Config.getString("dm2e.ns.dm2e")
////			, RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
//			, DM2ELOG = "http://onto.dm2e.eu/logging#"
//			;
//	public static final String[] NAMESPACE_MAP;
//
//	static {
//		HashMap<String, String> map = new HashMap<>();
//		map.put("omnom", OMNOM.BASE);
//		map.put("skos", SKOS.BASE);
//		map.put("dc", DC.BASE);
//		map.put("dcterms", DCTERMS.BASE);
//		map.put("dct", DCTERMS.BASE);
//		map.put("prov", PROV.BASE);
//		map.put("rdf", RDF.BASE);
//		map.put("rdfs", RDFS.BASE);
//		map.put("co", CO.BASE);
//		NAMESPACE_MAP = (String[]) Arrays.asList(map).toArray();
//	}
}
