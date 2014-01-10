package eu.dm2e.ws;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;





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

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface OWLAnnotation {
		String description();
		String owlType() default NS.OWL.CLASS; // can be NS.OWL.DATATYPE_PROPERTY, NS.OWL.OBJECT_PROPERTY or NS.OWL.CLASS
		String domain() default "";
		String range() default "";
		String label() default "";
		boolean deprecated() default false;
	}

	/**
	 * Single place to collect all OmNom property and class names.
	 *
	 * @author Konstantin Baierer
	 *
	 */
	public static final class OMNOM {
		
		public static final String BASE = "http://onto.dm2e.eu/omnom/";

		@OWLAnnotation(description="A class representing an uploaded file.")
        public static final String CLASS_FILE                 = BASE + "File";

		@OWLAnnotation(description="A class representing a job.")
        public static final String CLASS_JOB                  = BASE + "Job";

		@OWLAnnotation(description="A class representing single entry in the log.")
        public static final String CLASS_LOG_ENTRY            = BASE + "LogEntry";

		@OWLAnnotation(description="A class representing a web service parameter.")
        public static final String CLASS_PARAMETER            = BASE + "Parameter";

		@OWLAnnotation(description="TODO")
        public static final String CLASS_PARAMETER_ASSIGNMENT = BASE + "ParameterAssignment";

		@OWLAnnotation(description="TODO")
        public static final String CLASS_PARAMETER_CONNECTOR  = BASE + "ParameterConnector";

		@OWLAnnotation(description="TODO")
        public static final String CLASS_WEBSERVICE           = BASE + "Webservice";

		@OWLAnnotation(description="TODO")
        public static final String CLASS_WEBSERVICE_CONFIG    = BASE + "WebserviceConfig";

		@OWLAnnotation(description="TODO")
        public static final String CLASS_WORKFLOW             = BASE + "Workflow";
        
		@OWLAnnotation( description = "A job representing the execution of a Workflow with a specific configuration.",
				deprecated = true)
		public static final String CLASS_WORKFLOW_JOB = BASE + "WorkflowJob";
        public static final String CLASS_WORKFLOW_POSITION    = BASE + "WorkflowPosition";

		@OWLAnnotation( description = "Links a ParameterAssignment to a WebserviceConfig",
				owlType = NS.OWL.OBJECT_PROPERTY,
				range = NS.OMNOM.CLASS_PARAMETER_ASSIGNMENT,
				domain = NS.OMNOM.CLASS_WEBSERVICE_CONFIG)
		public static final String PROP_ASSIGNMENT          = BASE + "assignment";

		@OWLAnnotation( description = "Default Value for a parameter",
				owlType = NS.OWL.DATATYPE_PROPERTY,
				label = "Default Value",
				range = NS.XSD.STRING)
		public static final String PROP_DEFAULT_VALUE       = BASE + "defaultValue";
		
		@OWLAnnotation(description = "URI that points to a web location where a user can edit this resource.",
				owlType = NS.OWL.DATATYPE_PROPERTY,
				range = NS.XSD.ANY_URI)
		public static final String PROP_FILE_EDIT_URI       = BASE + "fileEditURI";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FILE_LOCATION       = BASE + "internalFileLocation";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FILE_RETRIEVAL_URI  = BASE + "fileRetrievalURI";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FILE_STATUS         = BASE + "fileStatus";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FILE_TYPE           = BASE + "fileType";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FINISHED_JOB        = BASE + "finishedJobs";
//		public static final String PROP_FINISHED_POSITION   = BASE + "finishedPositions";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FOR_PARAM           = BASE + "forParam";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FOR_SLOT            = BASE + "forSlot";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FROM_PARAM          = BASE + "fromParam";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FROM_POSITION       = BASE + "fromPosition";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FROM_WORKFLOW       = BASE + "fromWorkflow";

		@OWLAnnotation(description="TODO")
		public static final String PROP_INPUT_PARAM         = BASE + "inputParam";

		@OWLAnnotation(description="TODO")
		public static final String PROP_IN_WORKFLOW         = BASE + "inWorkflow";

		@OWLAnnotation(description="TODO")
        public static final String PROP_HAS_ITERATIONS      = BASE + "hasIterations";

		@OWLAnnotation(description="TODO")
        public static final String PROP_IS_REQUIRED         = BASE + "isRequired";

		@OWLAnnotation(description="TODO")
        public static final String PROP_ITEM_LABEL_XPATH    = BASE + "itemLabelXPath";

		@OWLAnnotation(description="TODO")
		public static final String PROP_ITEM_ROOT_XPATH     = BASE + "itemRootXPath";

		@OWLAnnotation(description="TODO")
        public static final String PROP_JOB_TEMPID          = BASE + "temporaryID";

		@OWLAnnotation(description="TODO")
        public static final String PROP_JOB_STATUS          = BASE + "status";

		@OWLAnnotation(description="TODO")
        public static final String PROP_JOB_STARTED         = BASE + "startedJobs";

		@OWLAnnotation(description="TODO")
        public static final String PROP_JOB_PARENT          = BASE + "parentJob";

		@OWLAnnotation(description="TODO")
        public static final String PROP_JOB_LATEST_RESULT   = BASE + "latestResult";

		@OWLAnnotation(description="TODO")
        public static final String PROP_LOG_ENTRY           = BASE + "logEntries";

		@OWLAnnotation(description="TODO")
		public static final String PROP_LOG_LEVEL           = BASE + "hasLogLevel";

		@OWLAnnotation(description="TODO")
		public static final String PROP_LOG_MESSAGE         = BASE + "hasLogMessage";

		@OWLAnnotation(description="TODO")
		public static final String PROP_MD5                 = BASE + "md5";

		@OWLAnnotation(description="TODO")
		public static final String PROP_ORIGINAL_NAME       = BASE + "originalName";

		@OWLAnnotation(description="TODO")
		public static final String PROP_OUTPUT_PARAM        = BASE + "outputParam";

		@OWLAnnotation(description="TODO")
		public static final String PROP_PARAMETER_CONNECTOR = BASE + "parameterConnector";

		@OWLAnnotation(description="TODO")
		public static final String PROP_PARAMETER_TYPE      = BASE + "parameterType";

		@OWLAnnotation(description="TODO")
        public static final String PROP_PARAMETER_VALUE     = BASE + "parameterValue";

		@OWLAnnotation(description="TODO")
        public static final String PROP_PARAMETER_SERIAL     = BASE + "parameterSerial";

		@OWLAnnotation(description="TODO")
        public static final String PROP_WORKFLOW_POSITION   = BASE + "workflowPosition";

		@OWLAnnotation(description="TODO")
		public static final String PROP_TO_PARAM            = BASE + "toParam";

		@OWLAnnotation(description="TODO")
		public static final String PROP_TO_POSITION         = BASE + "toPosition";

		@OWLAnnotation(description="TODO")
		public static final String PROP_TO_WORKFLOW         = BASE + "toWorkflow";

		@OWLAnnotation(description="TODO")
        public static final String PROP_WEBSERVICE          = BASE + "webservice";

		@OWLAnnotation(description="TODO")
        public static final String PROP_WEBSERVICE_ID          = BASE + "implementationID";

		@OWLAnnotation(description="TODO")
        public static final String PROP_EXEC_WEBSERVICE          = BASE + "isExecutableAt";

		@OWLAnnotation(description="TODO")
        public static final String PROP_WEBSERVICE_CONFIG   = BASE + "webserviceConfig";

		@OWLAnnotation(description="TODO")
		public static final String PROP_WORKFLOW            = BASE + "workflow";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FILE_SERVICE = BASE + "fileservice";

		@OWLAnnotation(description="TODO")
		public static final String PROP_FILE_OWNER = BASE + "fileOwner";

		@OWLAnnotation(description="TODO")
		public static final String PROP_RUNNING_JOB = BASE + "runningJob";

//		public static final String PROP_FAILED_JOB = BASE + "failedJob";

		@OWLAnnotation(description="TODO")
		public static final String PROP_EXECUTES_POSITION = BASE + "executesPosition";

		@OWLAnnotation(description="TODO")
		public static final String PROP_PREFERRED_THEME     = BASE + "preferredTheme";

		@OWLAnnotation(description="TODO")
		public static final String PROP_GLOBAL_USER_FILTER     = BASE + "globalUserFilter";

		@OWLAnnotation(description="TODO")
		public static final String PROP_POSITIONS_TO_RUN = BASE + "positionsToRun";
	}

	/**
	 * Resource types for files, used for content-negotiating webservices, validation...
	 */
	public static final class OMNOM_TYPES {

		public static final String BASE    = "http://onto.dm2e.eu/omnom-types/";
		public static final String TGZ     = BASE + "TGZ";
		public static final String TGZ_XML = BASE + "TGZ-XML";
		public static final String XML     = BASE + "XML";
		public static final String XSLT    = BASE + "XSLT";
		public static final String ZIP_XML = BASE + "ZIP-XML";
		public static final String ZIP_XSLT= BASE + "ZIP-XSLT";
		public static final String UNKNOWN = BASE + "UNKNOWN";
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
		public static final String PROP_PREF_LABEL = BASE + "prefLabel";
	}

	/**
	 * Dublin Core Elements.
	 */
	public static final class DC {

		public static final String BASE       = "http://purl.org/dc/elements/1.1/";
		public static final String PROP_TITLE = BASE + "title";
		public static final String PROP_DATE  = BASE + "date";
		public static final String PROP_TYPE = BASE + "type";
		public static final String PROP_IDENTIFIER = BASE + "identifier";
		public static final String PROP_SUBJECT = BASE + "subject";
		public static final String PROP_PUBLISHER = BASE + "publisher";

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
		public static final String PROP_IS_PART_OF = BASE + "isPartOf";
		public static final String PROP_TITLE = BASE + "title";
		public static final String PROP_ISSUED = BASE + "issued";

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
		public static final String STRING = BASE + "string";
		public static final String ANY_URI = BASE + "anyURI";
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

	public static final class DM2E {
		public static final String BASE         = "http://onto.dm2e.eu/schemas/dm2e/1.1/";
		public static final String CLASS_MANUSCRIPT = BASE + "Manuscript";
		public static final String PROP_HAS_ANNOTABLE_VERSION_AT = BASE + "hasAnnotatableVersionAt";
		public static final String PROP_PRINTED_AT = BASE + "printedAt";
	}
	public static final class FABIO {
		public static final String BASE         = "http://purl.org/spar/fabio/";
		public static final String CLASS_ARTICLE = BASE + "Article";
	}
	public static final class BIBO {
		public static final String BASE         = "http://purl.org/ontology/bibo/";
		public static final String CLASS_ISSUE = BASE + "Issue";
	}
	public static final class EDM {
		public static final String BASE = "http://www.europeana.eu/schemas/edm/";
		public static final String PROP_AGGREGATED_CHO = BASE + "aggregatedCHO";
		public static final String PROP_IS_SHOWN_BY = BASE + "isShownBy";
		public static final String PROP_IS_SHOWN_AT = BASE + "isShownAT";
		public static final String PROP_RIGHTS = BASE + "rights";
		public static final Object CLASS_PROVIDED_CHO = BASE + "ProvidedCHO";
	}
	public static final class PRO {
		public static final String BASE = "http://purl.org/spar/pro/";
		public static final String PROP_AUTHOR = BASE + "author";
	}
	public static final class DM2E_UNOFFICIAL {
		public static final String BASE = "http://onto.dm2e.eu/UNOFFICIAL/";
		public static final String PROP_HAS_COLLECTION = BASE + "hasCollection";
		public static final String PROP_HAS_VERSION    = BASE + "hasVersion";
		public static final String PROP_CONTAINS_CHO = BASE + "containsCHO";
	}
	public static final class OAI {
		public static final String BASE = "http://www.openarchives.org/OAI/2.0/";
	}
	public static final class OAI_DC {
		public static final String BASE = "http://www.openarchives.org/OAI/2.0/oai_dc/";
	}
	public static final class OAI_RIGHTS {
		public static final String BASE = "http://www.openarchives.org/OAI/2.0/rights/";
	}
	public static final class CRM {
		public static final String BASE = "http://www.cidoc-crm.org/cidoc-crm/";
	}
	public static final class OWL {
		public static final String BASE = "http://www.w3.org/2002/07/owl#";
		public static final String CLASS = BASE + "Class";
		public static final String DATATYPE_PROPERTY = BASE + "DatatypeProperty";
		public static final String OBJECT_PROPERTY = BASE + "ObjectProperty";
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
