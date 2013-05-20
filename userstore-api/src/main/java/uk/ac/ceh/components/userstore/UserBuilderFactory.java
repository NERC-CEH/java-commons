package uk.ac.ceh.components.userstore;

import java.lang.reflect.Type;

/**
 * The following interface defines the factory for obtaining user builder 
 * instances
 * @author Christopher Johnson
 */
public interface UserBuilderFactory<U> {
    /**
     * Determines the desired Type for some uri which userbuilders of this factory
     * may want have set when using {@link UserBuilder#set(java.lang.String, java.lang.Object) }
     * 
     * This method should return null if any type is appropriate.
     * @param uri the uri to determine the desired type of
     * @return The type if one is desired, or null
     */
    Type getDesiredTypeForURI(String uri);
    
    /**
     * The following method will return a user builder which is capable of 
     * generating a UserBuilder which will generate users of type U
     * @param username The username to use
     * @return A new UserBuilder of type B
     */
     <B extends UserBuilder<U>> B newUserBuilder(String username);
}
