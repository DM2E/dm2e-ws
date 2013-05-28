package eu.dm2e.ws.api;

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
@RDFClass("omnom:File")
@RDFInstancePrefix("http://localhost:9998/file/")
public class FilePojo extends AbstractPersistentPojo<FilePojo>{
	
	@RDFId
	private String id;
	
	@RDFProperty("omnom:fileLocation")
	private String fileLocation;
	
	@RDFProperty("omnom:fileStatus")
	private String fileStatus;
	
	@RDFProperty("dct:format")
	private String mediaType;
	
	@RDFProperty("dc:title")
	private String originalName;
	
	@RDFProperty("omnom:md5")
	private String md5;
	
	@RDFProperty("dct:extent")
	private long fileSize;
	
	@RDFProperty("omnom:generatedBy")
	private JobPojo generatorJob;
	
	@RDFProperty("omnom:fileRetrievalURI")
	private URI fileRetrievalURI;

	@RDFProperty("omnom:fileEditURI")
	private URI fileEditURI;

//	public static FilePojo readFilePojo(Grafeo g) {
//		GResource blank = g.findTopBlank();
//		FilePojo filePojo = readFromGrafeo(g, blank);
//		// TODO rename
//		return filePojo;
//	}
//	
//	public static FilePojo readFromGrafeo(Grafeo g, String subject) { return readFromGrafeo(g, g.resource(subject)); }
//	public static FilePojo readFromGrafeo(Grafeo g, URI subject) { return readFromGrafeo(g, g.resource(subject)); }
//	
//	public static FilePojo readFromGrafeo(Grafeo g, GResource subject) {
//		FilePojo filePojo = new FilePojo();
////		filePojo.setIdURI(subject);
//		
//		Object fileLocation = g.firstMatchingObject(subject.toString(), "omnom:fileLocation");
//		if (null != fileLocation) filePojo.setFileLocation(fileLocation.toString());
//		
//		Object mediaType = g.firstMatchingObject(subject.toString(), "dct:format");
//		if (null != mediaType) filePojo.setMediaType(mediaType.toString());
//		
//		Object originalName = g.firstMatchingObject(subject.toString(), "dc:title");
//		if (null != originalName) filePojo.setOriginalName(originalName.toString());
//		
//		Object md5 = g.firstMatchingObject(subject.toString(), "omnom:md5");
//		if (null != md5) filePojo.setMd5(md5.toString());
//
//		Object fileRetrievalURI = g.firstMatchingObject(subject.toString(), "omnom:fileRetrievalURI");
//		if (null != fileRetrievalURI) filePojo.setFileRetrievalURI(fileRetrievalURI.toString());
//
//		Object fileSize = g.firstMatchingObject(subject.toString(), "dct:extent");
//		if (null != fileSize) filePojo.setFileSize(Long.parseLong(fileSize.toString()));
//
//		Object fileEditURI = g.firstMatchingObject(subject.toString(), "omnom:fileEditURI");
//		if (null != fileEditURI) filePojo.setFileEditURI(fileEditURI.toString());
//		
////		if (fileRetrievalURI)
//		
//		return filePojo;
//	}	

	
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

	public URI getFileRetrievalURI() { return fileRetrievalURI; }

	public void setFileRetrievalURI(URI fileRetrievalURI) { this.fileRetrievalURI = fileRetrievalURI; }
	public void setFileRetrievalURI(String fileRetrievalURIString) {
		try {
			this.fileRetrievalURI = new URI(fileRetrievalURIString);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public URI getFileEditURI() { return fileEditURI; }
	public void setFileEditURI(URI fileEditURI) { this.fileEditURI = fileEditURI; }
	public void setFileEditURI(String fileEditURIString) {
		try {
			this.fileEditURI = new URI(fileEditURIString);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getFileStatus() { return fileStatus; }
	public void setFileStatus(String fileStatus) { this.fileStatus = fileStatus; }
	
	public JobPojo getGeneratorJob() { return generatorJob; }
	public void setGeneratorJob(JobPojo generatorJob) { this.generatorJob = generatorJob; }
}
