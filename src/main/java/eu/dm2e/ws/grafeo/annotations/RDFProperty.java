package eu.dm2e.ws.grafeo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This field should represents a RDF property.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RDFProperty {
    String value();
    /**
     * @return true if this property should be serialized as a reference URI only
     */
    boolean serializeAsURI() default false;
    String itemPrefix() default "item";
    /**
     * @return true if it is the inverse of the named relation (value()) that this relation represents
     */
    boolean inverse() default false;
}
