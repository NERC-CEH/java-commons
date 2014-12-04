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
    public static final String DISPLAY_NAME = "ceh:user:displayname";
    public static final String FIRSTNAME = "ceh:user:firstname";
    public static final String LASTNAME = "ceh:user:lastname";
    public static final String PHONE_NUMBER = "ceh:user:phonenumber";
    public static final String ACTIVE = "ceh:user:active";
    
    String value();
}
