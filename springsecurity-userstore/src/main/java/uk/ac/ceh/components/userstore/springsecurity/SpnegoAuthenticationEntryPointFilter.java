package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import static uk.ac.ceh.components.userstore.springsecurity.SpnegoAuthenticationFilter.isSpnegoRequest;
import static uk.ac.ceh.components.userstore.springsecurity.SpnegoAuthenticationFilter.isAuthenticated;

/**
 * The following Request filter takes a request matcher to decide if we should 
 * attempt to kick of the Spnego Authentication Mechanism. The use of a 
 * RequestMatcher allows this Spnego to be applied to web applications accessible
 * from outside of the Kerberos network as these systems can be filtered out 
 * with an ipaddress range (or whatever request matcher is applicable)
 * 
 * This filter is very much similar to a Spring Security EntryPoint, however
 * being a filter means that we can apply the EntryPoint across the entire 
 * application, and still have a generic exceptionHandling entrypoint.
 * @author cjohn
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class SpnegoAuthenticationEntryPointFilter extends OncePerRequestFilter {
    private final RequestMatcher matcher;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        response.addHeader("WWW-Authenticate", "Negotiate");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return isAuthenticated() || !matcher.matches(request) || isSpnegoRequest(request);
    }
}
