package uk.org.ukeof.db.git;

import org.eclipse.jgit.revwalk.RevCommit;
import uk.org.ukeof.db.DataAuthor;
import uk.org.ukeof.db.DataRevision;

/**
 *
 * @author cjohn
 */
class GitDataRevision<A extends DataAuthor> implements DataRevision<A> {
    private final A author;
    private final RevCommit commit;
    
    GitDataRevision(A author, RevCommit commit) {
        this.author = author;
        this.commit = commit;
    }
    
    @Override public String getRevisionID() {
        return commit.getId().getName();
    }

    @Override public String getMessage() {
        return commit.getFullMessage();
    }

    @Override public String getShortMessage() {
        return commit.getShortMessage();
    }

    @Override public A getAuthor() {
        return author;
    }
}
