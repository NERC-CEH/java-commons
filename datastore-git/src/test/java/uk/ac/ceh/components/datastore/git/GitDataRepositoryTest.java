package uk.ac.ceh.components.datastore.git;

import com.google.common.eventbus.EventBus;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import static org.junit.Assert.*;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;

/**
 * The following test will test the GitDataRepository using a InMemoryUserStore
 * @author cjohn
 */
public class GitDataRepositoryTest {
    private File repository;
    private EventBus bus;
    private InMemoryUserStore<GitTestUser> userStore;
    private GitDataRepository<GitTestUser> dataStore;
    private final AnnotatedUserHelper factory;
    
    public GitDataRepositoryTest() {
        factory = new AnnotatedUserHelper(GitTestUser.class);
    }
    
    @Before
    public void createEmptyRepository() throws IOException, UsernameAlreadyTakenException {
        //create an EventBus
        bus = new EventBus();
        
        //create an in memory userstore
        userStore = new InMemoryUserStore<>();
        populateTestUsers();
        
        //create a testRepo folder and then a git data repository
        repository = new File("testRepo");
        FileUtils.forceMkdir(repository);
        dataStore = new GitDataRepository(repository, userStore, factory, bus);
    }
    
    @Test
    public void addFileToRepository() throws UnknownUserException, DataRepositoryException, IOException {
        //Given
        String filename = "new.file";
        GitTestUser testUser = userStore.getUser("testuser");
        byte[] fileBytes = "file".getBytes();
        
        //When
        dataStore.submitData(testUser, "This is a test message", singleFileMap(filename, fileBytes));
        
        //Then
        byte[] gitFilebytes = IOUtils.toByteArray(dataStore.getData(filename));
        assertArrayEquals("Did not get expected file", fileBytes, gitFilebytes);
    }
    
    @Test
    public void addFileToRepositoryAsUnknownUser() throws UnknownUserException, DataRepositoryException, IOException {
        //Given
        String filename = "new.file";
        GitTestUser testUser = new GitTestUser.Builder("unknownUser")
                                            .setEmail("noone@somewhere.com")
                                            .build();
        byte[] fileBytes = "file".getBytes();

        //When
        dataStore.submitData(testUser, "This is a test message", singleFileMap(filename, fileBytes));
                
        //Then
        byte[] gitFilebytes = IOUtils.toByteArray(dataStore.getData(filename));
        assertArrayEquals("Did not get expected file", fileBytes, gitFilebytes);
    }
    
    @Test
    public void getFileOfUserWhoIsDeleted() throws UnknownUserException, DataRepositoryException, IOException {
        //Given
        String filename = "new.file";
        GitTestUser testUser = userStore.getUser("testuser");
        byte[] fileBytes = "file".getBytes();
        
        //When
        dataStore.submitData(testUser, "This is a test message", singleFileMap(filename, fileBytes));
        userStore.deleteUser("testuser"); //delete the original user
        
        //Then
        byte[] gitFilebytes = IOUtils.toByteArray(dataStore.getData(filename));
        assertNotSame("The user was deleted so that user should have been recreated as a phantom", 
                testUser, dataStore.getRevisions(filename).get(0).getAuthor());
        
        assertArrayEquals("Did not get expected file", fileBytes, gitFilebytes);
    }
    
    @Test
    public void getDeletedFile() throws UnknownUserException, DataRepositoryException, IOException {
        //Given
        String filename = "new.file";
        GitTestUser testUser = userStore.getUser("testuser");
        byte[] fileBytes = "file".getBytes();
        
        //When   
        dataStore.submitData(testUser, "This is a test message", singleFileMap(filename, fileBytes));
        dataStore.deleteData(testUser, "Deleting file as a test", Arrays.asList(filename));
        
        //Then
        List<DataRevision<GitTestUser>> revisions = dataStore.getRevisions("new.file");
        assertEquals("Expected revision history of size two (Added and removed)", 2, revisions.size());
        String revisionId = revisions.get(1).getRevisionID();
        byte[] gitFilebytes = IOUtils.toByteArray(dataStore.getData(filename, revisionId));
        assertArrayEquals("Did not get expected file", fileBytes, gitFilebytes);
    }
    
