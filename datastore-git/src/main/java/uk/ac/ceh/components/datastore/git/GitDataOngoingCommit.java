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
import uk.ac.ceh.components.datastore.DataOngoingCommit;
import uk.ac.ceh.components.userstore.User;

/**
 *
 * @author Christopher Johnson
 */
@Getter(lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access=lombok.AccessLevel.PROTECTED)
public class GitDataOngoingCommit<A extends DataAuthor & User> implements DataOngoingCommit<A> {
    private final GitDataRepository<A> repository;
    private Map<String, DataWriter> toWrite;
    private List<String> toDelete;
    
    GitDataOngoingCommit(GitDataRepository repo) {
        this(repo, new HashMap<String, DataWriter>(), new ArrayList<String>());
    }
    
    @Override
    public GitDataOngoingCommit<A> submitData(String filename, DataWriter writer) {
        toWrite.put(filename, writer);
        return this;
    }

    @Override
    public GitDataOngoingCommit<A> deleteData(String filename) {
        toDelete.add(filename);
        return this;
    }

    @Override
    public DataRevision<A> commit(A author, String message) throws DataRepositoryException {
        return repository.submit(this, author, message);
    }

}
