package uk.ac.ceh.components.datastore;

import java.util.Collection;

/**
 *
 * @author cjohn
 */
public interface DataDeletedEvent<R extends DataRepository> {
    Collection<String> getFilenames();
    R getDataRepository();
}
