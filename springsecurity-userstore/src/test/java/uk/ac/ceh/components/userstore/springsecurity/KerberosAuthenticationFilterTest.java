package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import static org.junit.Assert.assertEquals;
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
import uk.ac.ceh.components.userstore.UnknownUserException;

/**
 *
 * @author cjohn
 */
public class KerberosAuthenticationFilterTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock KerberosTicketValidator ticketValidator;
    @Mock RememberMeServices rememberMeServices;
    @Mock(answer=RETURNS_DEEP_STUBS) SecurityContext securityContext;
    
    KerberosAuthenticationFilter filter;
    MockHttpServletRequest request;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        SecurityContextHolder.setContext(securityContext);
        filter = new KerberosAuthenticationFilter(authenticationManager);
        filter.setRememberMeServices(rememberMeServices);
        
        //Set the default validators operating domain name and attach this the the default request
        when(ticketValidator.getServicePrincipalDomain()).thenReturn("default");
        filter.addTicketValidator(ticketValidator);
        request = new MockHttpServletRequest();
        request.setServerName("default");
    }
    
    @Test
    public void checkThatSpnegoCausesAuthenticationAndIsRemembered() throws ServletException, IOException {
        //Given
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
        verify(securityContext).setAuthentication(succesfulAuthentication);
        assertEquals("Expected username to be captured in token", captor.getValue().getName(), "cjohn");
    }
    
    @Test
    public void checkThatInvalidKerberosFailsButContinuesChain() throws UnknownUserException, ServletException, IOException {
        //Given
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
    public void checkThatCanPickTheCorrectTicketValidator() throws Exception {
        //Given
        request.addHeader("Authorization", "Negotiate NTLMJIBBERISH"); //SpnegoRequest
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        
        KerberosTicketValidator validator1 = mock(KerberosTicketValidator.class);
        KerberosTicketValidator validator2 = mock(KerberosTicketValidator.class);
        when(validator1.getServicePrincipalDomain()).thenReturn("domain.one");
        when(validator2.getServicePrincipalDomain()).thenReturn("domain.two");
        when(validator2.validateTicket(any(byte[].class))).thenReturn("username");
        
        request.setServerName("domain.two");
        
        filter.getTicketValidators().clear();
        filter.addTicketValidator(validator1);
        filter.addTicketValidator(validator2);
        
        //When
        filter.doFilterInternal(request, response, chain);
        
        //Then
        verify(validator2).validateTicket(any(byte[].class));
    }
    
    @Test
    public void checkThatUnsupportedDomainContinuesChain() throws UnknownUserException, ServletException, IOException {
        //Given
        request.setServerName("Somewhere.else");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        request.addHeader("Authorization", "Negotiate NTLMJIBBERISH"); //SpnegoRequest
        when(ticketValidator.validateTicket(any(byte[].class))).thenThrow(new BadCredentialsException("Invalid username"));
        
        //When
        filter.doFilterInternal(request, response, chain);
        
        //Then
        verify(chain).doFilter(request, response);
    }
}
