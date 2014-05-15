package uk.ac.ceh.components.datastore.git;

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

/**
 *
 * @author Christopher Johnson
 */
public class GitOngoingDataCommitTest {

    @Mock GitDataRepository<GitTestUser> repository;
    @Mock Map<String, DataWriter> toWrite;
    @Mock List<String> toDelete;
    GitDataOngoingCommit<GitTestUser> ongoingCommit;
    
    @Before
    public void createMocks() {
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
}
