package uk.ac.ceh.components.userstore.crowd;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.userstore.UserAttribute;
import uk.ac.ceh.components.userstore.UserBuilder;
import uk.ac.ceh.components.userstore.UserBuilderFactory;
import uk.ac.ceh.components.userstore.crowd.model.CrowdUser;

/**
 *
 * @author cjohn
 */
public class TestTransformCrowdUserToDomainUser {
    @Mock UserBuilderFactory factory;
    
    private TransformCrowdUserToDomainUser transformer;
    
    @Before
    public void createUserReader() {
        MockitoAnnotations.initMocks(this);
        transformer = new TransformCrowdUserToDomainUser(factory);
    }
    
    @Test
    public void checkItWritesStandardAttributesToBuilder() {
        //Given
        CrowdUser user = new CrowdUser();
        user.setDisplayname("display");
        user.setFirstname("firstname");
        user.setLastname("lastname");
        user.setEmail("email");
        
        UserBuilder builder = mock(UserBuilder.class);
        when(builder.set(any(String.class), any(Object.class))).thenReturn(builder);
        when(factory.newUserBuilder(any(String.class))).thenReturn(builder);
        
        //When
        transformer.apply(user);
        
        //Then
        verify(builder).set(UserAttribute.DISPLAY_NAME, "display");
        verify(builder).set(UserAttribute.EMAIL, "email");
        verify(builder).set(UserAttribute.FIRSTNAME, "firstname");
        verify(builder).set(UserAttribute.LASTNAME, "lastname");
    }
}
