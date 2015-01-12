package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * The following abstract base class for AuthenticationFilters which handle 
 * requests from the SPNEGO pseudo mechanism. SPNEGO covers both Kerberos 
 * authentication and NTLM SSP authentication.
 * 
 * SPNEGO authentication methods have the potential to automatically authenticate
 * without client intervention. This feature can be controlled using the autoLogin
 * attribute of subclasses.
 * @author cjohn
 */
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class AbstractSpnegoAuthenticationFilter extends OncePerRequestFilter {
    private AuthenticationEntryPoint entryPoint = new NegotiateAuthenticationEntryPoint();
    private RequestMatcher requestMatcher = AnyRequestMatcher.INSTANCE;
    private boolean skipIfAlreadyAuthenticated = true;
    private boolean autoLogin = true;
    
    @Override
    protected final void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authorization authorization = getAuthorization(request);
        if(authorization != null && isAuthenticatable(authorization)) {
            doAuthentication(authorization, request, response, filterChain);
        }
        else if(autoLogin) {
            //Browser has not sent authenticatable details to initiate a request
            //If autoLogin is true, kick off the authentication protocol
            entryPoint.commence(request, response, new AuthenticationCredentialsNotFoundException("Initiate automatic login"));
        }
        else {
            //Otherwise just continue with the filter chain
            filterChain.doFilter(request, response);
        }
    }
    
    /**
     * Sub classes should implement this method with bodies which return true if
     * this filter is capable of authenticating the given authorization object.
     * Usually this will be based on a check on the mechanism used for Authorization.
     * 
     * @param authorization to check for compatibility
     * @return true if this filter can process the authorization object
     */
    protected abstract boolean isAuthenticatable(Authorization authorization);
    
    /**
     * Performs the actual SPNEGO authentication of the supplied authorization object.
     * 
     * This method will only be called after a isAuthenticatable call which 
     * returns true.
     * 
     * @param authorization which triggered this method
     * @param request initial request
     * @param response response to write to
     * @param filterChain the filter chain for this current request
     * @throws ServletException
     * @throws IOException 
     */
    protected abstract void doAuthentication(Authorization authorization, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException;
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return (skipIfAlreadyAuthenticated && isAuthenticated()) || !requestMatcher.matches(request);
    }
    
    /**
     * Decide if the current Security Context contains an authenticated principal
     * @return if the Spring Security Context is populated with an authenticated
     *  principal
     */
    protected boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }
    
    //Private method which will extract the authorization token from the supplied
    //HttpServletRequest if it is present. This method will return an Authorization
    //object if the supplied Authorization header contains a mechanism followed
    //by a space and then a base64 encoded token.
    private Authorization getAuthorization(HttpServletRequest request) throws UnsupportedEncodingException {
        String header = request.getHeader("Authorization");
        if(header != null) {
            String[] parts = StringUtils.split(header, " ");
            if(parts != null) {
                byte[] token64 = parts[1].getBytes("UTF-8");
                if(Base64.isBase64(token64)) {
                    return new Authorization(parts[0], Base64.decode(token64));
                }   
            }
        }
        return null;
    }
    
    @Data
    public static class Authorization {
        private final String mechanism;
        private final byte[] token;
        
        /**
        * Determine if the given this authorization is of the NTLMSSP type
        * @return if the token is ntlm ssp
        */
       public boolean isNtlmSSP() {
           return token.length > 6 &&
                  token[0] == 'N' &&
                  token[1] == 'T' &&
                  token[2] == 'L' &&
                  token[3] == 'M' &&
                  token[4] == 'S' &&
                  token[5] == 'S' &&
                  token[6] == 'P';
       }
    }
}
