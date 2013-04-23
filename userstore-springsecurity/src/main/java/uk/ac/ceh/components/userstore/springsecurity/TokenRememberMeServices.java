package uk.ac.ceh.components.userstore.springsecurity;

import com.google.common.collect.Collections2;
import java.nio.ByteBuffer;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
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
 * The following class defines a Token based RememberMeServices with a Logout
 * capability. This class will store principles into Spring Securities Authentication
 * object as objects of type U. This means that the given UserStore implementation is
 * what defines the types of Principles returned from calls to 
 * {@link org.springframework.security.core.Authentication#getPrinciple}
 * 
 * Ultimately the implementation will create cookies which will be sent to some 
 * browser. These cookies will contain a token representation generated by the provided
 * TokenGenerator. The other details of the Cookie (such as domain, name and ttl) are
 * provided by an instance of {@link org.springframework.web.util.CookieGenerator}
 * 
 * @author Rod Scott, Christopher Johnson
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

    /**
     * The following method performs an automatic login. If the given request object
     * contains a Cookie which could have been created by the CookieGenerator of this
     * object. Then this will be read, converted from a Token into a message. This 
     * message will be the username of who should be logged in. The user can then
     * be looked up from the userstore and returned in an RememberMeAuthenticationToken
     * 
     * If the provided cookie was invalid/expired/representing an unknown user, the 
     * response will be told to delete that cookie.
     * 
     * @param request to read the cookie from
     * @param response to delete any invalid cookies from
     * @return An authentication containing the user for the request, else null if no 
     *  user is logged in
     */
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
    
    /**
     * The following method will be invoked by spring security in the event that a
     * user has been successfully authenticated. At this point we can create a token
     * to be set in the response.
     * @param request not actually used in this implementation
     * @param response to add the cookie to
     * @param successfulAuthentication The successful authentication object which triggered
     *  this loginSuccess
     */
    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        U user = (U)successfulAuthentication.getPrincipal();
        Token token = tokenGenerator.generateToken(
                                            ByteBuffer.wrap(user.getUsername().getBytes()),
                                            cookieGenerator.getCookieMaxAge() * 1000);
        
        String tokenBase64 = Base64.encodeBase64URLSafeString(token.getBytes());
        cookieGenerator.addCookie(response, tokenBase64);
    }

    /**
     * The following method will be called by spring security when an logout request
     * has been initiated. This implementation will delete any associated cookies
     * for that user.
     * @param request
     * @param response
     * @param authentication 
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        cookieGenerator.removeCookie(response);
    }
    
    /**
     * No implementation is provided. If a user login fails, there is nothing for
     * us to do here.
     * @param request
     * @param response 
     */
    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {}
}