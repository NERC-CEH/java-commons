package uk.ac.ceh.components.userstore;

/**
 * The following interface defines how to read given user attributes from a user
 * instance.
 * @author Christopher Johnson
 */
public interface UserAttributeReader<U> {
    /**
     * Gets the value of the {@link UserAttribute} represented by uri from the 
     *  user
     * @param user The user to read from
     * @param uri The property to read
     * @return The value of uri from the user if it exists. else null.
     * @throws UserAttributeReaderException if the property exists but could not be read
     */
    Object get(U user, String uri) throws UserAttributeReaderException;
    
    /**
     * The following method will get the specified property off of the given user
     * and attempt to cast that value to the type specified by type.
     * 
     * Implementations are free to attempt to convert the actual value to the specified
     * type if necessary.
     * @param <T> The type to attempt to get the value of
     * @param user The user to read the property from
     * @param uri The uri of the property to read from the user
     * @param type The class representing the desired type
     * @return The property value to read in the desired type
     * @throws UserAttributeReaderException if the property can not be transformed
     *  to the desired type or the property exists but can not be read
     */
    <T> T get(U user, String uri, Class<T> type) throws UserAttributeReaderException;
}
