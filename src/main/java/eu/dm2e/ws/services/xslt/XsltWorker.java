package eu.dm2e.ws.services.xslt;

import java.io.StringWriter;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.worker.AbstractWorker;

/**
 * This worker transforms XML to XML using an XSLT style sheet and the Saxon XSL
 * transformation engine
 * 
 * @todo should think about where to store client and webresources (client
 *       creation is expensive and thread safe)
 *       http://stackoverflow.com/questions
 *       /8012680/jersey-client-connection-close-memory-leak-issue
 * @author kb
 * 
 */
public class XsltWorker extends AbstractWorker {

	public XsltWorker(JobPojo job) {
		super(job);
	}

	// private static final String SERVICE_URI =
	// Config.getString("dm2e.service.xslt.base_uri");
	private static final String FILE_SERVICE_URI = Config.getString("dm2e.service.file.base_uri");
	// private static final String NS_XSLT_SERVICE =
	// Config.getString("dm2e.service.xslt.namespace");

	// private static final String PROPERTY_XML_SOURCE = NS_XSLT_SERVICE +
	// "xmlSource";
	// private static final String PROPERTY_XSLT_SOURCE = NS_XSLT_SERVICE +
	// "xsltSource";
	//
	// private final String jobUri;
	// public String getJobUri() { return jobUri; }

	// public XsltWorker(String jobUri) {
	// this.jobUri = jobUri;
	// }

	// Logging
//	Logger log = Logger.getLogger(getClass().getName());

	// @Override
	// public String getServiceUri() {
	// return SERVICE_URI;
	// }

	@Override
	public Client getClient() {
		return client;
	}

	@Override
	public void run() {

		// Web Resource for the file
		WebResource fileResource = getClient().resource(FILE_SERVICE_URI);

		// create a logger that logs both to console our job resource
		//		JobLogger jobLog = new JobLogger(this, getJob().getId());

		// Wrapper to change job status
		//		JobStatus jobStatus = new JobStatus(this, getJob().getId());

		// Generate Grafeo for the current job
		//		job = 
		//		GrafeoImpl jobModel = new GrafeoImpl();
		//		jobModel.load(jobUri);

		job.debug("Starting to handle XSLT transformation job");

		// Find the configuration for this worker run
		//		GrafeoImpl configModel = new GrafeoImpl();
		//		String configUri;
		//		try {
		//			NodeIterator iter = jobModel.getModel().listObjectsOfProperty(
		//					jobModel.getModel().createProperty(NS.DM2E + "hasWebServiceConfig"));
		//			configUri = iter.next().toString();
		//		} catch (Exception e) {
		//			jobLog.severe("Job is missing hasWebServiceConfig: " + e.toString());
		//			jobStatus.failed();
		//			return;
		//		}

		// Populate the configuration model
		//		jobLog.info("Config URL: " + configUri);
		//		configModel.load(configUri);

		// Get the input parameters
		String xmlUrl, xsltUrl;
		try {
			xmlUrl = job.getWebServiceConfig().getParameterValueByName("xmlInParam");
			xsltUrl = job.getWebServiceConfig().getParameterValueByName("xsltInParam");
			job.debug("XML URL: " + xmlUrl);
			job.debug("XSL URL: " + xsltUrl);
		} catch (Exception e) {
			job.fatal(e);
			job.setFailed();
			return;
		}
		//		try {
		//			NodeIterator iter;
		//			iter = configModel.getModel().listObjectsOfProperty(
		//					jobModel.getModel().createProperty(PROPERTY_XML_SOURCE));
		//			xmlUrl = iter.next().toString();
		//			jobLog.info(xmlUrl);
		//			iter = configModel.getModel().listObjectsOfProperty(
		//					jobModel.getModel().createProperty(PROPERTY_XSLT_SOURCE));
		//			xsltUrl = iter.next().toString();
		//		} catch (Exception e) {
		//			jobLog.severe("Job is missing either xmlSource or xsltSource" + e.toString());
		//			jobStatus.failed();
		//			return;
		//		}

		// Make sure that the resources are available
		//		jobLog.fine("Waiting for all resources to become ready.");
		//		ArrayList<WebResource> unreadyResources = new ArrayList<WebResource>();
		//		unreadyResources.add(client.resource(xsltUrl));
		//		unreadyResources.add(client.resource(xmlUrl));
		//		try {
		//			ensureResourcesReady(unreadyResources, jobLog);
		//		} catch (Throwable e) {
		//			jobLog.severe(e.toString());
		//			jobStatus.failed();
		//		}

		job.info("Starting transformation");

		// update job status
		job.setStarted();
		TransformerFactory tFactory = TransformerFactory.newInstance();
		StringWriter xslResultStrWriter = new StringWriter();
		try {

			StreamSource xmlSource = new StreamSource(new URL(xmlUrl).openStream());
			StreamSource xslSource = new StreamSource(new URL(xsltUrl).openStream());
			Transformer transformer = tFactory.newTransformer(xslSource);

			StreamResult xslResult = new StreamResult(xslResultStrWriter);

			transformer.transform(xmlSource, xslResult);

		} catch (Exception e) {
			job.fatal("Error during XSLT transformation: " + e);
			job.setFailed();
			return;
		}

		job.info("Writing result to file service.");
		String xslResultStr = xslResultStrWriter.toString();
		if (xslResultStr.length() == 0) {
			job.fatal("Empty result.");
			job.setFailed();
			return;
		}
		FormDataMultiPart form = new FormDataMultiPart();

		// add file part
		MediaType xml_type = MediaType.valueOf(MediaType.APPLICATION_XML);
		FormDataBodyPart fileFDBP = new FormDataBodyPart("file", xslResultStr, xml_type);
		form.bodyPart(fileFDBP);

		// add metadata part
		// FormDataBodyPart metaFDBP = new FormDataBodyPart("meta",
		// xslResult, xml_type);
		FilePojo fileDesc = new FilePojo();
		fileDesc.setGeneratorJob(job);
		fileDesc.setMediaType(xml_type.toString());
		//			metaModel.add(blank, metaModel.createProperty(NS.DM2E + "generatedBy"), metaModel.createResource(jobUri));
		//			String metaNTriples = metaGrafeo.getNTriples().replaceAll("_[^\\s]+", "[]");
		//			String metaNTriples = metaGrafeo.getNTriples();	
		MediaType n3_type = MediaType.valueOf(DM2E_MediaType.TEXT_RDF_N3);
		FormDataBodyPart metaFDBP = new FormDataBodyPart("meta", fileDesc.getNTriples(), n3_type);
		form.bodyPart(metaFDBP);


		Builder builder = fileResource
				.type(MediaType.MULTIPART_FORM_DATA)
				.accept(DM2E_MediaType.TEXT_TURTLE)
				.entity(form);
		ClientResponse resp = builder.post(ClientResponse.class);
		if (resp.getStatus() >= 400) {
			job.fatal("File storage failed: " + resp.getEntity(String.class));
			return;
		}
		String fileLocation = resp.getLocation().toString();
		job.info("File stored at: " + fileLocation);
		fileDesc.setFileRetrievalURI(fileLocation);
		fileDesc.publish();
		
		ParameterAssignmentPojo ass = new ParameterAssignmentPojo();
		ass.setForParam(job.getWebService().getParamByName("xmlOutParam"));
		ass.setParameterValue(fileLocation);
		job.addOutputParameterAssignment(ass);
		job.publish();

		// Update job status
		job.info("XSLT Transformation complete.");
		job.setFinished();
	}
}