package uk.ac.ceh.components.userstore.springsecurity;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

/**
 * Automated sign on filters will automatically log you in if no authentication 
 * object is found in the security context. This means, if you log out of a cookie
 * based RememberMeServices, you will be instantly logged back in. This Signout
 * Remember Me Services will remember the intent of signing out as a cookie.
 * 
 * If the sign out cookie is present, and the wrapped autoLogin method doesn't 
 * return an Authentication, the user will be presented with a given Anonymous 
 * Authentication
 * @author Christopher Johnosn
 */
@Data
public class SignoutRememberMeServices implements RememberMeServices, LogoutHandler {
    private final Authentication signedOut;
    private final RememberMeServices rememberMeServices;
    private final LogoutHandler logoutHandler;
    private final CookieGenerator cookieGenerator;
    private String signoutValue = "goodbye";
    
    /***
     * Delegate to the wrapped rememberMeServices instance, if it succeeds in
     * generating an authentication object then this method will return it and also
     * perform remove the signout cookie. If it fails and there is a signout cookie
     * in place, the signedOut authentication will be returned
     * @param request to read the cookie from
     * @param response to set a signed out cookie on
     * @return The response from rememberMeServices.autoLogin or the signedOut 
     *  authentication
     */
    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, cookieGenerator.getCookieName());
        Authentication authentication = rememberMeServices.autoLogin(request, response);
        if(authentication == null && cookie != null && signoutValue.equals(cookie.getValue())) {
            return signedOut;
        }
        else {
            removeCookie(request, response);
            return authentication;
        }
    }
    
    /**
     * Request that the signedout cookie is removed (if present) when a successful
     * authentication has been performed by the wrapped remember me services.
     * 
     * This method delegates to the loginSuccess method of the wrapped 
     * RememberMeServices
     * @param request to look for the sign out cookie
     * @param response to remove the sign  
     * @param successfulAuthentication the successful authentication object which 
     * caused this loginSuccess method to be called
     */
    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        if(successfulAuthentication != signedOut) {
            removeCookie(request, response);
        }
        rememberMeServices.loginSuccess(request, response, successfulAuthentication);
    }
    
    /**
     * When a logout action has been performed, we will set the signout cookie
     * and then call the wrapped LogoutHandler 
     * @param request 
     * @param response
     * @param authentication
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        cookieGenerator.addCookie(response, signoutValue); //Logged out, add the signout intent cookie
        logoutHandler.logout(request, response, authentication); //Perform the upstream logout
    }

    /**
     * If an automated attempt to login fails, we can set the signout cookie. 
     * This will stop further automated attempts to login from occuring
     * @param request
     * @param response 
     */
    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {
        cookieGenerator.addCookie(response, signoutValue); //Logged out, add the signout intent cookie
        rememberMeServices.loginFail(request, response);
    }
    
    /**
     * Removes the sign out cookie, only if it is present
     */
    private void removeCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, cookieGenerator.getCookieName());
        if(cookie != null) {
            cookieGenerator.removeCookie(response);
        }
    }
}
