package eu.dm2e.ws.services.demo;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.services.AbstractTransformationService;

import javax.ws.rs.Path;

/**
 * DemoService shows the necessary steps / best practices to create a webservice.
 */
@Path("/service/demo")
public class DemoService extends AbstractTransformationService {

	public static final String PARAM_SLEEPTIME = "sleeptime";
	public static final String PARAM_COUNTDOWN_PHRASE = "countdownPhrase";
	public static final String PARAM_RANDOM_OUTPUT = "randomOutput";

    public DemoService() {
        final WebservicePojo ws = getWebServicePojo();
        ws.setLabel("Demo");

		ParameterPojo sleeptimeParam = ws.addInputParameter(PARAM_SLEEPTIME);
        sleeptimeParam.setParameterType(NS.XSD.INT);
        sleeptimeParam.setDefaultValue("3");
        sleeptimeParam.setIsRequired(false);

        ParameterPojo countdownPhraseParam = ws.addInputParameter(PARAM_COUNTDOWN_PHRASE);
        countdownPhraseParam.setDefaultValue("bottles of beer on the wall.");
        countdownPhraseParam.setIsRequired(false);

        ParameterPojo randomOutputParam = ws.addOutputParameter(PARAM_RANDOM_OUTPUT);
        randomOutputParam.setIsRequired(false);
    }

    @Override
    public void run() {
    	JobPojo jobPojo = getJobPojo();
    	try {
    		jobPojo.debug("DemoWorker starts to run now.");
    		WebserviceConfigPojo wsConf = jobPojo.getWebserviceConfig();
    		jobPojo.debug("wsConf: " + wsConf);

    		int sleepTime = 0;
    		jobPojo.debug("sleeptime: " + wsConf.getParameterValueByName(PARAM_SLEEPTIME));
    		jobPojo.debug("countdownPhrase: " + wsConf.getParameterValueByName(PARAM_COUNTDOWN_PHRASE));

    		sleepTime = Integer.parseInt(jobPojo.getInputParameterValueByName(PARAM_SLEEPTIME));
    		log.info("Parsed sleeptime: " + sleepTime);

    		String countdownPhrase = jobPojo.getInputParameterValueByName(PARAM_COUNTDOWN_PHRASE);

    		jobPojo.debug("DemoWorker will sleep for " + sleepTime + " seconds.");
    		jobPojo.setStarted();

    		// snooze
    		for (int i=0 ; i < sleepTime ; i++) {
    			jobPojo.info((sleepTime - i) + " " + countdownPhrase);
    			//                jobPojo.trace("Still Sleeping for " + (sleepTime - i) + " seconds.");
    			Thread.sleep(1000);
    		}


    		jobPojo.debug("DemoWorker is finished now.");
    		jobPojo.setStatus(JobStatus.FINISHED);
    	} catch (Exception e) {
    		jobPojo.setStatus(JobStatus.FAILED);
    		jobPojo.fatal(e.toString());
    		throw new RuntimeException(e);
    	} finally {
    		// client.publishPojoToJobService(jobPojo);
    	}
    }


}
