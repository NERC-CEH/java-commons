package uk.ac.ceh.components.userstore;

/**
 * The following interface defines a userstore which can have users added and 
 * removed
 * @author Christopher Johnson
 */
public interface WritableUserStore<U extends User> extends UserStore<U> {
    /**
     * The following method will add a user to the UserStore.
     * @param user User to add
     * @param password This users login password
     * @throws UsernameAlreadyTakenException If a user already exists with that 
     *  username
     * @throws InvalidCredentialsException If the password does not conform to 
     *  this user stores password policy
     */
    void addUser(U user, String password) throws UsernameAlreadyTakenException, InvalidCredentialsException;
    
    /**
     * Takes the modified user and replaces the old one in the userstore with
     *  this one. Requests to obtaining a user from this userstore with a
     *  username matching the users passed into this method will then return 
     *  the updated user
     * @param user The user with updated details to put into this userstore
     * @throws UnknownUserException If no user existed with this users username
     */
    void updateUser(U user) throws UnknownUserException;
    
    /**
     * Removes the user with the given username from the userstore
     * @param username
     * @throws UnknownUserException If no user existed with this users username
     */
    void deleteUser(String username) throws UnknownUserException;
    
    /**
     * The following method will set the given users password to the given password
     * @param username The username of the user to update
     * @param newPassword The new password
     * @throws UnknownUserException If the user is not known
     */
    void setUserPassword(String username, String newPassword) throws UnknownUserException;
}
