package uk.ac.ceh.components.userstore;

/**
 * The following interface defines the factory for obtaining user builder 
 * instances
 * @author Christopher Johnson
 */
public interface UserBuilderFactory<B extends UserBuilder<?>> {
    /**
     * The following method will return a user builder which is capable of 
     * generating a UserBuilder which will generate users of type U
     * @param username The username to use
     * @return A new UserBuilder of type B
     */
     B newUserBuilder(String username);
}