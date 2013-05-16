package uk.ac.ceh.components.userstore.crowd;

import uk.ac.ceh.components.userstore.crowd.model.CrowdErrorResponse;

/**
 * The following exception is thrown when an interaction with crowd rest api 
 * returns a status code which is not expected or defined in the documentation of
 * the rest api.
 * @author Christopher Johnson
 */
public class CrowdRestException extends RuntimeException {   
    CrowdRestException(String mess) {
        super(mess);
    }
    
    CrowdRestException(CrowdErrorResponse serverResponse) {
        super(serverResponse.getMessage());
    }
}
