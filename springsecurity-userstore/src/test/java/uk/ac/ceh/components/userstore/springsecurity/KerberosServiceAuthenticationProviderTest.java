package uk.ac.ceh.components.userstore.springsecurity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserStore;

/**
 *
 * @author cjohn
 */
public class KerberosServiceAuthenticationProviderTest {
    @Mock KerberosTicketValidator ticketValidator;
    @Mock UserStore userStore;
    @Mock GroupStore groupStore;
    KerberosServiceAuthenticationProvider provider;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        provider = new KerberosServiceAuthenticationProvider(ticketValidator, userStore, groupStore);
    }
    
    @Test
    public void checkThatProviderSupportsKerberosTokens() {
        //Given
        Class tokenClass = KerberosServiceRequestToken.class;
        
        //When
        boolean supports = provider.supports(tokenClass);
        
        //Then
        assertTrue("Expected the provider to suppor the class", supports);
    }
    
    @Test
    public void checkThatCanAuthenticateToken() throws UnknownUserException {
        //Given
        byte[] tokenBytes = new byte[]{1,2,3,4};
        User user = mock(User.class);
        KerberosServiceRequestToken token = new KerberosServiceRequestToken(tokenBytes);
        when(ticketValidator.validateTicket(tokenBytes)).thenReturn("username");
        when(userStore.getUser("username")).thenReturn(user);
        
        //When
        Authentication newAuth = provider.authenticate(token);
        
        //Then
        assertTrue(newAuth.isAuthenticated());
        assertEquals(newAuth.getPrincipal(), user);
    }
    
    @Test(expected=UsernameNotFoundException.class)
    public void checkThatInvalidUsernameThrowsException() throws UnknownUserException {
        //Given
        byte[] tokenBytes = new byte[]{1,2,3,4};
        KerberosServiceRequestToken token = new KerberosServiceRequestToken(tokenBytes);
        when(ticketValidator.validateTicket(tokenBytes)).thenReturn("username");
        when(userStore.getUser("username")).thenThrow(new UnknownUserException("Who are you after?"));
        
        //When
        provider.authenticate(token);
        
        //Then
        fail("Expected an exception");        
    }
}
