package uk.ac.ceh.components.userstore.crowd;

import uk.ac.ceh.components.userstore.crowd.model.CrowdUser;
import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;
import uk.ac.ceh.components.userstore.UserAttributeReader;
import uk.ac.ceh.components.userstore.crowd.model.CrowdAttributes;
import uk.ac.ceh.components.userstore.crowd.model.CrowdAttributes.CrowdAttribute;

/**
 *
 * @author Christopher Johnson
 */
class TransformDomainUserToCrowdUser<U extends User> implements Function<U, CrowdUser> {
    private static final TypeToken<List<String>> typeToken = new TypeToken<List<String>>(){};
    
    private final UserAttributeReader<U> reader;
    private final Map<String, Type> attributesToRead;

    public TransformDomainUserToCrowdUser(UserAttributeReader<U> reader) {
        this.reader = reader;
        this.attributesToRead = new HashMap<>(reader.getDefinedAttributes());
        
        attributesToRead.remove(UserAttribute.USERNAME);
        attributesToRead.remove(UserAttribute.EMAIL);
        attributesToRead.remove(UserAttribute.DISPLAY_NAME);
        attributesToRead.remove(UserAttribute.FIRSTNAME);
        attributesToRead.remove(UserAttribute.LASTNAME);
        attributesToRead.remove(UserAttribute.ACTIVE);
    }
    
    @Override
    public CrowdUser apply(U user) {
        CrowdUser newUser = new CrowdUser();
        newUser.setName(user.getUsername());
        newUser.setEmail(reader.get(user, UserAttribute.EMAIL, String.class));
        newUser.setDisplayname(reader.get(user, UserAttribute.DISPLAY_NAME, String.class));
        newUser.setFirstname(reader.get(user, UserAttribute.FIRSTNAME, String.class));
        newUser.setLastname(reader.get(user, UserAttribute.LASTNAME, String.class));
        
        // Reading a Boolean value, if ACTIVE is null then we will default to 
        // activating the user. 
        Boolean isActive = reader.get(user, UserAttribute.ACTIVE, Boolean.class);
        newUser.setActive((isActive == null) ? true : isActive);
        
        newUser.setAttributes(getCrowdAttributes(user));
        return newUser;
    }
    
    private CrowdAttributes getCrowdAttributes(U user) {
        CrowdAttributes toReturn = new CrowdAttributes();
        
        for(Entry<String, Type> currAttr : attributesToRead.entrySet()) {
            String attributeName = currAttr.getKey();
            
            Object attributeValue = reader.get(user, attributeName);
            
            toReturn.add(new CrowdAttribute(attributeName, 
                    valueToStringList(attributeValue, currAttr.getValue())
            ));
        }
        
        return toReturn;
    }
    
    private static List<String> valueToStringList(Object value, Type desiredType) {
        if(value == null) {
            return Collections.EMPTY_LIST;
        }
        else if(typeToken.isAssignableFrom(desiredType)) {
            return (List)value;
        }
        else {
            return Arrays.asList(value.toString());
        }
    }
}
