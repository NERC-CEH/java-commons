package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 
 * The following request will handle Kerberos authentication, by either starting
 * the authentication process or validating a kerberos Authorization header.
 * 
 * This filter takes a request matcher, this allows Kerberos automated sign on
 * to only be applied to requests which match the request matcher. For example
 * this could be an ipaddress range (or whatever request matcher is applicable)
 * 
 * @author cjohn
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class KerberosAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;
    private final KerberosTicketValidator ticketValidator;
    private final RequestMatcher matcher;
    private RememberMeServices rememberMeServices = new NullRememberMeServices();
    private boolean skipIfAlreadyAuthenticated = true;
    private boolean stripRealm = true;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(isSpnegoRequest(request)) {
            String header = request.getHeader("Authorization");
            try {
                PreAuthenticatedAuthenticationToken token = createToken(header.substring(10));
                Authentication auth = authenticationManager.authenticate(token);
                rememberMeServices.loginSuccess(request, response, auth);
            }
            catch(AuthenticationException ae) {
                //Authentication failed. Let the remember me services know and carry on
                rememberMeServices.loginFail(request, response);
            }
            
            filterChain.doFilter(request, response);
        }
        else {
            response.addHeader("WWW-Authenticate", "Negotiate");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return (skipIfAlreadyAuthenticated && isAuthenticated()) || !matcher.matches(request);
    }
    
    /**
     * Given a Token represented as Base64, decode it, validate it and return a 
     * PreAuthenticatedAuthenticationToken
     * @param token
     * @return A KerberosServiceRequestToken
     * @throws java.io.IOException if UTF-8 is not supported (This shouldn't happen
     *  as UTF-8 is a JVM requirement
     */
    protected PreAuthenticatedAuthenticationToken createToken(String token) throws IOException {
        try {
            byte[] ticket = Base64.decode(token.getBytes("UTF-8"));
            String username = stripRealm(ticketValidator.validateTicket(ticket));
            return new PreAuthenticatedAuthenticationToken(username, ticket);
        }
        catch(IllegalArgumentException iae) {
            throw new BadCredentialsException("Failed to decode kerberos authentication ticket");
        }
    }
    
    /*
     * Simple method to check if the given request represents a Spenego request 
     */
    protected boolean isSpnegoRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Negotiate ");
    }
    
    protected boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }
    
    /*
     * Simple method to remove any realm information from a given username if
     * stripRealm is set to true
     */
    private String stripRealm(String username) {
        int indexOfAt = username.lastIndexOf('@');
        if(stripRealm && indexOfAt != -1) {
            return username.substring(0, indexOfAt);
        }
        else {
            return username;
        }
    }
}
