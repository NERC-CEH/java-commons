package uk.ac.ceh.components.userstore.crowd.jaas;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author mw
 */
public class UserPrincipalTest {
    
    @Test
    public void checkGetNameReturnsName() {
        //Given
        UserPrincipal user = new UserPrincipal();
        user.setUsername("my test username");
        
        //When
        String name = user.getName();
        
        //Then
        assertEquals("Expected to find my test user name", "my test username", name);        
    }
    
}
