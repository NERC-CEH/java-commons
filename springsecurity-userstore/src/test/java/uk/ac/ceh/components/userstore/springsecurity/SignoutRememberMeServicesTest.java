package uk.ac.ceh.components.userstore.springsecurity;

import javax.servlet.http.Cookie;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.util.CookieGenerator;

/**
 *
 * @author cjohn
 */
public class SignoutRememberMeServicesTest {
    @Mock Authentication signedOut;
    @Mock RememberMeServices rememberMeServices;
    @Mock LogoutHandler logoutHandler;
    @Mock CookieGenerator cookieGenerator;
    private SignoutRememberMeServices signOutServices;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        signOutServices = new SignoutRememberMeServices(signedOut, rememberMeServices, logoutHandler, cookieGenerator);
    }
    
    @Test
    public void signedOutAuthenticationIfRememberMeServicesFailsAndSignOutCookieIsSet() {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(cookieGenerator.getCookieName()).thenReturn("name");
        request.setCookies(new Cookie("name", signOutServices.getSignoutValue()));
        
        //When
        Authentication auth = signOutServices.autoLogin(request, response);
        
        //Then
        assertSame("Expected the signedOut authentication", auth, signedOut);
        verify(cookieGenerator, never()).removeCookie(response);
    }
    
    @Test
    public void removedSignedOutCookieIfSuccesfulAuthentication() {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(cookieGenerator.getCookieName()).thenReturn("name");
        request.setCookies(new Cookie("name", signOutServices.getSignoutValue()));
        Authentication successfulAuth = mock(Authentication.class);
        when(rememberMeServices.autoLogin(request, response)).thenReturn(successfulAuth);
        
        //When
        Authentication auth = signOutServices.autoLogin(request, response);
        
        //Then
        assertSame("Expecte the successful auth", auth, successfulAuth);
        verify(cookieGenerator).removeCookie(response); //Cookie was removed
    }
    
    @Test
    public void succesfulAuthenticationDoesntRemoveCookieIfNotSet() {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication successfulAuth = mock(Authentication.class);
        when(rememberMeServices.autoLogin(request, response)).thenReturn(successfulAuth);
        
        //When
        Authentication auth = signOutServices.autoLogin(request, response);
        
        //Then
        assertSame("Expecte the successful auth", auth, successfulAuth);
        verify(cookieGenerator, never()).removeCookie(response); //Cookie was removed
    }
    
    @Test
    public void succesfulLoginRemovesCookie() {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(cookieGenerator.getCookieName()).thenReturn("name");
        request.setCookies(new Cookie("name", signOutServices.getSignoutValue()));
        Authentication successfulAuth = mock(Authentication.class);
        
        //When
        signOutServices.loginSuccess(request, response, successfulAuth);
        
        //Then
        verify(cookieGenerator).removeCookie(response); //Cookie was removed
        verify(rememberMeServices).loginSuccess(request, response, successfulAuth);
    }
    
    @Test
    public void succesfulLoginDoesntRemoveCookieIfNotSet() {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication successfulAuth = mock(Authentication.class);
        
        //When
        signOutServices.loginSuccess(request, response, successfulAuth);
        
        //Then
        verify(cookieGenerator, never()).removeCookie(response); //Cookie was removed
        verify(rememberMeServices).loginSuccess(request, response, successfulAuth);
    }
    
    @Test
    public void logingOutSetsSignoutCookie() {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication logoutAuth = mock(Authentication.class);
        
        //When
        signOutServices.logout(request, response, logoutAuth);
        
        //Then
        verify(cookieGenerator).addCookie(response, signOutServices.getSignoutValue());
        verify(logoutHandler).logout(request, response, logoutAuth);
    }
    
    @Test
    public void loginFailSetsSignoutCookieAndDelegatesToRememberMeServices() {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        //When
        signOutServices.loginFail(request, response);
        
        //Then
        verify(cookieGenerator).addCookie(response, signOutServices.getSignoutValue());
        verify(rememberMeServices).loginFail(request, response);
    }
}
