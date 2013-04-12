package uk.org.ukeof.db;

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
