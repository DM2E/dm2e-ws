package eu.dm2e.ws.services.xslt;

import java.io.StringWriter;
import java.net.URL;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractTransformationService;

@Path("/service/xslt")
public class XsltService extends AbstractTransformationService {

	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();

		ParameterPojo xsltInParam = ws.addInputParameter("xsltInParam");
		xsltInParam.setTitle("XSLT input");
		xsltInParam.setIsRequired(true);

		ParameterPojo xmlInParam = ws.addInputParameter("xmlInParam");
		xmlInParam.setTitle("XML input");
		xmlInParam.setIsRequired(true);

		ParameterPojo xmlOutParam = ws.addOutputParameter("xmlOutParam");
		xmlOutParam.setTitle("XML output");
		
		ParameterPojo fileServiceParam = ws.addInputParameter("fileServiceParam");
		fileServiceParam.setIsRequired(false);
		
		return ws;
	}

	@Override
	public void run() {
		log.warning("FOO");
		WebResource fileResource = jerseyClient.resource(FILE_SERVICE_URI);
		jobPojo.debug("Starting to handle XSLT transformation job");
		String xmlUrl, xsltUrl;
		try {
			// TODO this should be refactored to a validation routine in the JobPojo
			xmlUrl = jobPojo.getWebserviceConfig().getParameterValueByName("xmlInParam");
			xsltUrl = jobPojo.getWebserviceConfig().getParameterValueByName("xsltInParam");
			if (null == xmlUrl) {
				throw new NullPointerException("xmlUrl is null");
			}
			if (null == xsltUrl) {
				throw new NullPointerException("xsltUrl is null");
			}
		} catch (Exception e) {
			jobPojo.fatal(e);
			jobPojo.setFailed();
			return;
		}
		jobPojo.debug("XML URL: " + xmlUrl);
		jobPojo.debug("XSL URL: " + xsltUrl);
		jobPojo.info("Starting transformation");

		// update job status
		jobPojo.setStarted();
		TransformerFactory tFactory = TransformerFactory.newInstance();
		StringWriter xslResultStrWriter = new StringWriter();
		try {
			StreamSource xmlSource = new StreamSource(new URL(xmlUrl).openStream());
			StreamSource xslSource = new StreamSource(new URL(xsltUrl).openStream());
			Transformer transformer = tFactory.newTransformer(xslSource);

			StreamResult xslResult = new StreamResult(xslResultStrWriter);

			transformer.transform(xmlSource, xslResult);

		} catch (Exception e) {
			jobPojo.fatal("Error during XSLT transformation: " + e);
			jobPojo.debug(e);
			jobPojo.setFailed();
			return;
		}
		jobPojo.info("Getting the transformation result as a string.");
		String xslResultStr = "";
		try {
			xslResultStr = xslResultStrWriter.toString();
			if (xslResultStr.length() == 0) {
				throw new RuntimeException("No result from the transformation.");
			}
			else if (xslResultStr.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
				jobPojo.warn("XSLT transformation yielded in empty XML file.");
			}
		} catch (Exception e) {
			jobPojo.debug(e);
			jobPojo.setFailed();
			return;
		}
		
		jobPojo.trace("Building file service multipart envelope.");
		FormDataMultiPart form = new FormDataMultiPart();

		// add file part
		MediaType xml_type = MediaType.valueOf(MediaType.APPLICATION_XML);
		FormDataBodyPart fileFDBP = new FormDataBodyPart("file", xslResultStr, xml_type);
		form.bodyPart(fileFDBP);

		// add metadata part
		FilePojo fileDesc = new FilePojo();
		fileDesc.setGeneratorJob(jobPojo);
		fileDesc.setMediaType(xml_type.toString());
		MediaType n3_type = MediaType.valueOf(DM2E_MediaType.TEXT_RDF_N3);
		FormDataBodyPart metaFDBP = new FormDataBodyPart("meta", fileDesc.getNTriples(), n3_type);
		form.bodyPart(metaFDBP);

		// build the response stub
		Builder builder = fileResource
				.type(MediaType.MULTIPART_FORM_DATA)
				.accept(DM2E_MediaType.TEXT_TURTLE)
				.entity(form);
		
		// Post the file to the file service
		jobPojo.info("Posting result to the file service.");
		ClientResponse resp = builder.post(ClientResponse.class);
		if (resp.getStatus() >= 400) {
			jobPojo.fatal("File storage failed: " + resp.getEntity(String.class));
			jobPojo.setFailed();
			return;
		}
		String fileLocation = resp.getLocation().toString();
		jobPojo.info("File stored at: " + fileLocation);
		try {
			fileDesc.setFileRetrievalURI(fileLocation);
			fileDesc.publish();
		} catch(Exception e) {
			jobPojo.fatal(e);
		}
		
		jobPojo.info("Store result URI on the job (" + fileLocation + ").");
		ParameterAssignmentPojo ass = new ParameterAssignmentPojo();
		ass.setForParam(jobPojo.getWebService().getParamByName("xmlOutParam"));
		ass.setParameterValue(fileLocation);
		jobPojo.addOutputParameterAssignment(ass);
		jobPojo.publish();

		// Update job status
		jobPojo.info("XSLT Transformation complete.");
		jobPojo.setFinished();
	}
}