package uk.ac.ceh.components.userstore.crowd;

import com.sun.jersey.api.client.ClientResponse;
import static com.sun.jersey.api.client.ClientResponse.Status.NOT_FOUND;
import static com.sun.jersey.api.client.ClientResponse.Status.OK;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import java.util.List;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.WritableGroupStore;

/**
 *
 * @author Christopher Johnson
 */
public class CrowdGroupStore<U extends User> implements WritableGroupStore<U> {
    WebResource crowd;
    
    @Override
    public List<Group> getGroups(U user) {
        GenericType<List<Group>> genericType = new GenericType<List<Group>>() {};
        
        ClientResponse crowdResponse = crowd.path("user/group/direct")
                                            .queryParam("username", user.getUsername())
                                            .get(ClientResponse.class);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK: return crowdResponse.getEntity(genericType);
            case NOT_FOUND: throw new IllegalArgumentException("The specified user is not known to crowd");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public Group getGroup(String groupname) throws IllegalArgumentException {
        ClientResponse crowdResponse = getCrowdGroupClientResponse(groupname);
        
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return crowdResponse.getEntity(CrowdGroup.class);
            case NOT_FOUND : throw new IllegalArgumentException("The specified group does not exist");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }
    
    @Override
    public List<Group> getAllGroups() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isGroupInExistance(String groupname) {
        ClientResponse crowdResponse = getCrowdGroupClientResponse(groupname);
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return true;
            case NOT_FOUND : return false;
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
        
    }

    @Override
    public boolean isGroupDeletable(String groupname) throws IllegalArgumentException {
        getGroup(groupname); //attempt to get the group with the given name
        return true;    //all groups in crowd are deletable
    }

    @Override
    public Group createGroup(String groupname, String description) throws IllegalArgumentException {
        CrowdGroup newGroup = new CrowdGroup(groupname, description);
        
        ClientResponse crowdResponse = crowd.path("group")
                                            .post(ClientResponse.class, newGroup);
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return newGroup;
            case BAD_REQUEST : throw new IllegalArgumentException("The specified group already exists");
            case FORBIDDEN:
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public Group updateGroup(String groupname, String description) throws IllegalArgumentException {
        CrowdGroup updatedGroup = new CrowdGroup(groupname, description);
        
        ClientResponse crowdResponse = crowd.path("group")
                                            .queryParam("groupname", groupname)
                                            .put(ClientResponse.class, updatedGroup);
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return updatedGroup;
            case FORBIDDEN: //throw some exception
            case NOT_FOUND: throw new IllegalArgumentException("The given group does not exist");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public boolean deleteGroup(String groupname) throws IllegalArgumentException {
        ClientResponse crowdResponse = crowd.path("group")
                                            .queryParam("groupname", groupname)
                                            .delete(ClientResponse.class);
        switch(crowdResponse.getClientResponseStatus()) {
            case OK : return true; //successfully deleted
            case NOT_FOUND : throw new IllegalArgumentException("The given group does not exist");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public boolean grantGroupToUser(U user, String groupname) throws IllegalArgumentException {
        ClientResponse crowdResponse = crowd.path("group/user/direct")
                                            .queryParam("groupname", groupname)
                                            .post(ClientResponse.class, user);
        switch(crowdResponse.getClientResponseStatus()) {
            case CREATED : return true;
            case BAD_REQUEST: throw new IllegalArgumentException("The specified user is not known");
            case NOT_FOUND:throw new IllegalArgumentException("The given group does not exist");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }

    @Override
    public boolean revokeGroupFromUser(U user, String groupname) throws IllegalArgumentException {
        ClientResponse crowdResponse = crowd.path("group/user/direct")
                                            .queryParam("groupname", groupname)
                                            .queryParam("username", user.getUsername())
                                            .delete(ClientResponse.class);
        switch(crowdResponse.getClientResponseStatus()) {
            case NO_CONTENT : return true;
            case NOT_FOUND : throw new IllegalArgumentException("Either the group or username don't exist");
            default: throw new CrowdRestException("Unexpected status code: " + crowdResponse.getStatus());
        }
    }
    
    private ClientResponse getCrowdGroupClientResponse(String groupname) {
        return crowd.path("group")
                    .queryParam("groupname", groupname)
                    .get(ClientResponse.class);
    }
}
