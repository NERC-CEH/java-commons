package uk.ac.ceh.components.userstore.crowd;

import com.google.common.base.Function;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;
import uk.ac.ceh.components.userstore.UserBuilderFactory;

/**
 *
 * @author Christopher Johnson
 */
class TransformCrowdUserToDomainUser<U extends User> implements Function<CrowdUser, U> {
    private final UserBuilderFactory<U> factory;

    public TransformCrowdUserToDomainUser(UserBuilderFactory<U> factory) {
        this.factory = factory;
    }
    
    @Override
    public U apply(CrowdUser crowdUser) {
        return factory.newUserBuilder(crowdUser.getName())
                        .set(UserAttribute.EMAIL, crowdUser.getEmail())
                        .build();
    }
}
