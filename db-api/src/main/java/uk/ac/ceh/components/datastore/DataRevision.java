package uk.ac.ceh.components.datastore;

/**
 *
 * @author cjohn
 */
public interface DataRevision<A extends DataAuthor> {
    String getRevisionID();
    String getMessage();
    String getShortMessage();
    A getAuthor();
}
