package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.ntlmv2.liferay.NtlmLogonException;
import org.ntlmv2.liferay.NtlmManager;
import org.ntlmv2.liferay.NtlmUserAccount;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import uk.ac.ceh.components.userstore.springsecurity.AbstractSpnegoAuthenticationFilter.Authorization;

/**
 *
 * @author cjohn
 */
public class NtlmAuthenticationFilterTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock NtlmManager ntlmManager;
    @Mock RememberMeServices rememberMeServices;
    @Mock(answer=RETURNS_DEEP_STUBS) SecurityContext securityContext;
    
    NtlmAuthenticationFilter filter;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        SecurityContextHolder.setContext(securityContext);
        filter = new NtlmAuthenticationFilter(authenticationManager, ntlmManager);
        filter.setRememberMeServices(rememberMeServices);
    }
    
    @Test
    public void checkThatCanAuthenticateNegotiateRequests() {
        //Given
        Authorization auth = new Authorization("Negotiate", new byte[0]);
        
        //When
        boolean authenticatable = filter.isAuthenticatable(auth);
        
        //Then
        assertTrue("Expected to be able to authenticate", authenticatable);
    }
        
    @Test
    public void checkThatCanAuthenticateNTLMRequests() {
        //Given
        Authorization auth = new Authorization("NTLM", new byte[0]);
        
        //When
        boolean authenticatable = filter.isAuthenticatable(auth);
        
        //Then
        assertTrue("Expected to be able to authenticate", authenticatable);
    }
        
    @Test
    public void checkThatCantAuthenticateGibberishRequests() {
        //Given
        Authorization auth = new Authorization("Gibber", new byte[0]);
        
        //When
        boolean authenticatable = filter.isAuthenticatable(auth);
        
        //Then
        assertFalse("Expected to not be able to authenticate", authenticatable);
    }
    
    @Test
    public void checkThatNtlmType1MessageReturnsAType2Message() throws ServletException, IOException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        byte[] type1 = new byte[]{'N','T','L','M','S','S','P', '\0', 1};
        Authorization auth = new Authorization("MECH", type1);
        
        byte[] type2 = new byte[]{'N','T','L','M','S','S','P', '\0', 2};
                  
        when(ntlmManager.negotiate(eq(type1), any(byte[].class))).thenReturn(type2);
        
        //When
        filter.doAuthentication(auth, request, response, chain);
        
        //Then
        HttpSession session = request.getSession(false);
        assertEquals("Expected type 2 response with mechanism", response.getHeader("WWW-Authenticate"), "MECH TlRMTVNTUAAC");
        assertEquals("Expected 401 response", response.getStatus(), 401);
        assertNotNull("Expected session", session);
        assertNotNull("Expected Challenge in session", session.getAttribute(filter.getChallengeAttribute()));
    }
    
    @Test
    public void checkThatAuthenticatesOnType3Message() throws Exception {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        byte[] serverChallenge = new byte[]{1,2,3,4,5,6,7,8};
        Authentication successfulAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(successfulAuthentication);
        
        request.getSession().setAttribute(filter.getChallengeAttribute(), serverChallenge);

        byte[] type3 = new byte[]{'N','T','L','M','S','S','P', '\0', 3};
        Authorization auth = new Authorization("MECH", type3);
        
        when(ntlmManager.authenticate(any(byte[].class), any(byte[].class))).thenReturn(new NtlmUserAccount("superadmin"));
        //When
        filter.doAuthentication(auth, request, response, chain);
        
        //Then
        HttpSession session = request.getSession(false);
        assertNull("Expected Challenge to be removed session", session.getAttribute(filter.getChallengeAttribute()));

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("expected ntlm user", captor.getValue().getPrincipal(), "superadmin");
        verify(rememberMeServices).loginSuccess(request, response, successfulAuthentication);
    }
    
    @Test
    public void checkThatIgnoresNonNTLMSSPMessages() throws Exception {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        
        byte[] nonNtlm = "NotNtlm".getBytes();
        Authorization auth = new Authorization("MECH", nonNtlm);
        
        //When
        filter.doAuthentication(auth, request, response, chain);
        
        //Then
        verify(chain).doFilter(request, response);
    }
    
    @Test
    public void checkThatIgnoresIfSessionWasNotCreated() throws Exception {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        
        byte[] type3 = new byte[]{'N','T','L','M','S','S','P', '\0', 3};
        Authorization auth = new Authorization("MECH", type3);
        
        //When
        filter.doAuthentication(auth, request, response, chain);
        
        //Then
        verify(chain).doFilter(request, response);
    }
    
    @Test
    public void checkThatReEntersOnAuthenicationException() throws Exception {
        //Given
        AuthenticationEntryPoint entryPoint = mock(AuthenticationEntryPoint.class);
        filter.setEntryPoint(entryPoint);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        byte[] serverChallenge = new byte[]{1,2,3,4,5,6,7,8};
        request.getSession().setAttribute(filter.getChallengeAttribute(), serverChallenge);
        byte[] type3 = new byte[]{'N','T','L','M','S','S','P', '\0', 3};
        Authorization auth = new Authorization("MECH", type3);
        
        when(ntlmManager.authenticate(any(byte[].class), any(byte[].class))).thenThrow(new NtlmLogonException("Failed") );
        
        //When
        filter.doAuthentication(auth, request, response, chain);
        
        //Then
        verify(entryPoint).commence(eq(request), eq(response), any(AuthenticationException.class));
    }
}
