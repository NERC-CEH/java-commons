package uk.org.ukeof.db;

import java.io.File;

/**
 *
 * @author cjohn
 */
public interface DataSubmissionListener<A extends DataAuthor> {
    void dropIndexes();
    void indexFile(File file);
}
