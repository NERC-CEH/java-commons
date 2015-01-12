package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import java.util.List;
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
public class NtlmAuthenticationEntryPointTest {
    @Test
    public void checkThatEntryPointInitiatesNegotiate() throws IOException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = mock(AuthenticationException.class);
        NtlmAuthenticationEntryPoint entryPoint = new NtlmAuthenticationEntryPoint();
        
        //When
        entryPoint.commence(request, response, exception);
        
        //Then
        List<String> headers = response.getHeaders("WWW-Authenticate");
        assertTrue("Expected to see the negotiate header", headers.contains("Negotiate"));
        assertTrue("Expected to see the negotiate header", headers.contains("NTLM"));
        assertEquals("Expected a 401 response", 401, response.getStatus());
    }
}
