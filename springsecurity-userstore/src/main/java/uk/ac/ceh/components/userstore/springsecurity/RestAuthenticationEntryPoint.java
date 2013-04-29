package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * The following is a convenience class which can be used to make spring security
 * respond with an unauthorised status code rather than automatically redirecting
 * to the login page. This is useful for creating restful web services which are 
 * secured with spring security.
 * @author cjohn
 */
public final class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
 
   @Override
   public void commence(HttpServletRequest request, 
                        HttpServletResponse response, 
                        AuthenticationException authException) throws IOException{
      response.sendError( HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized" );
   }
}