package eu.dm2e.ws.services.publish;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.VersionedDatasetPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.AbstractTransformationService;

import javax.ws.rs.Path;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
@Path("/publish")
public class PublishService extends AbstractTransformationService {
    private Logger log = Logger.getLogger(getClass().getName());

    public PublishService() {
        WebservicePojo ws = getWebServicePojo();
        ws.addInputParameter("to-publish").setIsRequired(true);
        ws.addInputParameter("dataset-id").setIsRequired(true);
        ws.addInputParameter("provider-id").setIsRequired(true);
        ws.addInputParameter("label").setIsRequired(true);
        ws.addInputParameter("comment");
        ws.addInputParameter("endpoint-update");
        ws.addInputParameter("endpoint-select");
    }

    @Override
    public void run() {
        try {
            WebserviceConfigPojo wsConf = jobPojo.getWebserviceConfig();
            jobPojo.debug("wsConf: " + wsConf);

            String input = wsConf.getParameterValueByName("to-publish");
            String dataset = wsConf.getParameterValueByName("dataset-id");
            String provider = wsConf.getParameterValueByName("provider-id");
            String label = wsConf.getParameterValueByName("label");
            String comment = wsConf.getParameterValueByName("comment");
            String endpoint = wsConf.getParameterValueByName("endpoint-update");
            String endpointSelect = wsConf.getParameterValueByName("endpoint-select");
            if (null == endpoint) endpoint = NS.ENDPOINT_STATEMENTS;
            if (null == endpointSelect) endpointSelect = NS.ENDPOINT;

            jobPojo.debug("Input file: " + input);
            jobPojo.debug("Dataset: " + dataset);
            jobPojo.debug("Label: " + label);
            jobPojo.debug("Comment: " + comment);
            jobPojo.debug("Endpoint: " + endpoint);

            jobPojo.setStarted();

            String datasetURI = dataset;
            if (!dataset.startsWith("http")) datasetURI = Config.getString("dm2e.service.publish.graph_prefix") + provider + "/" + dataset;

            String versionedURI = datasetURI;
            if (!versionedURI.endsWith("/")) versionedURI = versionedURI + "/";
            versionedURI = versionedURI + new Date().getTime();

            VersionedDatasetPojo ds = new VersionedDatasetPojo();
            ds.setId(versionedURI);
            ds.setLabel(label != null ? label : dataset);
            ds.setComment(comment);
            ds.setTimestamp(new Date());
            try {
                ds.setJobURI(new URI(jobPojo.getId()));
                ds.setDatasetID(new URI(datasetURI));
            } catch (URISyntaxException e) {
                throw new RuntimeException("Error in URI: " + e, e);
            }
            ds.findLatest(endpointSelect);


            Grafeo g = new GrafeoImpl();
            g.loadWithoutContentNegotiation(input);
            g.getObjectMapper().addObject(ds);
            log.info("Published graph: " + g.getTurtle());
            jobPojo.debug("Published graph: " + g.getTurtle());
            log.info("Write to endpoint: " + endpoint + " / Graph: " + versionedURI);
            g.writeToEndpoint(endpoint, versionedURI);

        } catch (Throwable t) {
            log.log(Level.SEVERE, "Exception during publishing: " + t, t);
            jobPojo.fatal(t);
            jobPojo.setFailed();
            throw t;
        }
        jobPojo.setFinished();
    }


}
