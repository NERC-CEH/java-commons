package uk.ac.ceh.components.userstore.springsecurity;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import uk.ac.ceh.components.tokengeneration.ExpiredTokenException;
import uk.ac.ceh.components.tokengeneration.InvalidTokenException;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserStore;

/**
 *
 * @author cjohn
 */
public class PreAuthenticatedUsernameAuthenticationProviderTest {
    @Mock UserStore userstore;
    @Mock GroupStore groupstore;
    
    PreAuthenticatedUsernameAuthenticationProvider provider;
    
    @Before
    public void createProvider() {
        MockitoAnnotations.initMocks(this);
        provider = new PreAuthenticatedUsernameAuthenticationProvider(userstore, groupstore);
    }
    
    @Test
    public void checkCanAuthenticateSuppliedUsernameToken() throws UnknownUserException {
        //Given
        User user = mock(User.class);
        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("username", "credentials which are ignored");
        when(userstore.getUser("username")).thenReturn(user);
        
        //When
        Authentication authenticate = provider.authenticate(token);
        
        //Then
        assertSame("Expected to get user", authenticate.getPrincipal(), user);
        assertTrue("Excepted to be instance of PreAuthenticatedAuthenticationToken", authenticate instanceof PreAuthenticatedAuthenticationToken);
    }
    
    @Test(expected=UsernameNotFoundException.class)
    public void checkThatMissingUserThrowsUsernameException() throws UnknownUserException {
        //Given
        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("missinguser", "credentials which are ignored");
        when(userstore.getUser("missinguser")).thenThrow(new UnknownUserException("Missing user"));
        
        //When
        provider.authenticate(token);
        
        //Then
        fail("Expected to fail with exception");
    }
    
    
    @Test
    public void checkThatSupportsPreAuthenticatedAuthenticationToken() {
        //Given
        Class clazz = PreAuthenticatedAuthenticationToken.class;
                
        //When
        boolean supports = provider.supports(clazz);
        
        //Then
        assertTrue("Expected supports given token", supports);
    }

    @Test(expected=AuthenticationServiceException.class)
    public void checkThatUnexpectedUserstoreExceptionThrowsAuthenticationServiceException() throws UnknownUserException, InvalidTokenException, ExpiredTokenException {
        //Given
        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("anyUser", "credentials which are ignored");
        when(userstore.getUser("anyUser")).thenThrow(new RuntimeException("Userstore is offline"));
        
        //When
        provider.authenticate(token);
        
        //Then
        fail("Expected a service exception");
    }
}
