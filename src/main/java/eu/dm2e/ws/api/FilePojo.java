package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
	 "dc", "http://purl.org/dc/elements/1.1/",
	 "dct", "http://purl.org/dc/terms/"})
@RDFClass(NS.OMNOM.CLASS_FILE)
@RDFInstancePrefix("http://localhost:9998/file/")
public class FilePojo extends AbstractPersistentPojo<FilePojo>{

	/********************
	 * 
	 * GETTERS / SETTERS 
	 * 
	 *********************/
	@RDFProperty(NS.OMNOM.PROP_FILE_LOCATION)
	private String fileLocation;
	public String getFileLocation() { return fileLocation; }
	public void setFileLocation(String fileLocation) { this.fileLocation = fileLocation; }

	@RDFProperty(NS.DCTERMS.PROP_FORMAT)
	private String mediaType;
	public String getMediaType() { return mediaType; }
	public void setMediaType(String mediaType) { this.mediaType = mediaType; }

	@RDFProperty(NS.DC.PROP_TITLE)
	private String originalName;
	public String getOriginalName() { return originalName; }
	public void setOriginalName(String originalName) { this.originalName = originalName; }

	@RDFProperty(NS.OMNOM.PROP_MD5)
	private String md5;
	public String getMd5() { return md5; }
	public void setMd5(String md5) { this.md5 = md5; }

	@RDFProperty(NS.DCTERMS.PROP_EXTENT)
	private long fileSize;
	public long getFileSize() { return fileSize; }
	public void setFileSize(long fileSize) { this.fileSize = fileSize; }

	@RDFProperty(NS.OMNOM.PROP_FILE_RETRIEVAL_URI)
	private String fileRetrievalURI;
	public String getFileRetrievalURI() { return fileRetrievalURI; }
	public void setFileRetrievalURI(String fileRetrievalURI) { this.fileRetrievalURI = fileRetrievalURI; }

	@RDFProperty(NS.OMNOM.PROP_FILE_EDIT_URI)
	private String fileEditURI;
	public String getFileEditURI() { return fileEditURI; }
	public void setFileEditURI(String fileEditURI) { this.fileEditURI = fileEditURI; }

	@RDFProperty(NS.OMNOM.PROP_FILE_STATUS)
	private String fileStatus;
	public String getFileStatus() { return fileStatus; }
	public void setFileStatus(String fileStatus) { this.fileStatus = fileStatus; }
	
	@RDFProperty(NS.PROV.PROP_WAS_GENERATED_BY)
	private JobPojo generatorJob;
	public JobPojo getGeneratorJob() { return generatorJob; }
	public void setGeneratorJob(JobPojo generatorJob) { this.generatorJob = generatorJob; }
	
	/**********************
	 * 
	 * equals / hashCode()
	 * 
	 ********************/
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fileEditURI == null) ? 0 : fileEditURI.hashCode());
		result = prime * result + ((fileRetrievalURI == null) ? 0 : fileRetrievalURI.hashCode());
		result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
		result = prime * result + ((generatorJob == null) ? 0 : generatorJob.hashCode());
		result = prime * result + ((md5 == null) ? 0 : md5.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (!(obj instanceof FilePojo)) return false;
		FilePojo other = (FilePojo) obj;
		if (fileEditURI == null) {
			if (other.fileEditURI != null) return false;
		} else if (!fileEditURI.equals(other.fileEditURI)) return false;
		if (fileRetrievalURI == null) {
			if (other.fileRetrievalURI != null) return false;
		} else if (!fileRetrievalURI.equals(other.fileRetrievalURI)) return false;
		if (fileSize != other.fileSize) return false;
		if (generatorJob == null) {
			if (other.generatorJob != null) return false;
		} else if (!generatorJob.equals(other.generatorJob)) return false;
		if (md5 == null) {
			if (other.md5 != null) return false;
		} else if (!md5.equals(other.md5)) return false;
		return true;
	}
}
