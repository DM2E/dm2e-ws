package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import java.net.URI;
import java.net.URISyntaxException;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
	 "dc", "http://purl.org/dc/elements/1.1/",
	 "dct", "http://purl.org/dc/terms/"})
@RDFClass(NS.OMNOM.CLASS_FILE)
@RDFInstancePrefix("http://localhost:9998/file/")
public class FilePojo extends AbstractPersistentPojo<FilePojo>{
	
	@RDFId
	private String id;
	
	@RDFProperty(NS.OMNOM.PROP_FILE_LOCATION)
	private String fileLocation;
	
	@RDFProperty(NS.OMNOM.PROP_FILE_STATUS)
	private String fileStatus;
	
	@RDFProperty(NS.DCTERMS.PROP_FORMAT)
	private String mediaType;
	
	@RDFProperty(NS.DC.PROP_TITLE)
	private String originalName;
	
	@RDFProperty(NS.OMNOM.PROP_MD5)
	private String md5;
	
	@RDFProperty(NS.DCTERMS.PROP_EXTENT)
	private long fileSize;
	
	@RDFProperty(NS.PROV.PROP_WAS_GENERATED_BY)
	private JobPojo generatorJob;
	
	@RDFProperty(NS.OMNOM.PROP_FILE_RETRIEVAL_URI)
	private String fileRetrievalURI;

	@RDFProperty(NS.OMNOM.PROP_FILE_EDIT_URI)
	private String fileEditURI;
	
	/***************************************************
	 * 
	 * GETTERS AND SETTERS FROM HERE ON 
	 * 
	 ***************************************************/
	public String getFileLocation() { return fileLocation; }
	public void setFileLocation(String fileLocation) { this.fileLocation = fileLocation; }

	public String getMediaType() { return mediaType; }
	public void setMediaType(String mediaType) { this.mediaType = mediaType; }

	public String getOriginalName() { return originalName; }
	public void setOriginalName(String originalName) { this.originalName = originalName; }

	public String getMd5() { return md5; }
	public void setMd5(String md5) { this.md5 = md5; }

	public long getFileSize() { return fileSize; }
	public void setFileSize(long fileSize) { this.fileSize = fileSize; }

	public String getFileRetrievalURI() { return fileRetrievalURI; }
	public void setFileRetrievalURI(String fileRetrievalURI) { this.fileRetrievalURI = fileRetrievalURI; }

	public String getFileEditURI() { return fileEditURI; }
	public void setFileEditURI(String fileEditURI) { this.fileEditURI = fileEditURI; }

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getFileStatus() { return fileStatus; }
	public void setFileStatus(String fileStatus) { this.fileStatus = fileStatus; }
	
	public JobPojo getGeneratorJob() { return generatorJob; }
	public void setGeneratorJob(JobPojo generatorJob) { this.generatorJob = generatorJob; }
}
