package uk.ac.ceh.components.datastore.git;

import com.google.common.eventbus.EventBus;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import uk.ac.ceh.components.datastore.*;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;
import uk.ac.ceh.components.userstore.UserBuilderFactory;
import uk.ac.ceh.components.userstore.UserStore;

/**
 * The following is a concrete implementation of a DataRepository. It is based 
 * upon the software version control system Git.
 *
 * DataAuthors of this implementation come from some given userstore. Since
 * it is a valid situation that users can be deleted from the userstore but 
 * still have revision history associated to them, a UserBuilderFactory needs to
 * be provided which can create DataAuthors are of type A.
 * @author cjohn
 */
public class GitDataRepository<A extends DataAuthor & User> implements DataRepository<A> {
    private final Repository repository;
    private final UserStore<A> authorResolver;
    private final File root;
    private final EventBus events;
    private final UserBuilderFactory<A> phantomUserFactory;
    
    /**
     * The following is the constructor for the GitDataRepository.
     * 
     * @param data A folder which contains a git repository
     * @param authorResolver A userStore which will be the primary source for 
     *  obtaining GitAuthorUsers
     * @param phantomUserFactory The userbuilder factory which will be used to create
     *  phantom users, that is. Users who do not exist in the UserStore but have
     *  revision history associated to them
     * @param events
     * @throws IOException 
     */
    public GitDataRepository(File data, UserStore<A> authorResolver, 
                                UserBuilderFactory<A> phantomUserFactory, 
                                EventBus events) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.events = events;
        this.root = data;
        this.authorResolver = authorResolver;
        this.phantomUserFactory = phantomUserFactory;
        repository = builder.setGitDir(new File(data, ".git"))
            .readEnvironment() // scan environment GIT_* variables
            .findGitDir() // scan up the file system tree
            .build();
        
