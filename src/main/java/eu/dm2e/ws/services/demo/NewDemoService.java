package eu.dm2e.ws.services.demo;

import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.model.JobStatusConstants;
import eu.dm2e.ws.services.AbstractTransformationService;

import javax.ws.rs.Path;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 5/27/13
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/service/newdemo")
public class NewDemoService extends AbstractTransformationService {

    public NewDemoService() {
        getWebServicePojo().addInputParameter("sleeptime");
    }

    @Override
    public void run() {
        log.info("Seriously, we start now...");
        // jobPojo.debug("DemoWorker starts to run now.");
        log.info("1");

        WebserviceConfigPojo wsConf = jobPojo.getWebserviceConfig();
        log.info("2");
        jobPojo.debug("wsConf: " + wsConf);
        log.info("3");

        int sleepTime = 0;
        log.info("4");
        jobPojo.debug(wsConf.getParameterValueByName("sleepTime"));
        log.info("5");
        try {
            sleepTime = Integer.parseInt(wsConf.getParameterValueByName("sleepTime"));
            log.info("6");
        } catch(Exception e) {
            jobPojo.warn("Exception occured!: " + e);
            log.info("7");

        }

        jobPojo.debug("DemoWorker will sleep for " + sleepTime + " seconds.");
        jobPojo.setStarted();

        log.info("We go to sleep for " + sleepTime + " seconds.");
        // snooze
        try {
            for (int i=0 ; i < sleepTime ; i++) {
                jobPojo.debug("Still Sleeping for " + (sleepTime - i) + "seconds.");
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            jobPojo.setStatus(JobStatusConstants.FAILED);
            jobPojo.fatal(e.toString());
            return;
        }

        jobPojo.debug("DemoWorker is finished now.");
        log.info("We are finished :-)");
        jobPojo.setStatus(JobStatusConstants.FINISHED);
        jobPojo.publish();
    }
}
