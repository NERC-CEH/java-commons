package uk.ac.ceh.components.userstore;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The following class bundles up a UserBuilderFactory and a UserAttributeReader
 * to create a UserHelper implementation which is capable of reading and creating
 * generically typed user objects.
 * 
 * This implementation relies on the annotation {@link UserAttribute} for deciding
 * which fields of an user object class should be read and written to.
 * 
 * @author Christopher Johnson
 */
public class AnnotatedUserHelper<U extends User> implements UserBuilderFactory<U>, UserAttributeReader<U> {
    private final Map<String, Field> namedFields;
    private Class<U> userClass;
    
    /**
     * Constructs a new AnnotatedUserHelper for the given user class. The 
     * constructor will fail if the supplied user does not contain a field which
     * is annotated a with UserAttribute(value= {@link UserAttribute#USERNAME} )
     * @param userClass 
     */
    public AnnotatedUserHelper(Class<U> userClass) {
        this.namedFields = new HashMap<>();
        this.userClass = userClass;
        
        //process the user class to find the annotated fields
        for(Class<?> clazz = userClass; clazz != null; clazz = clazz.getSuperclass()) {
            for(Field field : clazz.getDeclaredFields()) {
                UserAttribute annotation = field.getAnnotation(UserAttribute.class);
                if(annotation != null) {
                    field.setAccessible(true); //make this field modifable
                    namedFields.put(annotation.value(), field);
                }
            }
        }
        
        if(!namedFields.containsKey(UserAttribute.USERNAME)) {
            throw new IllegalArgumentException("The given user class does not have a field annotated with UserAttribute(value=UserAttribute.USERNAME)");
        }
    }

    @Override
    public UserBuilder<U> newUserBuilder(String username) {
        return new FieldSettingUserBuilder<>(userClass, username, namedFields);
    }
    
    @Override
    public Object get(U user, String uri) {
        if(namedFields.containsKey(uri)) {
            try {
                return namedFields.get(uri).get(user);
            } catch (IllegalAccessException ex) {
                throw new UserAttributeReaderException(ex);
            }
        }
        else {
            return null;
        }
    }

    @Override
    public <T> T get(U user, String uri, Class<T> type) throws UserAttributeReaderException {
        try {
            return type.cast(get(user, uri));
        }
        catch(ClassCastException ex) {
            throw new UserAttributeReaderException("Unable to convert to the desired class", ex);
        }
    }

    @Override
    public Map<String, Type> getDefinedAttributes() {
        Map<String, Type> toReturn = new HashMap<>();
        for(Entry<String, Field> currEntry : namedFields.entrySet()) {
            toReturn.put(currEntry.getKey(), currEntry.getValue().getGenericType());
        }
        return toReturn;
    }

    @Override
    public Type getDesiredTypeForURI(String uri) {
        return getDefinedAttributes().get(uri);
    }
}
