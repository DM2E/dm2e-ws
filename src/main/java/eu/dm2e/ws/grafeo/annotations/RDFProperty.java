package eu.dm2e.ws.grafeo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/31/13
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RDFProperty {
    String value();
    String itemPrefix() default "item";
    /**
     * @return true if it is the inverse of the named relation (value()) that this relation represents
     */
    boolean inverse() default false;
}
