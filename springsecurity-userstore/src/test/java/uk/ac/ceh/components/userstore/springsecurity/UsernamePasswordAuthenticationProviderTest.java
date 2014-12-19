package uk.ac.ceh.components.userstore.springsecurity;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import uk.ac.ceh.components.tokengeneration.ExpiredTokenException;
import uk.ac.ceh.components.tokengeneration.InvalidTokenException;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.InvalidCredentialsException;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.UserStore;

/**
 *
 * @author cjohn
 */
public class UsernamePasswordAuthenticationProviderTest {
    @Mock UserStore userstore;
    @Mock GroupStore groupstore;
    
    UsernamePasswordAuthenticationProvider provider;
    
    @Before
    public void createProvider() {
        MockitoAnnotations.initMocks(this);
        provider = new UsernamePasswordAuthenticationProvider(userstore, groupstore);
    }

    @Test(expected=AuthenticationServiceException.class)
    public void checkThatUnexpectedUserstoreExceptionThrowsAuthenticationServiceException() throws UnknownUserException, InvalidTokenException, ExpiredTokenException, InvalidCredentialsException {
        //Given
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("anyUser", "password");
        when(userstore.authenticate("anyUser", "password")).thenThrow(new RuntimeException("Userstore is offline"));
        
        //When
        provider.authenticate(token);
        
        //Then
        fail("Expected a service exception");
    }
}
