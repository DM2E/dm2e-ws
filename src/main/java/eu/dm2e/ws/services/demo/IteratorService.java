package eu.dm2e.ws.services.demo;

import java.util.StringTokenizer;

import javax.ws.rs.Path;

import eu.dm2e.NS;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.services.AbstractTransformationService;

/**
 * DemoService shows the necessary steps / best practices to create a webservice.
 */
@Path("/service/iterator")
public class IteratorService extends AbstractTransformationService {

	public static final String PARAM_SLEEPTIME = "sleeptime";
	public static final String PARAM_COUNTDOWN_LIST = "countdownList";
	public static final String PARAM_PHRASE = "countdownPhrase";

    public IteratorService() {
        final WebservicePojo ws = getWebServicePojo();
        ws.setLabel("Iterator");

		ParameterPojo sleeptimeParam = ws.addInputParameter(PARAM_SLEEPTIME);
        sleeptimeParam.setParameterType(NS.XSD.INT);
        sleeptimeParam.setDefaultValue("3");
        sleeptimeParam.setIsRequired(false);

        ParameterPojo countdownListParam = ws.addInputParameter(PARAM_COUNTDOWN_LIST);
        countdownListParam.setDefaultValue("beer,wine,whisky,rum");
        countdownListParam.setIsRequired(false);
        countdownListParam.setComment("Comma separated list of things on the wall ;-)");

        ParameterPojo randomOutputParam = ws.addOutputParameter(PARAM_PHRASE);
        randomOutputParam.setIsRequired(true);
        randomOutputParam.setHasIterations(true);
    }

    @Override
    public void run() {
    	JobPojo jobPojo = getJobPojo();
    	try {
    		jobPojo.debug("Iterator starts to run now.");
    		WebserviceConfigPojo wsConf = jobPojo.getWebserviceConfig();
    		jobPojo.debug("wsConf: " + wsConf);

    		int sleepTime = 0;
    		jobPojo.debug("sleeptime: " + wsConf.getParameterValueByName(PARAM_SLEEPTIME));
    		jobPojo.debug("countdownList: " + wsConf.getParameterValueByName(PARAM_COUNTDOWN_LIST));

    		sleepTime = Integer.parseInt(jobPojo.getInputParameterValueByName(PARAM_SLEEPTIME));
    		log.info("Parsed sleeptime: " + sleepTime);

    		String countdownList = jobPojo.getInputParameterValueByName(PARAM_COUNTDOWN_LIST);

    		jobPojo.debug("Iterator will sleep for " + sleepTime + " seconds.");
    		jobPojo.setStarted();


            StringTokenizer st = new StringTokenizer(countdownList,",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                jobPojo.addOutputParameterAssignment(PARAM_PHRASE, "bottles of " + token + " on the wall.");
                jobPojo.iterate();
    			Thread.sleep(1000*sleepTime);
    		}


    		jobPojo.debug("Iterator is finished now.");
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
