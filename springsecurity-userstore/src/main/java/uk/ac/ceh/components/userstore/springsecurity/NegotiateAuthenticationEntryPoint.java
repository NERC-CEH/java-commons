package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * An authentication entry point which is used to initiate an authentication 
 * using the negotiate mechanism
 * @see KerberosAuthenticationFilter
 * @author cjohn
 */
public class NegotiateAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Returns a 401 Unauthorized status to the given response along with the 
     * negotiate header
     *
     * @param request The current request we are dealing with
     * @param response To send the 401 response to
     * @param authException which triggered this entry point
     * @throws IOException If an issue occurs whilst writing to the http response
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        //Not logged in and not presented with a Negotiate Token. Request 
        //the browser, performs authentication.
        response.addHeader("WWW-Authenticate", "Negotiate");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
