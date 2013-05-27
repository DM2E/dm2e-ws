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
        jobPojo.debug("DemoWorker starts to run now.");
        WebserviceConfigPojo wsConf = jobPojo.getWebserviceConfig();
        jobPojo.debug("wsConf: " + wsConf);

        int sleepTime = 0;
        jobPojo.debug(wsConf.getParameterValueByName("sleeptime"));

        try {
            sleepTime = Integer.parseInt(wsConf.getParameterValueByName("sleeptime"));
        } catch(Exception e) {
            jobPojo.warn("Exception occured!: " + e);
        }

        jobPojo.debug("DemoWorker will sleep for " + sleepTime + " seconds.");
        jobPojo.setStarted();

        // snooze
        try {
            for (int i=0 ; i < sleepTime ; i++) {
                jobPojo.debug("Still Sleeping for " + (sleepTime - i) + " seconds.");
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            jobPojo.setStatus(JobStatusConstants.FAILED);
            jobPojo.fatal(e.toString());
            return;
        }

        jobPojo.debug("DemoWorker is finished now.");
        jobPojo.setStatus(JobStatusConstants.FINISHED);
        jobPojo.publish();
    }
}
