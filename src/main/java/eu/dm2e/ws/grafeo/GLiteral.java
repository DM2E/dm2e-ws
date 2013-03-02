package eu.dm2e.ws.grafeo;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/2/13
 * Time: 4:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class GLiteral {
    private String value;
    private Grafeo grafeo;

    public GLiteral(Grafeo grafeo, String value) {
        if (grafeo.isEscaped(value)) {
            this.value = grafeo.unescape(value);
        } else {
            this.value = value;
        }
        this.grafeo = grafeo;
    }

    public String getValue() {
        return value;
    }

    public Grafeo getGrafeo() {
        return grafeo;
    }
}
