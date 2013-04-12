package uk.org.ukeof.db.git;

/**
 *
 * @author cjohn
 */
public interface GitAuthorResolver<A> {
    A getAuthor(String name);
}
