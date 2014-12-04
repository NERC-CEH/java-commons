package uk.ac.ceh.components.userstore.crowd;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.userstore.UserAttribute;
import uk.ac.ceh.components.userstore.UserAttributeReader;
import uk.ac.ceh.components.userstore.crowd.model.CrowdUser;

/**
 *
 * @author cjohn
 */
public class TestTransformDomainUserToCrowdUser {
    @Mock UserAttributeReader reader;
    
    private TransformDomainUserToCrowdUser transformer;
    
    @Before
    public void createUserReader() {
        MockitoAnnotations.initMocks(this);
        transformer = new TransformDomainUserToCrowdUser(reader);
    }
    
    @Test
    public void checkItHandlesNullActiveState() {
        //Given
        TestUser user = new TestUser();
        when(reader.get(any(), eq(UserAttribute.ACTIVE), eq(Boolean.class)))
                .thenReturn(null);
        
        //When
        CrowdUser crowdUser = transformer.apply(user);
        
        //Then
        assertTrue("Expected activation to default to true", crowdUser.isActive());
    }
}
