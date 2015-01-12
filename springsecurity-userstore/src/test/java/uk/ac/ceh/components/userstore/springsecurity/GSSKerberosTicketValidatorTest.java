package uk.ac.ceh.components.userstore.springsecurity;

import java.io.File;
import javax.security.auth.login.LoginException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author cjohn
 */
public class GSSKerberosTicketValidatorTest {
    @Test
    public void checkThatCanObtainSPNName() throws LoginException {
        //Given
        String spn = "HTTP/somewhere.i.want@AD.DOMAIN";
        GSSKerberosTicketValidator validator = new GSSKerberosTicketValidator(new File("toSomething"), spn);
        
        //When
        String servicePrincipalDomain = validator.getServicePrincipalDomain();
        
        //Then
        assertEquals("Expected the supplied domain", "somewhere.i.want", servicePrincipalDomain);
    }
}
