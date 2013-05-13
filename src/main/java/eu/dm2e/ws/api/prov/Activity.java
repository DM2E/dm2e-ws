package eu.dm2e.ws.api.prov;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 4/16/13
 * Time: 8:49 PM
 * To change this template use File | Settings | File Templates.
 */

@Namespaces({"prov", "http://www.w3.org/ns/prov#"})
@RDFClass("prov:Activity")
public class Activity {
    @RDFProperty("prov:used")
    Entity used;
    @RDFProperty("prov:generated")
    Entity generated;
    @RDFProperty("prov:wasAssociatedWith")
    Agent wasAssociatedWith;
    @RDFProperty("prov:endedAtTime")
    Calendar endedAtTime;
    @RDFProperty("prov:startedAtTime")
    Calendar startedAtTime;

    public Entity getUsed() {
        return used;
    }

    public void setUsed(Entity used) {
        this.used = used;
    }

    public Entity getGenerated() {
        return generated;
    }

    public void setGenerated(Entity generated) {
        this.generated = generated;
    }

    public Agent getWasAssociatedWith() {
        return wasAssociatedWith;
    }

    public void setWasAssociatedWith(Agent wasAssociatedWith) {
        this.wasAssociatedWith = wasAssociatedWith;
    }

    public Calendar getEndedAtTime() {
        return endedAtTime;
    }

    public void setEndedAtTime(Calendar endedAtTime) {
        this.endedAtTime = endedAtTime;
    }

    public Calendar getStartedAtTime() {
        return startedAtTime;
    }

    public void setStartedAtTime(Calendar startedAtTime) {
        this.startedAtTime = startedAtTime;
    }
}
