package uk.ac.ceh.components.datastore.git;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.components.userstore.User;

/**
 *
 * @author cjohn
 */
@Data
@AllArgsConstructor(access=lombok.AccessLevel.PACKAGE)
public class GitDataSubmittedEvent<A extends DataAuthor & User> implements DataSubmittedEvent<GitDataRepository<A>> {
    private final GitDataRepository<A> dataRepository;
    private final Collection<String> filenames;
}
