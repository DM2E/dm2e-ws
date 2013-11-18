/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */

@Grapes([
@GrabResolver(name = 'm2.java.net', root = 'http://download.java.net/maven/2') ,
@GrabResolver(name = 'glassfish.java.net', root = 'http://download.java.net/maven/glassfish'),
@GrabResolver(name = 'mvnrepository', root = 'http://repo1.maven.org/maven2'),
@GrabResolver(name = 'lski-snapshots', root = 'https://breda.informatik.uni-mannheim.de/nexus/content/repositories/snapshots'),
@GrabExclude(group = 'org.eclipse.jetty.orbit', module = 'javax.servlet', version = '3.0.0.v201112011016'),
@GrabExclude(group = 'org.eclipse.jetty', module = '*', version = '*'),
@Grab(group = 'eu.dm2e.grafeo', module = 'grafeo', version = '1.0-SNAPSHOT'),
@Grab(group = 'eu.dm2e.ws', module = 'dm2e-ws', version = '1.0-SNAPSHOT', changing=true)
])
import eu.dm2e.grafeo.Grafeo
import eu.dm2e.grafeo.jena.GrafeoImpl
import eu.dm2e.ws.api.ParameterPojo
import eu.dm2e.ws.api.WebserviceConfigPojo
import eu.dm2e.ws.api.WebservicePojo
import eu.dm2e.ws.api.WorkflowPojo
import eu.dm2e.ws.services.Client
import javax.ws.rs.core.Response
import javax.ws.rs.client.Entity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import eu.dm2e.ws.services.demo.IteratorService
import eu.dm2e.ws.api.WorkflowPositionPojo
import eu.dm2e.ws.services.demo.DemoService
import javax.jws.WebService
import eu.dm2e.ws.api.JobPojo
import eu.dm2e.utils.Misc
import eu.dm2e.ws.services.iteration.ZipIteratorService
import eu.dm2e.ws.services.xslt.XsltZipService
import eu.dm2e.ws.services.publish.PublishService

def static doit() {
    Logger log = LoggerFactory.getLogger(getClass().getName());
    def base = "http://localhost:9998/api/"
    def demoUrl = base + "service/demo"
    def iteratorUrl = base + "service/zip-iterator"
    def zipxsltUrl = base + "service/xslt-zip"
    def publishUrl = base + "publish"
    def workflowServiceUri = base + "workflow"

    // WebservicePojo demo = new WebservicePojo(demoUrl);
    WebservicePojo iterator = new WebservicePojo(iteratorUrl);
    WebservicePojo xslt = new WebservicePojo(zipxsltUrl);
    WebservicePojo publish = new WebservicePojo(publishUrl);
    WorkflowPojo wf = new WorkflowPojo();
    wf.setLabel("Iterator Workflow");
    WorkflowPositionPojo iterPos = wf.addPosition(iterator);
    WorkflowPositionPojo xsltPos = wf.addPosition(xslt);
    WorkflowPositionPojo pubPos = wf.addPosition(publish);
    // WorkflowPositionPojo demoPos = wf.addPosition(demo);
    wf.addConnectorFromPositionToPosition(iterPos,ZipIteratorService.PARAM_OUTFILE,xsltPos, XsltZipService.PARAM_XML_IN);
    wf.addConnectorFromPositionToPosition(xsltPos,XsltZipService.PARAM_XML_OUT,pubPos, PublishService.PARAM_TO_PUBLISH);
    // wf.addConnectorFromWorkflowToPosition(XsltZipService.PARAM_XML_OUT,pubPos, PublishService.PARAM_TO_PUBLISH);

    wf.autowire(true);


    wf.publishToService(workflowServiceUri)
    wf.refresh(0,false);
    wf

    WebservicePojo wfService = wf.getWebservice();
    WebserviceConfigPojo config = wfService.createConfig();
    config.addParameterAssignment("archive","https://dl.dropboxusercontent.com/s/e664vf3dk2se03n/ziptest.zip?dl=1&token_hash=AAHPDThbzDMqUeqEOGmL1wn-5lBylBpyFHrT4ddhNjqIJg")
    config.addParameterAssignment("xsltZipInput","https://dl.dropboxusercontent.com/s/ojujplva8mrt2jc/TEI2DM2E_xslt_20130605.zip?dl=1&token_hash=AAGEDLlGnegVJUTeNQsKnXy6x-JwGjJYeC6zAs7l1Z71Fw")
    config.addParameterAssignment("label","Test")
    config.addParameterAssignment("provider-id","Provider")
    config.addParameterAssignment("dataset-id","Dataset")
    Client client = new Client()

    client.publishPojoToConfigService(config);

    log.info("Configuration created for Test: " + config.getTurtle());

    Response response = client.target(wfService.getId()).request().put(Entity.text(config.getId()));

     log.info("JOB STARTED WITH RESPONSE: " + response.getStatus() + " / Location: "
           + response.getLocation() + " / Content: " + response.readEntity(String.class));

    JobPojo job = new JobPojo();
    job.loadFromURI(response.getLocation());
    while (job.isStillRunning()) {
        Thread.sleep(2000)
        job.refresh(0,true)
        println(job.getJobStatus() + ": " + job.getLatestResult())

    }
    println(Misc.output(job.getOutputParameterAssignments()))

    new File("fullxslt.dot").write(wf.getFullDot());

}
