package uk.ac.ceh.components.userstore.springsecurity;

import java.io.IOException;
import javax.servlet.ServletException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.ac.ceh.components.userstore.UnknownUserException;

/**
 *
 * @author cjohn
 */
public class HostnameKerberosTicketValidatorSelectorTest {
    @Test
    public void checkThatCanPickTheCorrectTicketValidator() throws Exception {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("domain.two");
        
        KerberosTicketValidator validator1 = mock(KerberosTicketValidator.class);
        KerberosTicketValidator validator2 = mock(KerberosTicketValidator.class);
        when(validator1.getServicePrincipalHostname()).thenReturn("domain.one");
        when(validator2.getServicePrincipalHostname()).thenReturn("domain.two");
        
        HostnameKerberosTicketValidatorSelector selector = new HostnameKerberosTicketValidatorSelector(
                validator1, validator2
        );
        
        //When
        KerberosTicketValidator validator = selector.selectValidator(request);
        
        //Then
        assertEquals("Expected second validator", validator, validator2);
    }
    
    @Test
    public void checkThatUnsupportedDomainContinuesChain() throws Exception {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("domain.somethingelse");
        
        KerberosTicketValidator validator1 = mock(KerberosTicketValidator.class);
        when(validator1.getServicePrincipalHostname()).thenReturn("domain.one");
        
        HostnameKerberosTicketValidatorSelector selector = new HostnameKerberosTicketValidatorSelector(
                validator1
        );
        
        //When
        KerberosTicketValidator validator = selector.selectValidator(request);
        
        //Then
        assertNull("Expected no validator", validator);
    }
    
    @Test
    public void checkThatWorksWhenNoServernameIsPresented() {
         //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        KerberosTicketValidator validator1 = mock(KerberosTicketValidator.class);
        when(validator1.getServicePrincipalHostname()).thenReturn("domain.one");
        
        HostnameKerberosTicketValidatorSelector selector = new HostnameKerberosTicketValidatorSelector(
                validator1
        );
        
        //When
        KerberosTicketValidator validator = selector.selectValidator(request);
        
        //Then
        assertNull("Expected no validator", validator);
    }
}
