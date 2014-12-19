package uk.ac.ceh.components.userstore.springsecurity;

import javax.servlet.http.Cookie;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.util.CookieGenerator;
import uk.ac.ceh.components.tokengeneration.ExpiredTokenException;
import uk.ac.ceh.components.tokengeneration.InvalidTokenException;
import uk.ac.ceh.components.tokengeneration.Token;
import uk.ac.ceh.components.tokengeneration.TokenGenerator;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.UserStore;

/**
 *
 * @author cjohn
 */
public class TokenRememberMeServicesTest {
    @Mock TokenGenerator tokenGenerator;
    @Mock UserStore userStore;
    @Mock GroupStore groupStore;
    @Mock CookieGenerator cookieGenerator;
    
    private TokenRememberMeServices provider;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        provider = new TokenRememberMeServices("whatever", userStore, groupStore, tokenGenerator, cookieGenerator);
    }
    
    @Test(expected=AuthenticationServiceException.class)
    public void checkThatUnexpectedUserstoreExceptionThrowsAuthenticationServiceException() throws UnknownUserException, InvalidTokenException, ExpiredTokenException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new Cookie("cookie", "password"));
        when(cookieGenerator.getCookieName()).thenReturn("cookie");
        when(tokenGenerator.getMessage(any(Token.class))).thenReturn(new byte[]{});
        when(userStore.getUser("anyUser")).thenThrow(new RuntimeException("Userstore is offline"));
        
        //When
        provider.autoLogin(request, response);
        
        //Then
        fail("Expected a service exception");
    }
}
