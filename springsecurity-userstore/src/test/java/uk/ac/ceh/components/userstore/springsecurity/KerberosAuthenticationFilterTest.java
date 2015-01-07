package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.util.matcher.RequestMatcher;
import uk.ac.ceh.components.userstore.UnknownUserException;

/**
 *
 * @author cjohn
 */
public class KerberosAuthenticationFilterTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock KerberosTicketValidator ticketValidator;
    @Mock RememberMeServices rememberMeServices;
    @Mock RequestMatcher matcher;
    @Mock(answer=RETURNS_DEEP_STUBS) SecurityContext securityContext;
    
    KerberosAuthenticationFilter filter;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        SecurityContextHolder.setContext(securityContext);
        filter = new KerberosAuthenticationFilter(authenticationManager, ticketValidator, matcher);
        filter.setRememberMeServices(rememberMeServices);
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
    public void checkThatCanDecodeToken() throws IOException {
        //Given
        String token = "YmFzZSA2NCBpcyB0aGUgYmVzdA==";
        when(ticketValidator.validateTicket(any(byte[].class))).thenReturn("username@ad");
        
        //When
        PreAuthenticatedAuthenticationToken serviceToken = filter.createToken(token);
        
        //Then
        assertNotNull("NotNull", serviceToken.getName());
    }
    
    @Test
    public void checkThatSpnegoRequestIsDetected() {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Negotiate blah");
        
        //When
        boolean isSpnego = filter.isSpnegoRequest(request);
        
        //Then
        assertTrue("excepted to be spnego", isSpnego);
    }
    
    @Test
    public void checkThatNonSpnegoRequestIsNotDetected() {
         //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        //When
        boolean isSpnego = filter.isSpnegoRequest(request);
        
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
        when(ticketValidator.validateTicket(any(byte[].class))).thenReturn("username@ad");
        
        //When
        filter.doFilterInternal(request, response, chain);
        
        //Then
        verify(authenticationManager).authenticate(any(PreAuthenticatedAuthenticationToken.class));
        verify(rememberMeServices).loginSuccess(eq(request), eq(response), any(PreAuthenticatedAuthenticationToken.class));
        verify(chain).doFilter(request, response);
    }
    
    @Test
    public void checkThatNonSpnegoCausesNegotiate401() throws ServletException, IOException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        
        //When
        filter.doFilterInternal(request, response, chain);
        
        //Then
        assertEquals("Expected www auth header", response.getHeader("WWW-Authenticate"), "Negotiate");
        assertEquals("Expected 401 error", response.getStatus(), 401);
        verify(chain,never()).doFilter(request, response);
    }
    
    @Test
    public void checkThatCanAuthenticateKerberos() throws UnknownUserException, ServletException, IOException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        Authentication succesfulAuthentication = mock(Authentication.class);
        request.addHeader("Authorization", "Negotiate NTLMJIBBERISH"); //SpnegoRequest
        when(ticketValidator.validateTicket(any(byte[].class))).thenReturn("cjohn");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(succesfulAuthentication);
        
        //When
        filter.doFilterInternal(request, response, chain);
        
        //Then
        ArgumentCaptor<PreAuthenticatedAuthenticationToken> captor = ArgumentCaptor.forClass(PreAuthenticatedAuthenticationToken.class);
        verify(chain).doFilter(request, response);
        verify(authenticationManager).authenticate(captor.capture());
        verify(rememberMeServices).loginSuccess(request, response, succesfulAuthentication);
        assertEquals("Expected username to be captured in token", captor.getValue().getName(), "cjohn");
    }
    
    @Test
    public void checkThatInvalidKerberosFailsButContinuesChain() throws UnknownUserException, ServletException, IOException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        request.addHeader("Authorization", "Negotiate NTLMJIBBERISH"); //SpnegoRequest
        when(ticketValidator.validateTicket(any(byte[].class))).thenThrow(new BadCredentialsException("Invalid username"));
        
        //When
        filter.doFilterInternal(request, response, chain);
        
        //Then
        verify(rememberMeServices).loginFail(request, response);
        verify(chain).doFilter(request, response);
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
    
    @Test
    public void checkThatRealmInformationCanBeStrippedFromUsername() throws IOException {
        //Given
        when(ticketValidator.validateTicket(any(byte[].class))).thenReturn("someone@some.realm");
        filter.setStripRealm(true);
        
        //When
        PreAuthenticatedAuthenticationToken serviceToken = filter.createToken("");
        
        //Then
        assertEquals("Expected realm to be stripped", serviceToken.getPrincipal(), "someone");
    }
        
    @Test
    public void checkThatRealmInformationCanBeLeftInPlaceFromUsername() throws IOException {
        //Given
        when(ticketValidator.validateTicket(any(byte[].class))).thenReturn("someone@some.realm");
        filter.setStripRealm(false);
        
        //When
        PreAuthenticatedAuthenticationToken serviceToken = filter.createToken("");
        
        //Then
        assertEquals("Expected realm to be left in place", serviceToken.getPrincipal(), "someone@some.realm");
    }
}
