package eu.dm2e.ws.api;

import eu.dm2e.grafeo.gom.SerializablePojo;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
 */
public class ValidationMessage {

	// TODO define these codes
    int code;
    SerializablePojo protester;
    String message;

    public ValidationMessage(SerializablePojo protester, int code, String message) {
        this.code = code;
        this.protester = protester;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public SerializablePojo getProtester() {
        return protester;
    }

    public void setProtester(SerializablePojo protester) {
        this.protester = protester;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (protester!=null) {
            sb.append(protester.getClass().getSimpleName().toUpperCase());
        }
        if (protester!=null && code!=0) sb.append("-");
        if (code!=0) sb.append(code);
        if (sb.length()>0) sb.append(": ");
        sb.append(message);
        if (protester!=null && protester.getId()!=null) {
            sb.append(" (").append(protester.getId()).append(")");
        }
        return sb.toString();
    }
}
