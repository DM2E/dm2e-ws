package eu.dm2e.ws.httpmethod;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.HttpMethod;

/**
 * This allows annotating methods responding to PATCH requests for partially
 * updating resources. 
 * @see http://tools.ietf.org/html/rfc5789
 * @author Konstantin Baierer
 *
 * @deprecated This is cool but not standardized enough to make use of.
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("PATCH")
public @interface PATCH { }
