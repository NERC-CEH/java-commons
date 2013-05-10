package uk.ac.ceh.components.userstore.inmemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.WritableGroupStore;

/**
 *
 * @author CJOHN
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
        if(userGroups.containsKey(user)) {
            return userGroups.get(user);
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public InMemoryGroup getGroup(String name) {
       return Objects.requireNonNull(groups.get(name), "Expected to find a group with name " + name);
    }

    @Override
    public List<Group> getAllGroups() {
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
    public Group createGroup(String groupname, String description) {
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
    public boolean deleteGroup(String groupname) {
        InMemoryGroup removed = getGroup(groupname);
        for(Entry<User, List<Group>> userGroup : userGroups.entrySet()) {
            userGroup.getValue().remove(removed); //remove group from all users
        }
        
        return true;
    }

    @Override
    public void grantGroupToUser(U user, String groupname) {
        InMemoryGroup group = getGroup(groupname);
        if(userGroups.containsKey(user)) {
            userGroups.get(user).add(group);
        }
        else {
            userGroups.put(user, new ArrayList<Group>(Arrays.asList(group)));
        }
    }

    @Override
    public boolean revokeGroupFromUser(U user, String groupname) {
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
