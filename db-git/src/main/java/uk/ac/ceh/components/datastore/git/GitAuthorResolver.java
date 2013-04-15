package uk.ac.ceh.components.datastore.git;

/**
 *
 * @author cjohn
 */
public interface GitAuthorResolver<A> {
    A getAuthor(String name);
}
