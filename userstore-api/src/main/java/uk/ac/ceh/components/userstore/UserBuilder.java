package uk.ac.ceh.components.userstore;

/**
 * The following interface defines a builder for users. This interface is intended
 * to be extended by other interfaces. This is why it only contains the build
 * method
 * @author Christopher Johnson
 */
public interface UserBuilder<U> {
    U build();
}
