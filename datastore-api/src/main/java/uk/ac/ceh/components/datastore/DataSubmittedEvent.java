package uk.ac.ceh.components.datastore;

import java.util.Collection;

/**
 *
 * @author cjohn
 */
public interface DataSubmittedEvent<R extends DataRepository> {
    Collection<String> getFilenames();    
    R getDataRepository();
}
