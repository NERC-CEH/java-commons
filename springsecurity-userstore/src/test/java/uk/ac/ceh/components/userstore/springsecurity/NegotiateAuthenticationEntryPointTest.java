package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

/**
 *
 * @author cjohn
 */
public class NegotiateAuthenticationEntryPointTest {
    @Test
    public void checkThatEntryPointInitiatesNegotiate() throws IOException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = mock(AuthenticationException.class);
        NegotiateAuthenticationEntryPoint entryPoint = new NegotiateAuthenticationEntryPoint();
        
        //When
        entryPoint.commence(request, response, exception);
        
        //Then
        assertTrue("Expected to see the negotiate header", response.getHeaders("WWW-Authenticate").contains("Negotiate"));
        assertEquals("Expected a 401 response", 401, response.getStatus());
    }
}
