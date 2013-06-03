package eu.dm2e.ws.services.demo;

import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.model.JobStatusConstants;
import eu.dm2e.ws.services.AbstractTransformationService;

import javax.ws.rs.Path;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
@Path("/service/demo")
public class DemoService extends AbstractTransformationService {

    public DemoService() {
        ParameterPojo sleeptimeParam = getWebServicePojo().addInputParameter("sleeptime");
        sleeptimeParam.setParameterType("xsd:int");
        sleeptimeParam.setIsRequired(true);
        ParameterPojo countdownPhraseParam = getWebServicePojo().addInputParameter("countdownPhrase");
        countdownPhraseParam.setIsRequired(false);
    }

    @Override
    public void run() {
        jobPojo.debug("DemoWorker starts to run now.");
        WebserviceConfigPojo wsConf = jobPojo.getWebserviceConfig();
        jobPojo.debug("wsConf: " + wsConf);

        int sleepTime = 0;
        jobPojo.debug(wsConf.getParameterValueByName("sleeptime"));
        jobPojo.debug(wsConf.getParameterValueByName("countdownPhrase"));

        try {
            sleepTime = Integer.parseInt(wsConf.getParameterValueByName("sleeptime"));
        } catch(Exception e) {
            jobPojo.warn(wsConf.getParameterValueByName("sleeptime") + " " + ErrorMsg.ILLEGAL_PARAMETER_VALUE.toString() + " " + e);
        }
        String countdownPhrase = (null == wsConf.getParameterValueByName("countdownPhrase"))
        		? "bottles of beer on the wall."
        		: wsConf.getParameterValueByName("countdownPhrase");

        jobPojo.debug("DemoWorker will sleep for " + sleepTime + " seconds.");
        jobPojo.setStarted();

        // snooze
        try {
            for (int i=0 ; i < sleepTime ; i++) {
				jobPojo.info((sleepTime - i) + " " + countdownPhrase);
//                jobPojo.trace("Still Sleeping for " + (sleepTime - i) + " seconds.");
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            jobPojo.setStatus(JobStatusConstants.FAILED);
            jobPojo.fatal(e.toString());
            return;
        }

        jobPojo.debug("DemoWorker is finished now.");
        jobPojo.setStatus(JobStatusConstants.FINISHED);
        jobPojo.publishToEndpoint();
    }
}
