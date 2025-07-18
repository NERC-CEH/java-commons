package uk.ac.ceh.components.datastore.git;

import com.google.common.eventbus.EventBus;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.List;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import static org.mockito.Mockito.*;
import uk.ac.ceh.components.datastore.DataDeletedEvent;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;

/**
 * The following test will test the GitDataRepository using a InMemoryUserStore
 * @author cjohn
 */
public class GitDataRepositoryTest {
    public @Rule TemporaryFolder folder= new TemporaryFolder();
    private @Mock EventBus bus;
    private @Spy InMemoryUserStore<GitTestUser> userStore;
    private GitDataRepository<GitTestUser> dataStore;
    private final AnnotatedUserHelper factory;
    
    public GitDataRepositoryTest() {
        factory = new AnnotatedUserHelper(GitTestUser.class);
    }
    
    @Before
    public void createEmptyRepository() throws IOException, UsernameAlreadyTakenException {        
        //create an in memory userstore
        userStore = new InMemoryUserStore<>();
        populateTestUsers();
        
        //Init mocks
        MockitoAnnotations.initMocks(this);
        
        //create a testRepo folder and then a git data repository
        dataStore = new GitDataRepository(folder.getRoot(), userStore, factory, bus);
    }
    
    @Test
    public void addFileToRepository() throws UnknownUserException, DataRepositoryException, IOException {
        //Given
        String filename = "new.file";
        GitTestUser testUser = userStore.getUser("testuser");
        String file = "file";
        
        //When
        dataStore.submitData(filename, new StringDataWriter(file))
                 .commit(testUser, "This is a test message");
        
        //Then
        byte[] gitFilebytes = IOUtils.toByteArray(dataStore.getData(filename).getInputStream());
        assertArrayEquals("Did not get expected file", file.getBytes(), gitFilebytes);
    }
    
    @Test
    public void addFileToRepositoryAsUnknownUser() throws UnknownUserException, DataRepositoryException, IOException {
        //Given
        String filename = "new.file";
        GitTestUser testUser = new GitTestUser.Builder("unknownUser")
                                            .setEmail("noone@somewhere.com")
                                            .build();
        String file = "file";

        //When
        dataStore.submitData(filename, new StringDataWriter(file))
                 .commit(testUser, "This is a test message");
                
        //Then
        byte[] gitFilebytes = IOUtils.toByteArray(dataStore.getData(filename).getInputStream());
        assertArrayEquals("Did not get expected file", file.getBytes(), gitFilebytes);
    }
    
    @Test
    public void getFileOfUserWhoIsDeleted() throws UnknownUserException, DataRepositoryException, IOException {
        //Given
        String filename = "new.file";
        GitTestUser testUser = userStore.getUser("testuser");
        String file = "file";
        
        //When
        dataStore.submitData(filename, new StringDataWriter(file))
                 .commit(testUser, "This is a test message");
        userStore.deleteUser("testuser"); //delete the original user
        
        //Then
        byte[] gitFilebytes = IOUtils.toByteArray(dataStore.getData(filename).getInputStream());
        assertNotSame("The user was deleted so that user should have been recreated as a phantom", 
                testUser, dataStore.getRevisions(filename).get(0).getAuthor());
        
        assertArrayEquals("Did not get expected file", file.getBytes(), gitFilebytes);
    }
    
    @Test
    public void getDeletedFile() throws UnknownUserException, DataRepositoryException, IOException {
        //Given
        String filename = "new.file";
        GitTestUser testUser = userStore.getUser("testuser");
        String file = "file";
        
        //When   
        dataStore.submitData(filename, new StringDataWriter(file))
                .commit(testUser, "This is a test message");
        
        dataStore.deleteData(filename)
                 .commit(testUser, "Deleting file as a test");
        
        //Then
        List<DataRevision<GitTestUser>> revisions = dataStore.getRevisions("new.file");
        assertEquals("Expected revision history of size two (Added and removed)", 2, revisions.size());
        String revisionId = revisions.get(1).getRevisionID();
        byte[] gitFilebytes = IOUtils.toByteArray(dataStore.getData(revisionId, filename).getInputStream());
        assertArrayEquals("Did not get expected file", file.getBytes(), gitFilebytes);
    }
    
