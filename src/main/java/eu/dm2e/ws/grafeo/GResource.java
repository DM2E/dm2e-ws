package eu.dm2e.ws.grafeo;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/5/13
 * Time: 11:22 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GResource extends GValue {
    void rename(String uri);
    String getUri();

    void set(String uri, GValue value);

    boolean isAnon();

    String getAnonId();
}
