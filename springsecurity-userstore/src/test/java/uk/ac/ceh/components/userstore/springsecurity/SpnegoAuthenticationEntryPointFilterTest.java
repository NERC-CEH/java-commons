package uk.ac.ceh.components.userstore.springsecurity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 *
 * @author cjohn
 */
public class SpnegoAuthenticationEntryPointFilterTest {
    @Mock RequestMatcher matcher;
    
    SpnegoAuthenticationEntryPointFilter filter;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        filter = new SpnegoAuthenticationEntryPointFilter(matcher);
    }
    
    @Test
    public void checkThatShouldNotFilterIfMatcherFails() {
        //Given        
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(matcher.matches(request)).thenReturn(false);
        
        //When
        boolean shouldNotFilter = filter.shouldNotFilter(request);
        
        //Then
        assertTrue("Don't filter if matcher fails", shouldNotFilter);
    }
    
    @Test
    public void checkThatShouldFilterIfMatcherPasses() {
        //Given        
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(matcher.matches(request)).thenReturn(true);
        
        //When
        boolean shouldNotFilter = filter.shouldNotFilter(request);
        
        //Then
        assertFalse("We should filter", shouldNotFilter);
    }
    
    @Test
    public void checkThatShouldNotFilterIfSpnegoRequest() {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Negotiate NTLMJIBBERISH"); //SpnegoRequest
        
        //When
        boolean shouldNotFilter = filter.shouldNotFilter(request);
        
        //Then
        assertTrue("Don't spgeno request", shouldNotFilter);
    }
    
    @Test
    public void checkThatAuthenticateHeaderIsSetWhenFiltering() throws Exception {
        //Given
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        //When
        filter.doFilterInternal(null, response, null);
        
        //Then
        assertEquals("Expected www auth header", response.getHeader("WWW-Authenticate"), "Negotiate");
        assertEquals("Expected 401 error", response.getStatus(), 401);
    }
}
