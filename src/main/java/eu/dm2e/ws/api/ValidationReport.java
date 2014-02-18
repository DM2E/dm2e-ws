package eu.dm2e.ws.api;

import java.util.ArrayList;

import eu.dm2e.grafeo.gom.SerializablePojo;

/**
 * A collection of messages on the validity of a Pojo.o
 * 
 * @author Kai Eckert
 */
@SuppressWarnings("serial")
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

    public void addMessage(SerializablePojo protester, int code, String message) {
        add(new ValidationMessage(protester, code, message));
    }

    public boolean containsMessage(Class protesterClass, int code) {
        for (ValidationMessage mes:this) {
            if (mes.getProtester().getClass().equals(protesterClass) && mes.getCode()==code) return true;
        }
        return false;
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
