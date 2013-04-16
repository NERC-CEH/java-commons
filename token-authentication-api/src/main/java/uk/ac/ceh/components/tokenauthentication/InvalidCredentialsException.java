package uk.ac.ceh.components.tokenauthentication;

/**
 * The following exception will be thrown if invalid credentials are specified
 * when attempting to generate a Token
 * @author Christopher Johnson
 */
public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException() {
        super();
    }
    
    public InvalidCredentialsException(String mess) {
        super(mess);
    }
    
    public InvalidCredentialsException(Throwable cause) {
        super(cause);
    }
    
    public InvalidCredentialsException(String mess, Throwable cause) {
        super(mess, cause);
    }
}