package uk.ac.ceh.components.userstore.springsecurity;

import java.lang.annotation.*;

/**
 * Annotation which flags a user to be injected into a method
 * @author Christopher Johnson
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ActiveUser {}
