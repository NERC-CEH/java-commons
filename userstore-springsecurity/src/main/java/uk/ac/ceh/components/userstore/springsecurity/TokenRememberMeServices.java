package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.collect.Collections2;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;
import uk.ac.ceh.components.tokengeneration.ExpiredTokenException;
import uk.ac.ceh.components.tokengeneration.InvalidTokenException;
import uk.ac.ceh.components.tokengeneration.Token;
import uk.ac.ceh.components.tokengeneration.TokenGenerator;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserStore;

/**
 * Modify {@link org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices} to use {@link uk.ac.ceh.components.tokenauthentication.TokenAuthenticator}
 * rather than the provided token encryption.
 * @author Rod Scott
 */
public class TokenRememberMeServices<U extends User & Roled> implements RememberMeServices, LogoutHandler {
    private final TokenGenerator tokenGenerator;
    private final String key;
    private final UserStore<U> userStore;
    private final CookieGenerator cookieGenerator;

    public TokenRememberMeServices( String key, 
                                    UserStore<U> userStore, 
                                    TokenGenerator tokenGenerator, 
                                    CookieGenerator cookieGenerator) {
        this.tokenGenerator = tokenGenerator;
        this.userStore = userStore;
        this.key = key;
        this.cookieGenerator = cookieGenerator;
    }

    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, cookieGenerator.getCookieName());
        if(cookie != null) {
            try {
                Token token = new Token(Base64.decodeBase64(cookie.getValue())); //get token from request
                ByteBuffer message = tokenGenerator.getMessage(token);
                byte[] messageBytes = new byte[message.remaining()];
                message.get(messageBytes);
                U user = userStore.getUser(new String(messageBytes));

                return new RememberMeAuthenticationToken(key, user, Collections2.transform(user.getRoles(), new TransformRoleToSimpleGrantedAuthority()));
            }
            catch (InvalidTokenException | ExpiredTokenException | UnknownUserException ex) {
                cookieGenerator.removeCookie(response); //failed login. Request a cookie delete
            }
        }
        return null;
    }
    
    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        U user = (U)successfulAuthentication.getPrincipal();
        Token token = tokenGenerator.generateToken(
                                            ByteBuffer.wrap(user.getUsername().getBytes()),
                                            cookieGenerator.getCookieMaxAge() * 1000);
        
        String tokenBase64 = Base64.encodeBase64URLSafeString(token.getBytes());
        cookieGenerator.addCookie(response, tokenBase64);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        cookieGenerator.removeCookie(response);
    }
    
    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {}
}