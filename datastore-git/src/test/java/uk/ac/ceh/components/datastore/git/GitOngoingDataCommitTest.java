package uk.ac.ceh.components.datastore.git;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import static org.mockito.Mockito.*;
import uk.ac.ceh.components.datastore.DataWriter;
import static org.junit.Assert.*;
import org.mockito.Spy;

/**
 *
 * @author Christopher Johnson
 */
public class GitOngoingDataCommitTest {

    @Mock GitDataRepository<GitTestUser> repository;
    @Spy Map<String, DataWriter> toWrite;
    @Spy List<String> toDelete;
    GitDataOngoingCommit<GitTestUser> ongoingCommit;
    
    @Before
    public void createMocks() {
        toWrite = new HashMap<>();
        toDelete = new ArrayList<>();
        MockitoAnnotations.initMocks(this);
        ongoingCommit = new GitDataOngoingCommit<>(repository, toWrite, toDelete);
    }
    
    @Test
    public void commitForwardsToRepository() throws DataRepositoryException {
        //Given
        GitTestUser testUser = new GitTestUser.Builder("unknownUser")
                                            .setEmail("noone@somewhere.com")
                                            .build();
        String message = "my commit message";
        
        //When
        ongoingCommit.commit(testUser, message);
        
        //Then
        verify(repository).submit(ongoingCommit, testUser, message);
    }
    
    @Test
    public void dataSubmitIsOngoing() {
        //Given
        String fileToAdd = "test";
        DataWriter writer = mock(DataWriter.class);
        
        //When
        GitDataOngoingCommit<GitTestUser> submitData = ongoingCommit.submitData(fileToAdd, writer);
        
        //Then
        verify(toWrite).put(fileToAdd, writer);
        assertSame("Expected the output of datasubmit to be the same ongoing commit", ongoingCommit, submitData);
    }
    
    @Test
    public void dataDeleteIsOngoing() {
        //Given
        String fileToDelete = "test";
        
        //When
        GitDataOngoingCommit<GitTestUser> deleteData = ongoingCommit.deleteData(fileToDelete);
        
        //Then
        verify(toDelete).add(fileToDelete);
        assertSame("Expected the output of datadelete to be the same ongoing commit", ongoingCommit, deleteData);
    }
    
    @Test
    public void submittedFilesContainToWriteAndToDelete() {
        //Given
        GitDataOngoingCommit<GitTestUser> ongoing = ongoingCommit.deleteData("toDelete")
                                                                 .submitData("toWrite", null);
        
        //When
        List<String> filesPotentiallyChanged = ongoing.getSubmittedFiles();
        
        //Then
        assertEquals("Expected only two files to have changed", 2, filesPotentiallyChanged.size());
        assertTrue("Expect to delete to be a file which could have changed", filesPotentiallyChanged.contains("toDelete") );
        assertTrue("Expect to write to be a file which could have changed", filesPotentiallyChanged.contains("toWrite") );
    }
}
