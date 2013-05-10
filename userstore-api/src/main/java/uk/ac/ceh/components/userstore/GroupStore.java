package uk.ac.ceh.components.userstore;

import java.util.List;

/**
 * The following interface defines a group manager which is capable of having 
 * group information read from.
 * @author Christopher Johnson
 */
public interface GroupStore<U extends User> {
    /**
     * The following method will return the list of groups associated to a given
     * User
     * @param user The user to look up the groups of
     * @return A list of groups for this user
     */
    List<Group> getGroups(U user);
    
    /**
     * The following method will return the group associated with a given name
     * @param name of the group to find
     * @return The group associated with this name
     * @throws IllegalArgumentException if no group exists with this name
     */
    Group getGroup(String name) throws IllegalArgumentException;
    
    /**
     * Returns all the groups stored in this GroupManager
     * @return A list of known groups for this group manager
     */
    List<Group> getAllGroups();
    
    /**
     * Checks to see if some given group exists.
     * @param name of the group to look up
     * @return Whether or not the group exists
     */
    boolean isGroupInExistance(String name);
    
    /**
     * Check to see if the given group can be deleted
     * @param group The name of the group to delete
     * @return true if the group represented by the given name can be deleted
     * @throws IllegalArgumentException if the group does not exist
     */
    boolean isGroupDeletable(String group) throws IllegalArgumentException;
}
