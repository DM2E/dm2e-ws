package eu.dm2e.ws.tests.unit.api;


import eu.dm2e.ws.api.*;
import eu.dm2e.ws.tests.OmnomTestCase;
import org.junit.Test;

import java.util.List;

public class WorkflowPositionPojoTest extends OmnomTestCase {

    @Test
    public void testValidate() {
        WorkflowPositionPojo pos = new WorkflowPositionPojo();
        List<ValidationMessage> res = pos.validate();
        for (ValidationMessage mes:res) {
            log.debug(mes.toString());
        }
        assert(res.size()==2 && res.get(0).getCode()==1 && res.get(1).getCode()==2);
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
        for (ValidationMessage mes:res) {
            log.debug(mes.toString());
        }
        assert(res.size()==1 && res.get(0).getCode()==3);
        workflow.addConnectorFromWorkflowToPosition("wp1",pos,"p1");
        res = pos.validate();
        assert(res.size()==0);
    }

}
