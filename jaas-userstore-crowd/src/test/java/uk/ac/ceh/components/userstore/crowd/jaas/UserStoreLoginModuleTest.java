package uk.ac.ceh.components.userstore.crowd.jaas;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import org.junit.Test;
/**
 *
 * @author mw
 */
public class UserStoreLoginModuleTest {    
    @Test
    public void checkThatAbortIsAlwaysFalse() throws LoginException {
        //Given
        UserStoreLoginModule module = mock(UserStoreLoginModule.class, CALLS_REAL_METHODS);
        
        //When
        boolean abort = module.abort();
        
        //Then
        assertFalse("Expected abort to be false", abort);
    }
    
    @Test
    public void checkWhenInitializedThatUserStoreAndGroupStoreAreCreated() {
        //Given
        Subject subject = new Subject();
        CallbackHandler callbackHandler = mock(CallbackHandler.class);
        Map sharedState = new HashMap();
        Map options = new HashMap();
        UserStoreLoginModule module = mock(UserStoreLoginModule.class, CALLS_REAL_METHODS);
        doNothing().when(module).createUserStoreAndGroupStore(options);
        
        //When
        module.initialize(subject, callbackHandler, sharedState, options);
        
        //Then
        verify(module).createUserStoreAndGroupStore(options);
    }
}
