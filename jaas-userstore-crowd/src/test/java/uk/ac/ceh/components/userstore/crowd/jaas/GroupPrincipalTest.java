package uk.ac.ceh.components.userstore.crowd.jaas;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import uk.ac.ceh.components.userstore.Group;

/**
 *
 * @author mw
 */
public class GroupPrincipalTest {
    @Test
    public void checkGroupPrincipalHasNameOfGroup() {
        //Given
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("some name");
        
        //When
        GroupPrincipal groupPrincipal = new GroupPrincipal(group);
        
        //Then
        assertEquals("Expected to find some name", "some name", groupPrincipal.getName());
    }
    
    @Test
    public void checkThatGroupNameIsOnlyCalledOnce() {
        //Given
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("some name");
        GroupPrincipal groupPrincipal = new GroupPrincipal(group);
        
        //When
        groupPrincipal.getName();
        groupPrincipal.getName();
        
        //Then
        verify(group, times(1)).getName();
    }
    
}
