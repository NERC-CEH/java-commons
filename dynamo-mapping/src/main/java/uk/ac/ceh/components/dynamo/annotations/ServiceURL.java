package uk.ac.ceh.components.dynamo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used in provided methods to obtain the url which the
 * provided method is operating on.
 * @author Christopher Johnson
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceURL {}
