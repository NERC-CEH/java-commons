package uk.ac.ceh.components.userstore.crowd;

import uk.ac.ceh.components.userstore.crowd.model.CrowdUser;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;
import uk.ac.ceh.components.userstore.UserBuilder;
import uk.ac.ceh.components.userstore.UserBuilderFactory;
import uk.ac.ceh.components.userstore.crowd.model.CrowdAttributes.CrowdAttribute;

/**
 *
 * @author Christopher Johnson
 */
class TransformCrowdUserToDomainUser<U extends User> implements Function<CrowdUser, U> {
    private static final TypeToken<List<String>> typeToken = new TypeToken<List<String>>(){};
    private final UserBuilderFactory<U> factory;

    public TransformCrowdUserToDomainUser(UserBuilderFactory<U> factory) {
        this.factory = factory;
    }
    
    @Override
    public U apply(CrowdUser crowdUser) {
        UserBuilder<U> userBuilder = factory.newUserBuilder(crowdUser.getName())
                                             .set(UserAttribute.EMAIL, crowdUser.getEmail())
                                             .set(UserAttribute.FIRSTNAME, crowdUser.getFirstname())
                                             .set(UserAttribute.LASTNAME, crowdUser.getLastname())
                                             .set(UserAttribute.DISPLAY_NAME, crowdUser.getDisplayname());
        
        for(CrowdAttribute attribute : crowdUser.getAttributes().getAttributes()) {
            Type desiredTypeForURI = factory.getDesiredTypeForURI(attribute.getName());
                    
            userBuilder.set(attribute.getName(), 
                desiredTypeForURI == null || typeToken.isAssignableFrom(desiredTypeForURI)
                    ? attribute.getValues()
                    : convertToDesired(attribute.getValues(), desiredTypeForURI));
        }
        return userBuilder.build();
    }
    
    private Object convertToDesired(List<String> toConvert, Type desired) {
        if(TypeToken.of(List.class).isAssignableFrom(desired)) {
            ParameterizedType type = (ParameterizedType)desired;
            final Type desiredType = type.getActualTypeArguments()[0];
            return new ArrayList(Collections2.transform(toConvert, new Function<String, Object>() {
                @Override
                public Object apply(String f) {
                    return convertSingleToDesired(f, desiredType);
                }
            }));
        }
        else if(toConvert.size() == 1) {
            return convertSingleToDesired(toConvert.get(0), desired);
        }
        else {
            throw new IllegalArgumentException("Can not convert " + toConvert + " to " + desired);
        }
    }
    
    private Object convertSingleToDesired(String value, Type desired) {
        if( String.class == desired )   return value;
        if( Boolean.class == desired )  return Boolean.parseBoolean( value );
        if( Byte.class == desired )     return Byte.parseByte( value );
        if( Short.class == desired )    return Short.parseShort( value );
        if( Integer.class == desired )  return Integer.parseInt( value );
        if( Long.class == desired )     return Long.parseLong( value );
        if( Float.class == desired )    return Float.parseFloat( value );
        if( Double.class == desired )   return Double.parseDouble( value );
        throw new IllegalArgumentException("Can not convert " + value + " to " + desired);
    }
}
