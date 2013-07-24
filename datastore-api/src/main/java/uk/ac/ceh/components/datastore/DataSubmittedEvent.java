package uk.ac.ceh.components.datastore;

import java.util.Collection;

/**
 *
 * @author cjohn
 */
public class DataSubmittedEvent<A extends DataAuthor> {
    private final Collection<String> dataAdded;
    private final DataRepository<A> repo;

    public DataSubmittedEvent(DataRepository<A> repo, Collection<String> dataAdded) {
        this.repo = repo;
        this.dataAdded = dataAdded;
    }
    
    public Collection<String> getFilenames() {
        return dataAdded;
    }
    
    public DataRepository<A> getDataRepository() {
        return repo;
    }
}
