package uk.ac.ceh.components.userstore.crowd;

/**
 * The following exception is thrown when an interaction with crowd rest api 
 * returns a status code which is not expected or defined in the documentation of
 * the rest api.
 * @author Christopher Johnson
 */
public class CrowdRestException extends RuntimeException {
    public CrowdRestException() {
        super();
    }
    
    public CrowdRestException(String mess) {
        super(mess);
    }
    
    public CrowdRestException(Throwable cause) {
        super(cause);
    }
    
    public CrowdRestException(String mess, Throwable cause) {
        super(mess, cause);
    }
}
