package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import static uk.ac.ceh.components.userstore.springsecurity.SpnegoAuthenticationFilter.isSpnegoRequest;

/**
 *
 * @author cjohn
 */
public class SpnegoAuthenticationFilterTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock RememberMeServices rememberMeServices;
    @Mock(answer=RETURNS_DEEP_STUBS) SecurityContext securityContext;
    
    SpnegoAuthenticationFilter filter;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        SecurityContextHolder.setContext(securityContext);
        filter = new SpnegoAuthenticationFilter(authenticationManager);
        filter.setRememberMeServices(rememberMeServices);
    }
    
    @Test
    public void checkThatCanDecideIfAuthenticated() {
        //Given
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(auth);
        
        //When
        boolean isAuthenticated = SpnegoAuthenticationFilter.isAuthenticated();
        
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
        boolean isAuthenticated = SpnegoAuthenticationFilter.isAuthenticated();
        
        //Then
        assertFalse("The user is not authenticated", isAuthenticated);
    }
    
    @Test
    public void checkThatCanDecodeToken() throws IOException {
        //Given
        String token = "YmFzZSA2NCBpcyB0aGUgYmVzdA==";
        
        //When
        KerberosServiceRequestToken serviceToken = filter.createToken(token);
        
        //Then
        assertNotNull("NotNull", serviceToken.getToken());
    }
    
    @Test
    public void checkThatSpnegoRequestIsDetected() {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Negotiate blah");
        
        //When
        boolean isSpnego = isSpnegoRequest(request);
        
        //Then
        assertTrue("excepted to be spnego", isSpnego);
    }
    
    @Test
    public void checkThatNonSpnegoRequestIsNotDetected() {
         //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        //When
        boolean isSpnego = isSpnegoRequest(request);
        
        //Then
        assertFalse("excepted not to be spnego", isSpnego);
    }
    
    @Test(expected=BadCredentialsException.class)
    public void checkThatGibberishCausesException() throws IOException {
        //Given
        String token = "in****Token";
        
        //When
        filter.createToken(token);
        
        //Then
        fail("Expected to get exception");        
    }
    
    @Test
    public void checkThatSpnegoCausesAuthenticationAndIsRemembered() throws ServletException, IOException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        request.addHeader("Authorization", "Negotiate blah");
        
        //When
        filter.doFilterInternal(request, response, chain);
        
        //Then
        verify(authenticationManager).authenticate(any(KerberosServiceRequestToken.class));
        verify(rememberMeServices).loginSuccess(eq(request), eq(response), any(KerberosServiceRequestToken.class));
        verify(chain).doFilter(request, response);
    }
    
    @Test
    public void checkThatNonSpnegoIsStillFiltered() throws ServletException, IOException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        
        //When
        filter.doFilterInternal(request, response, chain);
        
        //Then
        verify(chain).doFilter(request, response);
    }
}
