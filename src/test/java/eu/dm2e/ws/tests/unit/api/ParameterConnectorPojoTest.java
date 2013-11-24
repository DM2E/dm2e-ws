package eu.dm2e.ws.tests.unit.api;


import eu.dm2e.ws.api.*;
import eu.dm2e.ws.tests.OmnomTestCase;
import org.junit.Test;

public class ParameterConnectorPojoTest extends OmnomTestCase {

    @Test
    public void testValidate() {
        WebservicePojo ws = new WebservicePojo();
        ParameterPojo param = ws.addInputParameter("p1");
        param.setId("p1");
        param.setIsRequired(true);

        WorkflowPojo workflow = new WorkflowPojo();
        workflow.setId("workflow1");

        WorkflowPositionPojo pos = new WorkflowPositionPojo();
        ParameterPojo wp = workflow.addInputParameter("wp1");
        pos.setWebservice(ws);
        pos.setWorkflow(workflow);
        workflow.addPosition(pos);

        ParameterConnectorPojo conn = new ParameterConnectorPojo();

        ValidationReport res = conn.validate();
        log.debug(res.toString());
        assert(res.containsMessage(ParameterConnectorPojo.class, 1));
        conn.setInWorkflow(workflow);
        res = conn.validate();
        log.debug(res.toString());
        assert(res.containsMessage(ParameterConnectorPojo.class, 3));

        conn.setFromWorkflow(workflow);
        conn.setFromPosition(pos);
        conn.setFromParam(wp);
        conn.setToParam(param);
        res = conn.validate();
        log.debug(res.toString());
        assert(res.containsMessage(ParameterConnectorPojo.class, 5));
        conn.setFromPosition(null);
        conn.setToPosition(pos);
        res = conn.validate();
        log.debug(res.toString());
        assert(res.valid());
    }

}
