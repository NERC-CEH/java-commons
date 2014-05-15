package uk.ac.ceh.components.datastore.git;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.components.datastore.OngoingDataCommit;
import uk.ac.ceh.components.userstore.User;

/**
 *
 * @author Christopher Johnson
 */
@Getter(lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access=lombok.AccessLevel.PROTECTED)
public class GitOngoingDataCommit<A extends DataAuthor & User> implements OngoingDataCommit<A> {
    private final GitDataRepository<A> repository;
    private Map<String, DataWriter> toWrite;
    private List<String> toDelete;
    
    GitOngoingDataCommit(GitDataRepository repo) {
        this(repo, new HashMap<String, DataWriter>(), new ArrayList<String>());
    }
    
    @Override
    public GitOngoingDataCommit<A> submitData(String filename, DataWriter writer) {
        toWrite.put(filename, writer);
        return this;
    }

    @Override
    public GitOngoingDataCommit<A> deleteData(String filename) {
        toDelete.add(filename);
        return this;
    }

    @Override
    public DataRevision<A> commit(A author, String message) throws DataRepositoryException {
        return repository.submit(this, author, message);
    }

}
