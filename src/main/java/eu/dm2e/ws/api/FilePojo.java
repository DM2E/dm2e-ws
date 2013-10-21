package eu.dm2e.ws.api;

import java.net.URI;

import org.joda.time.DateTime;

import eu.dm2e.ws.NS;
import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.services.file.FileStatus;

/** Pojo for Files */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
	 "dc", "http://purl.org/dc/elements/1.1/",
	 "dct", "http://purl.org/dc/terms/"})
@RDFClass(NS.OMNOM.CLASS_FILE)
@RDFInstancePrefix("http://localhost:9998/file/")
public class FilePojo extends AbstractPersistentPojo<FilePojo>{
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((fileEditURI == null) ? 0 : fileEditURI.hashCode());
		result = prime * result + ((internalFileLocation == null) ? 0 : internalFileLocation.hashCode());
		result = prime * result + ((fileRetrievalURI == null) ? 0 : fileRetrievalURI.hashCode());
		result = prime * result + (int) (extent ^ (extent >>> 32));
		result = prime * result + ((fileStatus == null) ? 0 : fileStatus.hashCode());
		result = prime * result + ((fileType == null) ? 0 : fileType.hashCode());
		result = prime * result + ((modified == null) ? 0 : modified.hashCode());
		result = prime * result + ((md5 == null) ? 0 : md5.hashCode());
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + ((originalName == null) ? 0 : originalName.hashCode());
		result = prime * result + ((itemRootXPath == null) ? 0 : itemRootXPath.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FilePojo other = (FilePojo) obj;
		if (created == null) {
			if (other.created != null) return false;
		} else if (!created.equals(other.created)) return false;
		if (getId() == null) {
			if (other.getId() != null) return false;
		} else if (!getId().equals(other.getId())) return false;
		if (fileEditURI == null) {
			if (other.fileEditURI != null) return false;
		} else if (!fileEditURI.equals(other.fileEditURI)) return false;
		if (internalFileLocation == null) {
			if (other.internalFileLocation != null) return false;
		} else if (!internalFileLocation.equals(other.internalFileLocation)) return false;
		if (fileRetrievalURI == null) {
			if (other.fileRetrievalURI != null) return false;
		} else if (!fileRetrievalURI.equals(other.fileRetrievalURI)) return false;
		if (extent != other.extent) return false;
		if (fileStatus == null) {
			if (other.fileStatus != null) return false;
		} else if (!fileStatus.equals(other.fileStatus)) return false;
		if (fileType == null) {
			if (other.fileType != null) return false;
		} else if (!fileType.equals(other.fileType)) return false;
		if (modified == null) {
			if (other.modified != null) return false;
		} else if (!modified.equals(other.modified)) return false;
		if (md5 == null) {
			if (other.md5 != null) return false;
		} else if (!md5.equals(other.md5)) return false;
		if (format == null) {
			if (other.format != null) return false;
		} else if (!format.equals(other.format)) return false;
		if (originalName == null) {
			if (other.originalName != null) return false;
		} else if (!originalName.equals(other.originalName)) return false;
		if (itemRootXPath == null) {
			if (other.itemRootXPath != null) return false;
		} else if (!itemRootXPath.equals(other.itemRootXPath)) return false;
		return true;
	}

	/********************
	 * 
	 * GETTERS / SETTERS 
	 * 
	 *********************/
	@RDFProperty(NS.OMNOM.PROP_FILE_LOCATION)
	private String internalFileLocation;
	public String getInternalFileLocation() { return internalFileLocation; }
	public void setInternalFileLocation(String fileLocation) { this.internalFileLocation = fileLocation; }

	@RDFProperty(NS.DCTERMS.PROP_FORMAT)
	private String format;
	public String getFormat() { return format; }
	public void setFormat(String mediaType) { this.format = mediaType; }

	@RDFProperty(NS.OMNOM.PROP_ORIGINAL_NAME)
	private String originalName;
	public String getOriginalName() { return originalName; }
	public void setOriginalName(String originalName) { this.originalName = originalName; }

	@RDFProperty(NS.OMNOM.PROP_MD5)
	private String md5;
	public String getMd5() { return md5; }
	public void setMd5(String md5) { this.md5 = md5; }

	@RDFProperty(NS.DCTERMS.PROP_EXTENT)
	private long extent;
	public long getExtent() { return extent; }
	public void setExtent(long fileSize) { this.extent = fileSize; }

	@RDFProperty(NS.OMNOM.PROP_FILE_RETRIEVAL_URI)
	private URI fileRetrievalURI;
	public URI getFileRetrievalURI() { return fileRetrievalURI; }
	public void setFileRetrievalURI(URI fileRetrievalURI) { this.fileRetrievalURI = fileRetrievalURI; }
	public void setFileRetrievalURI(String fileRetrievalURI) { this.fileRetrievalURI = URI.create(fileRetrievalURI) ; }

	@RDFProperty(NS.OMNOM.PROP_FILE_EDIT_URI)
	private URI fileEditURI;
	public URI getFileEditURI() { return fileEditURI; }
	public void setFileEditURI(URI fileEditURI) { this.fileEditURI = fileEditURI; }
	public void setFileEditURI(String fileEditURI) { this.fileEditURI = URI.create(fileEditURI); }
	
	@RDFProperty(NS.OMNOM.PROP_FILE_TYPE)
	private URI fileType;
	public URI getFileType() { return fileType; }
	public void setFileType(URI fileType) { this.fileType = fileType; }
	public void setFileType(String fileType) { this.fileType = URI.create(fileType); }
	
	@RDFProperty(NS.DCTERMS.PROP_MODIFIED)
	private DateTime modified;
	public DateTime getModified() { return modified; }
	public void setModified(DateTime lastModified) { this.modified = lastModified; }
	
	@RDFProperty(NS.DCTERMS.PROP_CREATED)
	private DateTime created;
	public DateTime getCreated() { return created; }
	public void setCreated(DateTime created) { this.created = created; }

	@RDFProperty(NS.OMNOM.PROP_FILE_STATUS)
	private String fileStatus = FileStatus.AVAILABLE.name();
	public String getFileStatus() { return fileStatus; }
	public void setFileStatus(String fileStatus) { this.fileStatus = fileStatus; }
	
	@RDFProperty(NS.PROV.PROP_WAS_GENERATED_BY)
	private JobPojo wasGeneratedBy;
	public JobPojo getWasGeneratedBy() { return wasGeneratedBy; }
	public void setWasGeneratedBy(JobPojo generatorJob) { this.wasGeneratedBy = generatorJob; }
	
	@RDFProperty(NS.OMNOM.PROP_ITEM_ROOT_XPATH)
	private String itemRootXPath;
	public String getItemRootXPath() { return itemRootXPath; }
	public void setItemRootXPath(String itemRoot) { this.itemRootXPath = itemRoot; }
	
	@RDFProperty(NS.OMNOM.PROP_ITEM_LABEL_XPATH)
	private String itemLabelXPath;
	public String getItemLabelXPath() { return itemLabelXPath; }
	public void setItemLabelXPath(String itemLabelXPath) { this.itemLabelXPath = itemLabelXPath; }
	
	@RDFProperty(value = NS.OMNOM.PROP_FILE_OWNER, serializeAsURI = true)
	private UserPojo fileOwner;
	public UserPojo getFileOwner() { return fileOwner; }
	public void setFileOwner(UserPojo fileOwner) { this.fileOwner = fileOwner; }
}
