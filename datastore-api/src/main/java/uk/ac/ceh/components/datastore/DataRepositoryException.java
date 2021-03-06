package uk.ac.ceh.components.datastore;

import java.io.IOException;

/**
 *
 * @author cjohn
 */
public class DataRepositoryException extends IOException {

    public DataRepositoryException(String mess) {
        super(mess);
    }

    public DataRepositoryException(Throwable ex) {
        super(ex);
    }
    
    public DataRepositoryException(String mess, Throwable ex) {
        super(mess, ex);
    }
}
