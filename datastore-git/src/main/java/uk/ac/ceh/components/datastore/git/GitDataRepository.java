package uk.ac.ceh.components.datastore.git;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import uk.ac.ceh.components.datastore.*;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.UserStore;

/**
 *
 * @author cjohn
 */
public class GitDataRepository<A extends DataAuthor> implements DataRepository<A> {
    private final List<DataSubmissionListener<A>> listeners;
    private final Repository repository;
    private final UserStore<A, ? extends GitAuthorBuilder<A>> authorResolver;
    private final File root;
    
    public GitDataRepository(File data, UserStore<A, ? extends GitAuthorBuilder<A>> authorResolver) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.listeners = new ArrayList<>();
        this.root = data;
        this.authorResolver = authorResolver;
        repository = builder.setGitDir(new File(data, ".git"))
            .readEnvironment() // scan environment GIT_* variables
            .findGitDir() // scan up the file system tree
            .build();
    }
    
    @Override public InputStream getData(String name) throws DataRepositoryException {
        return getData(name, Constants.HEAD);
    }

    @Override public InputStream getData(String name, String revisionStr) throws DataRepositoryException {
        try {
            RevWalk revWalk = new RevWalk(repository);
            RevCommit commit = revWalk.parseCommit(repository.resolve(revisionStr));
            
            TreeWalk treeWalk = TreeWalk.forPath(repository, name, commit.getTree());
            if(treeWalk != null) {
                treeWalk.setRecursive(true);

                CanonicalTreeParser canonicalTreeParser = treeWalk.getTree(0, CanonicalTreeParser.class);

                if(!canonicalTreeParser.eof()) {
                    return repository.open(canonicalTreeParser.getEntryObjectId()).openStream();
                }
            }

            throw new DataRepositoryException("The file does not exist");
        } catch(IOException io) {
            throw new DataRepositoryException(io);
        }
    }

    @Override public synchronized DataRevision<A> submitData(A author, String message, Map<String, InputStream> toWrite) throws DataRepositoryException {
        try {
            AddCommand addCommand = new Git(repository).add();
            for(Entry<String, InputStream> curr : toWrite.entrySet()) {
                try (ReadableByteChannel inChannel = Channels.newChannel(curr.getValue())) {
                    File toCreate = new File(root, curr.getKey());  //define the file to create
                    toCreate.getParentFile().mkdirs();              //create the parent dirs
                    try (FileChannel outChannel = new FileOutputStream(toCreate).getChannel()) {
                        fastChannelCopy(inChannel, outChannel);
                    }
                    indexFile(toCreate);
                }
                addCommand.addFilepattern(curr.getKey());
            }
            addCommand.call();
            
            RevCommit revision = new Git(repository)
                                     .commit()
                                     .setMessage(message)
                                     .setAuthor(author.getID(), author.getEmail()).call();
            
            return new GitDataRevision(author, revision);
        } catch (GitAPIException | IOException ex) {
            throw new DataRepositoryException(ex);
        } 
    }
    
    
    @Override
    public DataRevision<A> deleteData(A author, String message, List<String> toDelete) throws DataRepositoryException {
        try {
            RmCommand remove = new Git(repository).rm();
            for(String curr: toDelete) {
                new File(root, curr).delete();
                remove.addFilepattern(curr);
            }
            remove.call();
            RevCommit revision = new Git(repository)
                                        .commit()
                                        .setMessage(message)
                                        .setAuthor(author.getID(), author.getEmail()).call();

            return new GitDataRevision(author, revision);
        } catch (GitAPIException ex) {
            throw new DataRepositoryException(ex);
        } 
    }
    
    @Override public List<DataRevision<A>> getRevisions(String name) throws DataRepositoryException {
        try {
            List<DataRevision<A>> toReturn = new ArrayList<>();
            Git git = new Git(repository);
            LogCommand logCommand = git.log()
                    .add(git.getRepository().resolve(Constants.HEAD))
                    .addPath(name);
            for(RevCommit commit : logCommand.call()) {
                PersonIdent authorIdent = commit.getAuthorIdent();
                String username = authorIdent.getName();
                A author = (authorResolver.userExists(username)) 
                        ? authorResolver.getUser(username) 
                        : authorResolver.getPhantomUserBuilder()
                                        .setUsername(username)
                                        .setEmail(authorIdent.getEmailAddress())
                                        .build();
                toReturn.add(new GitDataRevision<>(author, commit));
            }
            return toReturn;
        }
        catch(IOException | GitAPIException | UnknownUserException ex) {
            throw new DataRepositoryException(ex);
        }
    }
    
    @Override public void addDataSubmissionListener(DataSubmissionListener<A> listener) {
        listeners.add(listener);
    }

    @Override public boolean removeDataSubmissionListener(DataSubmissionListener<A> listener) {
        return listeners.remove(listener);
    }

    @Override
    public void triggerReindex() {
        for(DataSubmissionListener<A> curr : listeners) {
            curr.dropIndexes();
            indexFile(root);
        }
    }
    
    private void indexFile(File toIndex) {
        if(toIndex.isDirectory()) {
            for(File curr: toIndex.listFiles()) {
                indexFile(curr);
            }
        }
        else {
            for(DataSubmissionListener<A> curr : listeners) {
                curr.indexFile(toIndex);
            }
        }
    }
    
    private static void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (src.read(buffer) != -1) {
            buffer.flip();
            dest.write(buffer);
            buffer.compact();
        }
        buffer.flip();
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
    }
}
