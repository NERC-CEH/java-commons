package uk.ac.ceh.components.datastore;

/**
 *
 * @author cjohn
 */
public class DataRepositoryException extends Exception {

    public DataRepositoryException(String mess) {
        super(mess);
    }

    public DataRepositoryException(Throwable ex) {
        super(ex);
    }
}
