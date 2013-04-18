package uk.ac.ceh.components.userstore;

/**
 * The following exception will be thrown if a user is attempted to be added to
 * a user store but that users username is already taken
 * @author Christopher Johnson
 */
public class UsernameAlreadyTakenException extends Exception {
    public UsernameAlreadyTakenException() {
        super();
    }
    
    public UsernameAlreadyTakenException(String mess) {
        super(mess);
    }
    
    public UsernameAlreadyTakenException(Throwable cause) {
        super(cause);
    }
    
    public UsernameAlreadyTakenException(String mess, Throwable cause) {
        super(mess, cause);
    }
}