        if(!repository.getDirectory().exists()) { //If the repository does not already exist
            repository.create();                  //Create it
        }
    }
    
    @Override public GitDataDocument getData(String name) throws DataRepositoryException {
        return getData(Constants.HEAD, name);
    }

    @Override public GitDataDocument getData(String revisionStr, String name) throws DataRepositoryException {
        RevWalk revWalk = new RevWalk(repository);
        ObjectId revision = resolveRevision(revisionStr);
        
        if(revision != null) {
            try {
                RevCommit commit = revWalk.parseCommit(revision);

                TreeWalk treeWalk = TreeWalk.forPath(repository, name, commit.getTree());
                if(treeWalk != null) {
                    treeWalk.setRecursive(true);

                    CanonicalTreeParser canonicalTreeParser = treeWalk.getTree(0, CanonicalTreeParser.class);

                    if(!canonicalTreeParser.eof()) {
                        return new GitDataDocument(name, revisionStr, repository.open(canonicalTreeParser.getEntryObjectId()));
                    }
                }
            } catch(MissingObjectException | IncorrectObjectTypeException ex) {
                throw new GitRevisionNotFoundException("The specified revision does not exist", ex);
            }
            catch(IOException io) {
                throw new DataRepositoryException(io);
            }
            throw new GitFileNotFoundException("The file does not exist");
        }
        else {    
            throw new GitRevisionNotFoundException("The specified revision does not exist");
        }
    }
    
    @Override
    public DataRevision<A> getLatestRevision() throws DataRepositoryException {
        try {
            RevWalk revWalk = new RevWalk(repository);
            RevCommit commit = revWalk.parseCommit(resolveRevision(Constants.HEAD));
            A author = getAuthor(commit.getAuthorIdent());
            return new GitDataRevision<>(author, commit);
        } catch (DataRepositoryException ex) {
            throw ex;   
        } catch (UnknownUserException | IOException ex) {
            throw new DataRepositoryException(ex);
        }
    }
    
    /**
     * The following command will fetch data from the specified remote at the master
     * branch and then perform a git data reset to the HEAD of the remote master
     * @param remoteRepo The repository to fetch from
     * @param credentials The credentials to use, can be null if no credentials are
     *  required
     * @throws DataRepositoryException 
     */
    public synchronized void reset(String remoteRepo, GitCredentials credentials) throws DataRepositoryException {
        try {
            UsernamePasswordCredentialsProvider gitCred = credentials != null
                                                            ? credentials.getUsernamePasswordCredentialsProvider()
                                                            : null;
            
            FetchResult result = new Git(repository).fetch()
                                                    .setRemote(remoteRepo)
                                                    .setRefSpecs(new RefSpec("refs/heads/master"))
                                                    .setRemoveDeletedRefs(true)
                                                    .setCredentialsProvider(gitCred)
                                                    .call();
            
            new Git(repository).reset()
                               .setMode(ResetCommand.ResetType.HARD)
                               .setRef("FETCH_HEAD")
                               .call();
            
            events.post(new GitDataResetEvent(this, result.getMessages()));
            
        } catch(GitAPIException ex) {
            throw new DataRepositoryException(ex);
        }
    }
    
    /**
     * The following method will return a list of revisions for a given filename.
     * The revisions will be ordered in the list so that the first element is the
     * most modern revision of the file and the last the the initial revision of 
     * the file.
     * @param name of file to get history for
     * @return A list of revisions ordered as specified above.
     * @throws DataRepositoryException 
     */
    @Override public List<DataRevision<A>> getRevisions(String name) throws DataRepositoryException {
        List<DataRevision<A>> toReturn = new ArrayList<>();
        Git git = new Git(repository);
        ObjectId revision = resolveRevision(Constants.HEAD);
        if(revision != null) { //Only perform the git log if the repo has a HEAD
            try {
            
                LogCommand logCommand = git.log()
                        .add(revision)
                        .addPath(name);
                for(RevCommit commit : logCommand.call()) {
                    A author = getAuthor(commit.getAuthorIdent());                    
                    toReturn.add(new GitDataRevision<>(author, commit));
                }
                return toReturn;
            } catch(IOException | GitAPIException | UnknownUserException ex) {
                throw new DataRepositoryException(ex);
            }
        } 
        else {
            throw new GitRevisionNotFoundException("The repository has no head");
        }
    }

    @Override
    public List<String> getFiles() throws DataRepositoryException {
        ObjectId revision = resolveRevision(Constants.HEAD);
        if(revision != null) { //If there is no HEAD return an empty list
            return getFiles(revision);
        }
        else {
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<String> getFiles(String revisionStr) throws DataRepositoryException {
        ObjectId revision = resolveRevision(revisionStr);
        if(revision != null) {
            return getFiles(revision);
        }
        else {
            throw new GitRevisionNotFoundException("The specified revision does not exist");
        }
    }
    
    protected synchronized DataRevision<A> submit(GitDataOngoingCommit<A> toCommit, A author, String message) throws DataRepositoryException {
        try {
            List<Object> eventsList = new ArrayList<>();  //Create a list to store events
            deleteData(eventsList, toCommit.getToDelete());
            submitData(eventsList, toCommit.getToWrite());
            
            //Check to see if the above operations yeild any change in the repository.
            //If not, we won't perform the commit
            IndexDiff diff = new IndexDiff(repository, Constants.HEAD, new FileTreeIterator(repository) );
            diff.setFilter(PathFilterGroup.createFromStrings(toCommit.getSubmittedFiles()));
            if(diff.diff()) {
                RevCommit revision = new Git(repository)
                                        .commit()
                                        .setMessage(message)
                                        .setAuthor(author.getUsername(), author.getEmail()).call();
            
                for(Object event: eventsList) {
                    events.post(event);
                }
                return new GitDataRevision(author, revision);
            }
            else {
                return getLatestRevision();
            }
        } catch (DataRepositoryException ex) {
            throw ex;
        } catch (GitAPIException | IOException ex) {
            throw new DataRepositoryException(ex);
        } 
    }
    
    /**
     * Perform a gc operation on the git repository, this will optimise the 
     * git repository's size. It is recommended that applications schedule this
     * operation at intervals which best fit usage of that application.
     * 
     * A dumb recommendation would be to do this nightly at a period of low usage
     * @throws DataRepositoryException if the gc command cant be called
     */
    public synchronized void optimize() throws DataRepositoryException {
        try {
            new Git(repository).gc().call();
        } catch(GitAPIException ex) {
            throw new DataRepositoryException(ex);
        }
    }
        
    /**
     * Method to close the underlying git repository when it is no longer needed
     */
    public void close() {
        repository.close();
    }
    
    private void submitData(List<Object> events, Map<String, DataWriter> toWrite) throws IOException, DataRepositoryException, GitAPIException {
        if(!toWrite.isEmpty()) {
            AddCommand addCommand = new Git(repository).add();
            for(Entry<String, DataWriter> curr : toWrite.entrySet()) {
                File toCreate = new File(root, curr.getKey());  //define the file to create
                toCreate.getParentFile().mkdirs();              //create the parent dirs
                try (FileOutputStream out = new FileOutputStream(toCreate)){
                    curr.getValue().write(out);
                }
                addCommand.addFilepattern(curr.getKey());
            }
            events.add(new GitDataSubmittedEvent(this, toWrite.keySet())); //Perform a data submitted index for the given file
            addCommand.call();
        }
    }
    
    private void deleteData(List<Object> events, List<String> toDelete) throws GitAPIException {
        if(!toDelete.isEmpty()) {
            RmCommand remove = new Git(repository).rm();
            for(String curr: toDelete) {
                new File(root, curr).delete();
                remove.addFilepattern(curr);
            }
            events.add(new GitDataDeletedEvent(this, toDelete)); //Perform a data deleted index for the given file
            remove.call();
        }
    }
    
    private A getAuthor(PersonIdent authorIdent) throws UnknownUserException {
        String username = authorIdent.getName();
        return (authorResolver.userExists(username)) 
                ? authorResolver.getUser(username) 
                : phantomUserFactory.newUserBuilder(username)
                                .set(UserAttribute.EMAIL, authorIdent.getEmailAddress())
                                .build();
    }
    
    private List<String> getFiles(ObjectId revision) throws DataRepositoryException {
       try {
            List<String> toReturn = new ArrayList<>();
            RevWalk revWalk = new RevWalk(repository);
            RevCommit commit = revWalk.parseCommit(revision);
            TreeWalk walk = new TreeWalk(repository);
            walk.setRecursive(true);
            walk.addTree(commit.getTree());

            while(walk.next()) {
                toReturn.add(walk.getPathString());
            }
            return toReturn;
        } catch(MissingObjectException | IncorrectObjectTypeException ex) {
            throw new GitRevisionNotFoundException("Failed to find the specified revision", ex);
        } catch(IOException ex) {
            throw new DataRepositoryException(ex);
        }
    }
    
    /* Resolve the given git revision but wrap exceptions as DataRepositoryExceptions */
    private ObjectId resolveRevision(String revisionStr) throws DataRepositoryException {
        try {
            return repository.resolve(revisionStr);
        }
        catch(AmbiguousObjectException | IncorrectObjectTypeException | RevisionSyntaxException ex) {
            throw new GitRevisionNotFoundException("Failed to find the specified revision", ex);
        }
        catch(IOException ex) {
            throw new DataRepositoryException(ex);
        }
    }

    @Override
    public GitDataOngoingCommit<A> submitData(String filename, DataWriter writer) {
        return new GitDataOngoingCommit<A>(this)
                .submitData(filename, writer);
    }

    @Override
    public GitDataOngoingCommit<A> deleteData(String toDelete) {
        return new GitDataOngoingCommit<A>(this)
                .deleteData(toDelete);
    }
}
