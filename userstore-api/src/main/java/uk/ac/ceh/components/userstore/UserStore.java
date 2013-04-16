package uk.ac.ceh.components.userstore;

/**
 * The following interface defines the method which a user repository must be
 * capable of fulfilling in order to be a used by other components in the 
 * ceh.components project
 * @author Christopher Johnson
 */
public interface UserStore<U extends User, B extends UserBuilder<U>> {
    /**
     * The following method will return the user associated with the given username
     * @param username
     * @return The User if the username exists
     * @throws UnknownUserException if the username does not resolve to a user in
     *  this userstore
     */
    U getUser(String username) throws UnknownUserException;
    
    /**
     * The following method will return the user associated with the given userId
     * @param username
     * @return The User if the userId exists
     * @throws UnknownUserException if the username does not resolve to a user in
     *  this userstore
     */
    U getUser(int userId) throws UnknownUserException;
    
    /**
     * The following method will return the boolean state on whether or not a user
     * with this username exists in this UserStore
     * @param username
     * @return true if a user exists, false otherwise 
     */
    boolean userExists(String username);
    
    /**
     * The following method will check that a username with the given password
     * is valid for this user store
     * @param username The username of the user to be logged in
     * @param password The password of the user to be logged in
     * @return The user associated to the username
     * @throws InvalidCredentialsException if the username or password are incorrect
     */
    U authenticate(String username, String password) throws InvalidCredentialsException;
    
    /**
     * This method will return a UserBuilder implementation which is capable of
     * delegating the creation of Users who are not in this UserStore to dependant
     * components.
     * @return A UserBuilder which can build Users of type U
     */
    B getPhantomUserBuilder();
}
