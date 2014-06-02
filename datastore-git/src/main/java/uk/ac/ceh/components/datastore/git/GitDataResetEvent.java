package uk.ac.ceh.components.datastore.git;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.userstore.User;

/**
 *
 * @author cjohn
 */
@Data
@AllArgsConstructor(access=lombok.AccessLevel.PACKAGE)
public class GitDataResetEvent<A extends DataAuthor & User> {
    private final GitDataRepository<A> repo;
    private final String message;
}
