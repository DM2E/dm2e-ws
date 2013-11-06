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
import eu.dm2e.ws.api.WebservicePojo
import eu.dm2e.ws.api.WorkflowPositionPojo
import eu.dm2e.ws.api.WorkflowPojo
import eu.dm2e.ws.services.xslt.XsltService
import eu.dm2e.ws.services.publish.PublishService
import eu.dm2e.ws.api.ParameterConnectorPojo





def static doit() {
    def base = "http://localhost:9998/api/"
    def xsltURI = base + "service/xslt"
    def publishURI = base + "publish"






    WorkflowPojo wf = createWorkflow(xsltURI, publishURI, "http://localhost:9998/api/workflow")
    WorkflowPojo wf2 = createWorkflow(xsltURI, publishURI, "http://localhost:9998/api/workflow")
    WorkflowPojo wf3 = new WorkflowPojo();
    WorkflowPositionPojo pos1 = wf3.addPosition(wf.getWebservice());
    WorkflowPositionPojo pos2 = wf3.addPosition(wf2.getWebservice());
    wf3.addInputParameter("Input1").setIsRequired(true);
    wf3.addInputParameter("Input2").setIsRequired(true);
    wf3.addOutputParameter("Output").setIsRequired(true);
    WebservicePojo ws = wf.getWebservice();
    ws.loadFromURI(ws.getId());
    println(ws.getTerseTurtle())
    wf3.addConnectorFromPositionToPosition(pos1,"outputGraph",pos2,"inputXML");
    wf3.addConnectorFromWorkflowToPosition("Input1",pos1,"inputXML");
    wf3.addConnectorFromPositionToWorkflow(pos2,"outputGraph","Output");

    new File("test2.dot").write(wf3.getFullDot());

}

def static createWorkflow(String xsltServiceUri, String publishServiceUri, String workflowServiceUri) {

    final String _ws_label = "XML -> XMLRDF -> DM2E";
    final String _ws_param_provider = "providerID";
    final String _ws_param_xmlinput = "inputXML";
    final String _ws_param_xsltinput = "inputXSLT";
    final String _ws_param_outgraph = "outputGraph";
    final String _ws_param_datasetLabel = "datasetLabel";
    final String _ws_pos1_label = "XML -> XMLRDF";
    final String _ws_pos2_label = "XMLRDF -> Graphstore";
    final String _ws_param_datasetID = "datasetID";


    WebservicePojo xslt = new WebservicePojo(xsltServiceUri);
    WebservicePojo publish = new WebservicePojo(publishServiceUri)
    WorkflowPojo wf = new WorkflowPojo();
    wf.setLabel(_ws_label);
    wf.addInputParameter(_ws_param_xmlinput).setIsRequired(true);
    wf.addInputParameter(_ws_param_provider).setIsRequired(true);
    wf.addInputParameter(_ws_param_xsltinput).setIsRequired(true);
    wf.addInputParameter(_ws_param_datasetLabel).setIsRequired(true);
    wf.addInputParameter(_ws_param_datasetID).setIsRequired(true);
    wf.addOutputParameter(_ws_param_outgraph);

    WorkflowPositionPojo step1_pos = wf.addPosition(xslt);


    WorkflowPositionPojo step2_pos = wf.addPosition(publish);

    // workflow:inputXML => xmlrdf:xmlinput
    wf.addConnectorFromWorkflowToPosition(
            _ws_param_xmlinput,
            step1_pos,
            XsltService.PARAM_XML_IN);

    // workflow:inputXSLT => xmlrdf:xsltinput
    wf.addConnectorFromWorkflowToPosition(
            _ws_param_xsltinput,
            step1_pos,
            XsltService.PARAM_XSLT_IN);

    // workflow:datasetID => publish:dataset-id
    ParameterConnectorPojo x = wf.addConnectorFromWorkflowToPosition(
            _ws_param_datasetID,
            step2_pos,
            PublishService.PARAM_DATASET_ID);

    // workflow:providerID => publish:providerID
    wf.addConnectorFromWorkflowToPosition(
            _ws_param_provider,
            step2_pos,
            PublishService.PARAM_PROVIDER_ID);

    // workflow:label => publish:label
    wf.addConnectorFromWorkflowToPosition(
            _ws_param_datasetLabel,
            step2_pos,
            PublishService.PARAM_LABEL);

    // xmlrdf:xmloutput => publish:to-publish
    wf.addConnectorFromPositionToPosition(
            step1_pos,
            XsltService.PARAM_XML_OUT,
            step2_pos,
            PublishService.PARAM_TO_PUBLISH);

    // publish:result-dataset-id => workfow:outputGraph
    wf.addConnectorFromPositionToWorkflow(
            step2_pos,
            PublishService.PARAM_RESULT_DATASET_ID,
            _ws_param_outgraph);

    def loc = wf.publishToService(workflowServiceUri)
    wf.loadFromURI(loc);
    wf
}