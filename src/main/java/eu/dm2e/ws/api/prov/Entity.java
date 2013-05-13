package eu.dm2e.ws.api.prov;

import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 4/16/13
 * Time: 8:48 PM
 * To change this template use File | Settings | File Templates.
 */
@Namespaces({"prov", "http://www.w3.org/ns/prov#"})
@RDFClass("prov:Entity")
public class Entity {
    @RDFProperty("prov:wasGeneratedBy")
    Activity wasGeneratedBy;
    @RDFProperty("prov:wasUsedBy")
    Activity wasUsedBy;
    @RDFProperty("prov:wasDerivedFrom")
    Entity wasDerivedFrom;
    @RDFProperty("prov:wasAttributedTo")
    Agent wasAttributedTo;
    @RDFProperty("prov:generatedAtTime")
    Calendar generatedAtTime;
    @RDFProperty("prov:specializationOf")
    Entity specializationOf;
    @RDFProperty("prov:alternateOf")
    Entity alternateOf;

    public Activity getWasGeneratedBy() {
        return wasGeneratedBy;
    }

    public void setWasGeneratedBy(Activity wasGeneratedBy) {
        this.wasGeneratedBy = wasGeneratedBy;
    }

    public Activity getWasUsedBy() {
        return wasUsedBy;
    }

    public void setWasUsedBy(Activity wasUsedBy) {
        this.wasUsedBy = wasUsedBy;
    }

    public Entity getWasDerivedFrom() {
        return wasDerivedFrom;
    }

    public void setWasDerivedFrom(Entity wasDerivedFrom) {
        this.wasDerivedFrom = wasDerivedFrom;
    }

    public Agent getWasAttributedTo() {
        return wasAttributedTo;
    }

    public void setWasAttributedTo(Agent wasAttributedTo) {
        this.wasAttributedTo = wasAttributedTo;
    }

    public Calendar getGeneratedAtTime() {
        return generatedAtTime;
    }

    public void setGeneratedAtTime(Calendar generatedAtTime) {
        this.generatedAtTime = generatedAtTime;
    }

    public Entity getSpecializationOf() {
        return specializationOf;
    }

    public void setSpecializationOf(Entity specializationOf) {
        this.specializationOf = specializationOf;
    }

    public Entity getAlternateOf() {
        return alternateOf;
    }

    public void setAlternateOf(Entity alternateOf) {
        this.alternateOf = alternateOf;
    }
}
