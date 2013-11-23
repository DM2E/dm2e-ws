package eu.dm2e.ws.tests.unit.api;


import eu.dm2e.ws.api.*;
import eu.dm2e.ws.tests.OmnomTestCase;
import org.junit.Test;

import java.util.List;

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

        List<ValidationMessage> res = conn.validate();
        for (ValidationMessage mes:res) {
            log.debug(mes.toString());
        }

        assert(res.size()==1 && res.get(0).getCode()==1);
        conn.setInWorkflow(workflow);
        res = conn.validate();
        for (ValidationMessage mes:res) {
            log.debug(mes.toString());
        }
        assert(res.size()==2 && res.get(1).getCode()==3);

        conn.setFromWorkflow(workflow);
        conn.setFromPosition(pos);
        conn.setFromParam(wp);
        conn.setToParam(param);
        res = conn.validate();
        for (ValidationMessage mes:res) {
            log.debug(mes.toString());
        }
        assert(res.size()==2 && res.get(1).getCode()==5);
        conn.setFromPosition(null);
        conn.setToPosition(pos);
        res = conn.validate();
        for (ValidationMessage mes:res) {
            log.debug(mes.toString());
        }
        assert(res.size()==0);
    }

}
