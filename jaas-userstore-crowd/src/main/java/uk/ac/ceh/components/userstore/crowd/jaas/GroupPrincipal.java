package uk.ac.ceh.components.userstore.crowd.jaas;

import java.security.Principal;
import lombok.Data;
import uk.ac.ceh.components.userstore.Group;

/**
 *
 * @author mw
 */
@Data
public class GroupPrincipal implements Principal {
    private final String name;
    
    public GroupPrincipal(Group group) {
        this.name = group.getName();
    }
}
