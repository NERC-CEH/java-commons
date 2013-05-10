package uk.ac.ceh.components.userstore;

/**
 * The following interface defines a builder for users. The interface works in a 
 * similar way to a map in that properties of a user can be set based on a given
 * {@link UserAttribute} key.
 * @author Christopher Johnson
 */
public interface UserBuilder<U> {
    /**
     * Sets the given value for the given property on the user which is being 
     * built by this UserBuilder.
     * 
     * It is important to note that this interface shouldn't fail if the given 
     * uri is not supported by the <U> which is being built. In cases such as this
     * the value for that property should simply be ignored.
     * 
     * @param uri The {@link UserAttribute} key to set the value of
     * @param value to set for the given user attribute
     * @return this user builder 
     * @throws UserBuilderException if the UserAttribute exists but could not be 
     *  set on the user being built
     */
    UserBuilder<U> set(String uri, Object value) throws UserBuilderException;
    
    /**
     * @return The new instance of the built object
     * @throws UserBuilderException if the user could not be built
     */
    U build() throws UserBuilderException;
}
