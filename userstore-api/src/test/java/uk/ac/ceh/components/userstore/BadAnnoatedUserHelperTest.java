package uk.ac.ceh.components.userstore;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author CJOHN
 */
public class BadAnnoatedUserHelperTest {
    @Test(expected=IllegalArgumentException.class)
    public void attemptToCreateUserHelperFromUnAnnotatedUser() {
        //given
        Class<TestPoorlyAnnotatedUser> userClass = TestPoorlyAnnotatedUser.class;
        
        //when
        AnnotatedUserHelper<TestPoorlyAnnotatedUser> helper = new AnnotatedUserHelper<>(userClass);
        
        //then
        fail("Expected to fail with illegalargumentexception");
    }
    
    public class TestPoorlyAnnotatedUser implements User {
        private String username;

        @Override
        public String getUsername() {
            return username;
        }
    }
}
