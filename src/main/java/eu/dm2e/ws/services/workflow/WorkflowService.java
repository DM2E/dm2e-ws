package eu.dm2e.ws.services.workflow;

import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractRDFService;

public class WorkflowService extends AbstractRDFService {

    @Override
    public WebservicePojo getWebServicePojo() {
        WebservicePojo ws = new WebservicePojo();
        ws.setId("http://localhost:9998/data");
        return ws;
    }


}