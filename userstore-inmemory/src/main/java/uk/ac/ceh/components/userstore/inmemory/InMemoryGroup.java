package uk.ac.ceh.components.userstore.inmemory;

import uk.ac.ceh.components.userstore.Group;

/**
 * The following implementation of a group is a simple store of name and 
 * description. This particular implementations description is mutable by the 
 * package
 * @author Christopher Johnson
 */
public class InMemoryGroup implements Group {
    private final String name;
    private String description;
    
    public InMemoryGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
    void setDescription(String description) {
        this.description = description;
    }
}
