package uk.ac.ceh.components.tokengeneration;

/**
 * The following exception will be thrown if an invalid token is attempted to be
 * used to obtain a user
 * @author Christopher Johnson
 */
public class InvalidTokenException extends Exception {
    public InvalidTokenException() {
        super();
    }
    
    public InvalidTokenException(String mess) {
        super(mess);
    }
    
    public InvalidTokenException(Throwable cause) {
        super(cause);
    }
    
    public InvalidTokenException(String mess, Throwable cause) {
        super(mess, cause);
    }
}
