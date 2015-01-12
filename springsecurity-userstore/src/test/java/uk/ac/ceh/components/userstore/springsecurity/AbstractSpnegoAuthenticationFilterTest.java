package uk.ac.ceh.components.userstore.springsecurity;

import javax.servlet.ServletException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import org.mockito.Mock;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import uk.ac.ceh.components.userstore.springsecurity.AbstractSpnegoAuthenticationFilter.Authorization;

/**
 *
 * @author cjohn
 */
public class AbstractSpnegoAuthenticationFilterTest {@Mock AuthenticationManager authenticationManager;
    @Mock RequestMatcher matcher;
    @Mock(answer=RETURNS_DEEP_STUBS) SecurityContext securityContext;
    
    AbstractSpnegoAuthenticationFilter filter;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        SecurityContextHolder.setContext(securityContext);
        filter = mock(AbstractSpnegoAuthenticationFilter.class, CALLS_REAL_METHODS);
        filter.setRequestMatcher(matcher);
    }
    
    @Test
    public void checkThatAuthorizationCanDetectNTLMSSP() {
        //Given
        byte[] ntlmssp = "NTLMSSP".getBytes();
        Authorization auth = new Authorization("NTLM", ntlmssp);
        
        //When
        boolean isNtlmssp = auth.isNtlmSSP();
        
        //Then
        assertTrue("Expected to be ntlmssp", isNtlmssp);
    }
    
    
    @Test
    public void checkThatAuthorizationDoesntDetectNonNTLMSSP() {
        //Given
        byte[] ntlmssp = "Kerberos".getBytes();
        Authorization auth = new Authorization("NTLM", ntlmssp);
        
        //When
        boolean isNtlmssp = auth.isNtlmSSP();
        
        //Then
        assertFalse("Expected not to be ntlmssp", isNtlmssp);
    }
    
    @Test
    public void checkThatCanDecideIfAuthenticated() {
        //Given
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(auth);
        
        //When
        boolean isAuthenticated = filter.isAuthenticated();
        
        //Then
        assertTrue("The user is authenticated", isAuthenticated);
    }
    
    @Test
    public void checkThatCanDecideIfIsntAuthenticated() {
        //Given
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(auth);
        
        //When
        boolean isAuthenticated = filter.isAuthenticated();
        
        //Then
        assertFalse("The user is not authenticated", isAuthenticated);
    }
    
    @Test
    public void checkThatShouldNotFilterIfMatcherFails() throws ServletException {
        //Given        
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(matcher.matches(request)).thenReturn(false);
        
        //When
        boolean shouldNotFilter = filter.shouldNotFilter(request);
        
        //Then
        assertTrue("Don't filter if matcher fails", shouldNotFilter);
    }
    
    @Test
    public void checkThatShouldFilterIfMatcherPasses() throws ServletException {
        //Given        
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(matcher.matches(request)).thenReturn(true);
        
        //When
        boolean shouldNotFilter = filter.shouldNotFilter(request);
        
        //Then
        assertFalse("We should filter", shouldNotFilter);
    }
}
