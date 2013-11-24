package eu.dm2e.ws.tests.unit.api;


import eu.dm2e.ws.api.*;
import eu.dm2e.ws.tests.OmnomTestCase;
import org.junit.Test;

public class WorkflowPositionPojoTest extends OmnomTestCase {

    @Test
    public void testValidate() {
        WorkflowPositionPojo pos = new WorkflowPositionPojo();
        ValidationReport res = pos.validate();
        log.debug(res.toString());
        assert(res.containsMessage(WorkflowPositionPojo.class, 1));
        assert(res.containsMessage(WorkflowPositionPojo.class, 2));
        WebservicePojo ws = new WebservicePojo();
        ParameterPojo param = ws.addInputParameter("p1");
        param.setId("p1");
        param.setIsRequired(true);
        pos.setWebservice(ws);
        WorkflowPojo workflow = new WorkflowPojo();
        workflow.addPosition(pos);
        pos.setWorkflow(workflow);
        ParameterPojo wp = workflow.addInputParameter("wp1");
        res = pos.validate();
        log.debug(res.toString());
        assert(res.containsMessage(WorkflowPositionPojo.class, 3));
        workflow.addConnectorFromWorkflowToPosition("wp1",pos,"p1");
        res = pos.validate();
        assert(res.valid());
    }

}
