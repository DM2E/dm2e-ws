/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */

@GrabResolver(name = 'm2.java.net', root = 'http://download.java.net/maven/2')
@GrabResolver(name = 'glassfish.java.net', root = 'http://download.java.net/maven/glassfish')
@GrabResolver(name = 'mvnrepository', root = 'http://repo1.maven.org/maven2')
@GrabResolver(name = 'lski-snapshots', root = 'https://breda.informatik.uni-mannheim.de/nexus/content/repositories/snapshots')
@Grapes([
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


def static doit() {
    Logger log = LoggerFactory.getLogger(getClass().getName());
    def base = "http://localhost:9998/api/"
    def demoUrl = base + "service/demo"
    def workflowServiceUri = base + "workflow"

    WebservicePojo demo = new WebservicePojo(demoUrl);
    WorkflowPojo wf = new WorkflowPojo();
    wf.setLabel("Demo Workflow");
    wf.addPosition(demo);
    wf.autowire();

    wf.publishToService(workflowServiceUri)
    wf.refresh(0,false);
    wf

    WebservicePojo wfService = wf.getWebservice();

    WorkflowPojo outer = new WorkflowPojo();
    outer.addPosition(wfService)
    outer.autowire()
    outer.publishToService(workflowServiceUri)
    outer.refresh(0,false);
    outer

    WebservicePojo outerService = outer.getWebservice();


    WebserviceConfigPojo config = outerService.createConfig();
    Client client = new Client()
    client.publishPojoToConfigService(config);

    log.info("Configuration created for Test: " + config.getTurtle());

    Response response = client.target(outerService.getId()).request().put(Entity.text(config.getId()));
    log.info("JOB STARTED WITH RESPONSE: " + response.getStatus() + " / Location: "
            + response.getLocation() + " / Content: " + response.readEntity(String.class));


    new File("demo.dot").write(outer.getFullDot());

}
