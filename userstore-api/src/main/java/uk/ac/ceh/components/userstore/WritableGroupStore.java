package uk.ac.ceh.components.userstore;

/**
 * The following interface defines the mutation operations which are needed
 * to modify a GroupStore
 * @author Christopher Johnson
 */
public interface WritableGroupStore<U extends User> extends GroupStore<U> {
    
    /**
     * Creates a new group in this group store
     * @param newGroup the new group to add
     * @return an instance of group with the given groupname and description
     * @throws GroupnameAlreadyTakenException if the group name is already in use
     */
    Group createGroup(String groupname, String description) throws GroupnameAlreadyTakenException;
    
    /**
     * Updates the given group represented by the given groupname
     * @param groupName the name of the group to update
     * @param description the description of the group
     * @return an instance of the group
     * @throws UnknownGroupException if a group with the given groupname does not 
     *  exist
     */
    Group updateGroup(String groupName, String description) throws UnknownGroupException;
    
    /**
     * The following method will remove the group from the group store, any users
     * who have been associated to this group will have it removed from them.
     * @param groupname The groupname to remove from the groupstore
     * @return true if the group was removed from the groupstore
     * @throws UnknownGroupException if the groupname is not known to the groupstore
     */
    boolean deleteGroup(String groupname) throws UnknownGroupException;
    
    /**
     * Assign some user to a group
     * @param user The user to assign to 
     * @param groupname the group name to associate this user to
     * @throws UnknownGroupException if the group does not exist
     */
    void grantGroupToUser(U user, String groupname) throws UnknownGroupException;
    
    /**
     * Revokes the given group from a user if that user has that group associated 
     * to them.
     * @param user the user to remove a group of
     * @param groupname the group to remove from the user
     * @return true if the role was removed from the user. This method should 
     *  return false if the user did not have the group to start with
     * @throws UnknownGroupException If the group is not known
     */
    boolean revokeGroupFromUser(U user, String groupname) throws UnknownGroupException;
}