    @Test
    public void getNotifiedOfIndexEvent() throws DataRepositoryException, UnknownUserException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        DataSubmittedEventSubscriber subscriber = new DataSubmittedEventSubscriber();
        bus.register(subscriber);
        
        //When
        dataStore.submitData(testUser, "Adding test file", singleFileMap("filename", "data".getBytes()));
        
        //Then
        assertSame("Expected one datastore event", 1, subscriber.events.size());
    }
    
    @Test
    public void commitMultipleTimesAndGetFilesList() throws UnknownUserException, DataRepositoryException, Exception {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData(testUser, "This is a test message", singleFileMap("test1.file", "data".getBytes()));
        dataStore.submitData(testUser, "This is a test message", singleFileMap("test2.file", "data".getBytes()));
        
        //When
        List<String> files = dataStore.getFiles();
        
        //Then
        assertEquals("Expected two files in repository", 2, files.size());
    }
    
    @Test
    public void getFilesFromPenultimateRevision() throws UnknownUserException, DataRepositoryException, Exception {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData(testUser, "This is a test message", singleFileMap("test1.file", "data".getBytes()));
        dataStore.submitData(testUser, "This is a test message", singleFileMap("test2.file", "data".getBytes()));
        
        //When
        List<String> files = dataStore.getFiles("HEAD~1");
        
        //Then
        assertEquals("Expected one files in repository", 1, files.size());
    }
    
    @Test(expected=DataRepositoryException.class)
    public void getFileListFromNonExistantRevision() throws DataRepositoryException, UnknownUserException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData(testUser, "This is a test message", singleFileMap("test1.file", "data".getBytes()));
        
        //When
        List<String> files = dataStore.getFiles("1762164");
        
        //Then
        fail("Expected to fail getting the file list");
    }
    
    @Test(expected=DataRepositoryException.class)
    public void getFileFromNonExistantRevision() throws DataRepositoryException, UnknownUserException {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData(testUser, "This is a test message", singleFileMap("test1.file", "data".getBytes()));
        
        //When
        InputStream files = dataStore.getData("test1.file", "1762164");
        
        //Then
        fail("Expected to fail getting the file");
    }
    
    @Test(expected=DataRepositoryException.class)
    public void getFileBeforeAddingAnyData() throws DataRepositoryException {
        //Given
        String fileToGet = "dummyfile";
        
        //When
        dataStore.getData(fileToGet);
        
        //Then
        fail("Expected to fail when getting the file");
    }
    
    @Test(expected=DataRepositoryException.class)
    public void getFileListBeforeAddingAnyData() throws DataRepositoryException {
        //Given
        //Nothing
        
        //When
        dataStore.getFiles();
        
        //Then
        fail("Expected to fail when getting the file list");
    }
    
    @Test
    public void getFilesFromAfterDelete() throws UnknownUserException, DataRepositoryException, Exception {
        //Given
        GitTestUser testUser = userStore.getUser("testuser");
        dataStore.submitData(testUser, "This is a test message", singleFileMap("test1.file", "data".getBytes()));
        dataStore.deleteData(testUser, "This is a test message", Arrays.asList("test1.file"));
        
        //When
        List<String> files = dataStore.getFiles();
        
        //Then
        assertEquals("Expected no files in repository", 0, files.size());
    }
        
    @After
    public void deleteRepository() throws IOException {
        FileUtils.deleteDirectory(repository);
    }
    
    private void populateTestUsers() throws UsernameAlreadyTakenException {
        userStore.addUser(new GitTestUser.Builder("testuser")
                .setEmail("test@user.com")
                .build(), "");
    }
    
    private static Map<String, InputStream> singleFileMap(String name, byte[] content) {
        Map<String, InputStream> data = new HashMap<>();
        data.put(name, new ByteArrayInputStream(content));
        return data;
    }
}
