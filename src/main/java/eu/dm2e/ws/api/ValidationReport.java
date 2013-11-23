package eu.dm2e.ws.api;

import eu.dm2e.grafeo.gom.SerializablePojo;

import java.util.ArrayList;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
 */
public class ValidationReport extends ArrayList<ValidationMessage> {
    SerializablePojo validated;

    public ValidationReport(SerializablePojo validated) {
        this.validated=validated;
    }

    public SerializablePojo getValidated() {
        return validated;
    }

    public void setValidated(SerializablePojo validated) {
        this.validated = validated;
    }

    public boolean valid() {
        return size()==0;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Validation Report for: ").append(validated.toString()).append("\n");
        sb.append("Result: ");
        if (valid()) {
            sb.append("valid\n");
        } else {
            sb.append("Not valid, number of messages: ").append(size()).append("\n");
        }

        for (ValidationMessage mes:this) {
            sb.append(mes).append("\n");
        }
        return sb.toString();
    }
}