    @Test
    public void getNotifiedOfIndexEvent() throws DataRepositoryException, UnknownUserException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        
        //When
        dataStore.submitData("filename", new StringDataWriter("data"))
                .commit(testUser, "Adding test file");
        
        //Then
        verify(bus, times(1)).post(isA(DataSubmittedEvent.class));
    }
    
    @Test
    public void getNotifiedOfResetEvent() throws UnknownUserException, IOException, DataRepositoryException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        File originRepository = new File("originRepo");
        
        try {
            FileUtils.forceMkdir(originRepository);
            GitDataRepository originDataStore = new GitDataRepository(originRepository, userStore, factory, new EventBus());
            originDataStore.submitData("test1.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
           
            //When
            dataStore.reset(originRepository.getAbsolutePath(), null);

            //Then
            verify(bus, times(1)).post(isA(GitDataResetEvent.class));
        }
        finally {
            FileUtils.deleteDirectory(originRepository);
        }
    }
    
    @Test
    public void getNotifiedOfDeleteEvent() throws UnknownUserException, DataRepositoryException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData("filename", new StringDataWriter("data"))
                .commit(testUser, "Adding test file");
        
        //When
        dataStore.deleteData("filename")
                .commit(testUser, "Deleting file");
        
        //Then
        verify(bus, times(1)).post(isA(DataDeletedEvent.class));
    }
    
    @Test
    public void multipleDataAddsOnlyYieldOnDataSubmittedEvent() throws UnknownUserException, DataRepositoryException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        
        //When
        dataStore.submitData("filename", new StringDataWriter("data"))
                 .submitData("filename2", new StringDataWriter("data2"))
                 .commit(testUser, "My Commit");
        
        //Then
        ArgumentCaptor<DataSubmittedEvent> argument = ArgumentCaptor.forClass(DataSubmittedEvent.class);
        verify(bus, times(1)).post(argument.capture());
        assertEquals("Expected two files in event", 2, argument.getValue().getFilenames().size());
    }
    
    @Test
    public void commitMultipleTimesAndGetFilesList() throws UnknownUserException, DataRepositoryException, Exception {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData("test1.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
        dataStore.submitData("test2.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
        
        //When
        List<String> files = dataStore.getFiles();
        
        //Then
        assertEquals("Expected two files in repository", 2, files.size());
    }
    
    @Test
    public void getFilesFromPenultimateRevision() throws UnknownUserException, DataRepositoryException, Exception {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData("test1.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
        dataStore.submitData("test2.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
        
        //When
        List<String> files = dataStore.getFiles("HEAD~1");
        
        //Then
        assertEquals("Expected one files in repository", 1, files.size());
    }
    
    @Test(expected=GitRevisionNotFoundException.class)
    public void getFileListFromNonExistantRevision() throws DataRepositoryException, UnknownUserException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData("test1.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
        
        //When
        List<String> files = dataStore.getFiles("7965702063687269732072756c657320776f6f74");
        
        //Then
        fail("Expected to fail getting the file list");
    }
    
    @Test(expected=GitFileNotFoundException.class)
    public void getFileWhichDoesntExist() throws UnknownUserException, DataRepositoryException {
        //Given 
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData("test1.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
        
        //When
        dataStore.getData("file.not.present");
        
        //Then
        fail("Expected to fail with missing file exeception");
    }
    
    @Test(expected=GitRevisionNotFoundException.class)
    public void getFileFromNonExistantRevision() throws DataRepositoryException, UnknownUserException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData("test1.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
        
        //When
        GitDataDocument files = dataStore.getData("4920616d20736f207661696e2049206b6e6f772e", "test1.file");
        
        //Then
        fail("Expected to fail getting the file");
    }
    
    @Test
    public void getTheLatestRevisionForAPopulatedRepo() throws DataRepositoryException, UnknownUserException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData("test1.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
        
        //When
        DataRevision<GitTestUser> latestRevision = dataStore.getLatestRevision();
               
        //Then
        assertTrue("Expected a valid id", ObjectId.isId(latestRevision.getRevisionID()));
        assertEquals("Expected test user to have been the commiter", testUser, latestRevision.getAuthor());
    }
    
    @Test(expected=GitRevisionNotFoundException.class)
    public void getFileBeforeAddingAnyData() throws DataRepositoryException {
        //Given
        String fileToGet = "dummyfile";
        
        //When
        dataStore.getData(fileToGet);
        
        //Then
        fail("Expected to fail when getting the file");
    }
    
    @Test(expected=GitRevisionNotFoundException.class)
    public void getFilesFromHEADOfEmptyRepository() throws DataRepositoryException {
        //Given
        String revision = "HEAD";
        
        //When
        List<String> files = dataStore.getFiles(revision);
        
        //Then
        fail("Expected to fail from empty repo as the specified revision did not exists");
    }
    
    @Test
    public void getFileListBeforeAddingAnyData() throws DataRepositoryException {
        //Given
        //Nothing
        
        //When
        List<String> files = dataStore.getFiles();
        
        //Then
        assertEquals("Expected an empty repository", 0, files.size());
    }
    
    @Test
    public void getFilesFromAfterDelete() throws UnknownUserException, DataRepositoryException, Exception {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData("test1.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
        dataStore.deleteData("test1.file").commit(testUser, "This is a test message");
        
        //When
        List<String> files = dataStore.getFiles();
        
        //Then
        assertEquals("Expected no files in repository", 0, files.size());
    }
        
    @Test(expected=GitRevisionNotFoundException.class)
    public void getRevisionListForFilesWhenRepoHasNoHead() throws DataRepositoryException {
        //Given
        String filename = "dummy.txt";
        
        //When
        List<DataRevision<GitTestUser>> revisions = dataStore.getRevisions(filename);
        
        //Then
        fail("Expected to fail as there is no revision this file could live in");
    }
    
    @Test
    public void resetToExternalGitStore() throws Exception {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        File originRepository = new File("originRepo");
        
        try {
            FileUtils.forceMkdir(originRepository);
            GitDataRepository originDataStore = new GitDataRepository(originRepository, userStore, factory, new EventBus());
            originDataStore.submitData("test1.file", new StringDataWriter("data")).commit(testUser, "This is a test message");
           
            //When
            dataStore.reset(originRepository.getAbsolutePath(), null);

            //Then
            assertEquals("Expected one files in repository", 1, dataStore.getFiles().size());
        }
        finally {
            FileUtils.deleteDirectory(originRepository);
        }
    }
    
    @Test
    public void commitOfUnChangedFileDoesNotPerformEmptyGitCommit() throws UnknownUserException, DataRepositoryException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData("test1.file", new StringDataWriter("data"))
                 .commit(testUser, "This is a test message");
        
        //When
        dataStore.submitData("test1.file", new StringDataWriter("data"))
                 .commit(testUser, "Attempt to commit the same data again");
        
        //Then
        List<DataRevision<GitTestUser>> revisions = dataStore.getRevisions("test1.file");
        assertEquals("Expected to only find the original revision", 1, revisions.size());
    }
    
    @Test
    public void commitOfUnChangedFileReturnsThePreviousCommitAuthor() throws UnknownUserException, DataRepositoryException {
        //Given
        GitTestUser firstUser = userStore.getUser("testuser");
        GitTestUser secondUser = userStore.getUser("testuser2");
        dataStore.submitData("test1.file", new StringDataWriter("data"))
                 .commit(firstUser, "first commit of file");
        
        //When
        DataRevision<GitTestUser> revision = dataStore.submitData("test1.file", new StringDataWriter("data"))
                                                      .commit(secondUser, "recommit of exactly the same data");
        
        //Then
        assertEquals("Expected the revision to be authored by the last commit", firstUser, revision.getAuthor());
    }
    
    @Test
    public void reAddingFileDoesResultInMultipleCommits() throws UnknownUserException, DataRepositoryException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData("test1.file", new StringDataWriter("data"))
                 .commit(testUser, "This is a test message");
        
        dataStore.deleteData("test1.file").commit(testUser, "Deleting the file");
        
        //When
        dataStore.submitData("test1.file", new StringDataWriter("data"))
                 .commit(testUser, "Attempt to commit the same data again");
        
        //Then
        List<DataRevision<GitTestUser>> revisions = dataStore.getRevisions("test1.file");
        assertEquals("Expected the readding of the file to yeild 3 commits", 3, revisions.size());
    }
    
    @Test
    public void optimizeShrinksRepoWithLotsSimilarData() throws DataRepositoryException, UnknownUserException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        
        for(int i=0; i< 100; i++) {
            dataStore.submitData("test" + i + ".file", new StringDataWriter("testdata"))
                     .commit(testUser, "This is a test message"); 
        }
        
        long sizeOfRepoBeforeOptimize = FileUtils.sizeOfDirectory(folder.getRoot());
        
        //When
        dataStore.optimize();
        
        //Then
        long sizeOfRepoAfterOptimize = FileUtils.sizeOfDirectory(folder.getRoot());
        assertTrue("Expected the repo to have shrunk in size", sizeOfRepoBeforeOptimize > sizeOfRepoAfterOptimize);
    }
    
    @Test
    public void optimizeDoesntRemoveHistory() throws DataRepositoryException, UnknownUserException, IOException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        String filename = "new.file";
        String file = "file";
        
        dataStore.submitData(filename, new StringDataWriter(file))
                     .commit(testUser, "This is a test message");
        dataStore.deleteData(filename).commit(testUser, "Deleting file");
        
        //When
        dataStore.optimize();
        
        //Then
        List<DataRevision<GitTestUser>> revisions = dataStore.getRevisions(filename);
        assertEquals("Expected revision history of size two (Added and removed)", 2, revisions.size());
        String revisionId = revisions.get(1).getRevisionID();
        byte[] gitFilebytes = IOUtils.toByteArray(dataStore.getData(revisionId, filename).getInputStream());
        assertArrayEquals("Did not get expected file", file.getBytes(), gitFilebytes);
    }
    
    @Test
    public void bareRepositoryReturnsNullLatestRevision() throws DataRepositoryException {
        //Given
        //Empty Repository
        
        //When
        DataRevision<GitTestUser> latestRevision = dataStore.getLatestRevision();
        
        //Then
        assertNull("Expected latestRevision to be null", latestRevision);
    }

    @Test
    public void getRevisionsByCommitMessage() throws UnknownUserException, DataRepositoryException, IOException {
        //Given
        String filename = "new.file";
        GitTestUser testUser = userStore.getUser("testuser");
        String file = "file";
        String file2 = "file2";
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        long timeLimitInSecond = (currentTimestamp.getTime() / 1000) - 157680000;    // 5 year before

        //When
        dataStore.submitData(filename, new StringDataWriter(file))
                .commit(testUser, "This is a test message");
        dataStore.submitData(filename, new StringDataWriter(file2))
                .commit(testUser, "This is another test message");

        //Then
        List<DataRevision<GitTestUser>> revisions = dataStore.getRevisions(timeLimitInSecond, "This is a test message");
        assertEquals("Expected 1 revision history for commit message \"This is a test message\"", 1, revisions.size());
        String revisionId = revisions.get(0).getRevisionID();
        byte[] gitFilebytes = IOUtils.toByteArray(dataStore.getData(revisionId, filename).getInputStream());
        assertArrayEquals("Did not get expected file", file.getBytes(), gitFilebytes);
    }

    @After
    public void closeRepository() throws IOException {
        dataStore.close();
    }
    
    private void populateTestUsers() throws UsernameAlreadyTakenException {
        userStore.addUser(new GitTestUser.Builder("testuser")
                .setEmail("test@user.com")
                .build(), "");
        userStore.addUser(new GitTestUser.Builder("testuser2")
                .setEmail("test2@user.com")
                .build(), "");
    }
    
    @Data
    private static class StringDataWriter implements DataWriter {
        private final String content;
        
        @Override
        public void write(OutputStream out) throws IOException {
            out.write(content.getBytes());
        }
    }
}
