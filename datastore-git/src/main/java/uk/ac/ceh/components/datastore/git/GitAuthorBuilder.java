package uk.ac.ceh.components.datastore.git;

import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.userstore.UserBuilder;

/**
 *
 * @author Christopher Johnson
 */
public interface GitAuthorBuilder<A extends DataAuthor> extends UserBuilder<A> {
    GitAuthorBuilder<A> setEmail(String email);
}
