package uk.ac.ceh.components.userstore;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * The following UserBuilder uses reflection for setting the property values of
 * the user being built in a given Map of property names to fields.
 * @author Christopher Johnson
 */
public class FieldSettingUserBuilder<U extends User> implements UserBuilder<U> {
    private final U user;
    private final Map<String, Field> fields;
    
    /**
     * The following constructor will create a new instance of a user represented
     * by the supplied class and then set the username attribute on this new instance.
     * 
     * If not field exists in the map of fields for the username attribute, this 
     * constructor will fail with an IllegalArgumentException
     * 
     * @param clazz The class of which to user for creating a new user instance of
     * @param username The username of this user
     * @param fields to use for setting the values of UserAttributes of underlying user implementations
     */
    public FieldSettingUserBuilder(Class<U> clazz, String username, Map<String, Field> fields) {
        if(!fields.containsKey(UserAttribute.USERNAME)) {
            throw new IllegalArgumentException("The supplied map of fields did not contain a field for " + UserAttribute.USERNAME);
        }
        
        try {
            this.user = clazz.newInstance();
            this.fields = fields;
            fields.get(UserAttribute.USERNAME).set(user, username);
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public FieldSettingUserBuilder<U> set(String uri, Object value) {
        try {
            if(fields.containsKey(uri)) {
                fields.get(uri).set(user, value); 
            }
            return this;
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public U build() {
        return user;
    }
}
