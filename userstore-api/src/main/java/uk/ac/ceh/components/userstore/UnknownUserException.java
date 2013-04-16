package uk.ac.ceh.components.userstore;

/**
 * The following exception will be thrown if no user exists in the userstore with
 * a given username
 * @author Christopher Johnson
 */
public class UnknownUserException extends Exception {
    public UnknownUserException() {
        super();
    }
    
    public UnknownUserException(String mess) {
        super(mess);
    }
    
    public UnknownUserException(Throwable cause) {
        super(cause);
    }
    
    public UnknownUserException(String mess, Throwable cause) {
        super(mess, cause);
    }
}