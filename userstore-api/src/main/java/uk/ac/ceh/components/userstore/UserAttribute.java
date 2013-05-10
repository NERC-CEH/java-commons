package uk.ac.ceh.components.userstore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author CJOHN
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserAttribute {
    public static final String EMAIL = "ceh:user:email";
    public static final String USERNAME = "ceh:user:username";
    
    
    String value();
}