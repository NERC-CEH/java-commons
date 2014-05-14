package uk.ac.ceh.components.datastore.git;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.datastore.DataDeletedEvent;
import uk.ac.ceh.components.userstore.User;

/**
 *
 * @author cjohn
 */
@Data
@AllArgsConstructor(access=lombok.AccessLevel.PACKAGE)
public class GitDataDeletedEvent<A extends DataAuthor & User> implements DataDeletedEvent<GitDataRepository<A>> {
    private final GitDataRepository<A> dataRepository;
    private final Collection<String> filenames;
}
