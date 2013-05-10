package uk.ac.ceh.components.userstore.inmemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.WritableGroupStore;

/**
 * The following class defines a writable groupstore which is based upon a hashmap
 * 
 * This implementation is thread safe
 * @author cjohn
 */
public class InMemoryGroupStore<U extends User> implements WritableGroupStore<U>{
    private Map<String, InMemoryGroup> groups;
    private Map<User, List<Group>> userGroups;
    
    public InMemoryGroupStore() {
        this.userGroups  = new HashMap<>();
        this.groups = new HashMap<>();
    }
    
    @Override
    public List<Group> getGroups(U user) {
        List<Group> givenUsersGroups = userGroups.get(user);
        if(givenUsersGroups != null) {
            return givenUsersGroups;
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public InMemoryGroup getGroup(String name) {
        InMemoryGroup group = groups.get(name);
        if(group == null) {
            throw new IllegalArgumentException("Expected to find a group with name " + name);
        }
        return group;
    }

    @Override
    public synchronized List<Group> getAllGroups() {
        List<Group> toReturn = new ArrayList<>();
        toReturn.addAll(groups.values());
        return toReturn;
    }

    @Override
    public boolean isGroupInExistance(String name) {
        return groups.containsKey(name);
    }

    @Override
    public boolean isGroupDeletable(String group) {
        return true; //all groups are deletable
    }

    @Override
    public synchronized Group createGroup(String groupname, String description) {
        if(isGroupInExistance(groupname)) {
            throw new IllegalArgumentException("A group already exists with this groupname");
        }
        else {
            InMemoryGroup newGroup = new InMemoryGroup(groupname, description);
            groups.put(groupname, newGroup);
            return newGroup;
        }
    }

    @Override
    public Group updateGroup(String groupName, String description) {
        InMemoryGroup group = getGroup(groupName);
        group.setDescription(description); //update the description
        return group;
    }

    @Override
    public synchronized boolean deleteGroup(String groupname) {
        InMemoryGroup removed = getGroup(groupname);
        for(Entry<User, List<Group>> userGroup : userGroups.entrySet()) {
            userGroup.getValue().remove(removed); //remove group from all users
        }
        groups.remove(groupname);
        return true;
    }

    @Override
    public synchronized boolean grantGroupToUser(U user, String groupname) {
        InMemoryGroup group = getGroup(groupname);
        if(userGroups.containsKey(user)) {
            List<Group> currentUsersGroups = userGroups.get(user);
            if(!currentUsersGroups.contains(group)) {
                currentUsersGroups.add(group);
            }
            else {
                return false;
            }
        }
        else {
            userGroups.put(user, new ArrayList<Group>(Arrays.asList(group)));
        }
        return true;
    }

    @Override
    public synchronized boolean revokeGroupFromUser(U user, String groupname) {
        InMemoryGroup group = getGroup(groupname);
        if(userGroups.containsKey(user)) {
            List<Group> currUserGroups = userGroups.get(user);
            boolean removedValue = currUserGroups.remove(group);
            if(removedValue && currUserGroups.isEmpty()) {
                userGroups.remove(user);
            }
            return removedValue;
        }
        return false; //the user is not know, so it doesn't have that group to remove
    }
}
