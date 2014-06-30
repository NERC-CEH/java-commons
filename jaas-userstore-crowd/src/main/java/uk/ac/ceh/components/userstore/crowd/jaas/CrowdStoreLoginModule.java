package uk.ac.ceh.components.userstore.crowd.jaas;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.components.userstore.crowd.CrowdApplicationCredentials;
import uk.ac.ceh.components.userstore.crowd.CrowdGroupStore;
import uk.ac.ceh.components.userstore.crowd.CrowdUserStore;

/**
 *
 * @author mw
 */
public class CrowdStoreLoginModule extends UserStoreLoginModule {
    protected static final Map<CrowdApplicationCredentials, UserGroupStore> USER_GROUP_STORES = new HashMap<>();
    
    @Data
    protected class UserGroupStore {
        private final UserStore userstore;
        private final GroupStore groupstore;
    }
    
    @Override
    protected void createUserStoreAndGroupStore(Map<String, ?> options) {
       String location = (String)options.get("location");
       String applicationName = (String)options.get("applicationName");
       String password = (String)options.get("password");
       
       UserGroupStore userPair = getUserGroupPair(new CrowdApplicationCredentials(location, applicationName, password));
       setGroupStore(userPair.getGroupstore());
       setUserStore(userPair.getUserstore());
    }
    
    protected synchronized UserGroupStore getUserGroupPair(CrowdApplicationCredentials credentials) {
        if(!USER_GROUP_STORES.containsKey(credentials)) {
            AnnotatedUserHelper userHelper = new AnnotatedUserHelper(UserPrincipal.class);
            USER_GROUP_STORES.put(credentials, new UserGroupStore(
                    new CrowdUserStore(credentials, userHelper, userHelper),
                    new CrowdGroupStore(credentials)));
        }
        return USER_GROUP_STORES.get(credentials);
    }
}
