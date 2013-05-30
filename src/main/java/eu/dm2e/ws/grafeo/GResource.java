package eu.dm2e.ws.grafeo;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public interface GResource extends GValue {
    void rename(String uri);
    String getUri();

    void set(String uri, GValue value);

    boolean isAnon();

    String getAnonId();
}
