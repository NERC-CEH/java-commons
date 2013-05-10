package uk.ac.ceh.components.userstore;

/**
 * The following exception will be thrown if no group exists in the groupmanager with
 * a given name
 * @author Christopher Johnson
 */
public class UnknownGroupException extends Exception {
    public UnknownGroupException() {
        super();
    }
    
    public UnknownGroupException(String mess) {
        super(mess);
    }
    
    public UnknownGroupException(Throwable cause) {
        super(cause);
    }
    
    public UnknownGroupException(String mess, Throwable cause) {
        super(mess, cause);
    }
}