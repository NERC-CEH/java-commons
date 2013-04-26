package uk.ac.ceh.components.userstore.springsecurity;

import java.util.Collection;

/**
 * A simple interface which defines that some object has been granted a 
 * collection of roles
 * @author Christopher Johnson
 */
public interface Roled {
    /**
     * The following method will return a collection of roles represented as 
     * Strings. Each role should only appear once in the collection.
     * @return A collection of Roles
     */
    Collection<String> getRoles();
}
