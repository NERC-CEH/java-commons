
package uk.ac.ceh.components.tokenauthentication;

import java.nio.ByteBuffer;
import uk.ac.ceh.components.tokengeneration.ExpiredTokenException;
import uk.ac.ceh.components.tokengeneration.InvalidTokenException;
import uk.ac.ceh.components.tokengeneration.Token;
import uk.ac.ceh.components.tokengeneration.TokenGenerator;
import uk.ac.ceh.components.userstore.InvalidCredentialsException;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserStore;

/**
 *
 * @author cjohn
 */
public class UserStoreTokenAuthenticator<U extends User> implements TokenAuthenticator<U> {
    private final UserStore<U> users;
    private final TokenGenerator tokenGen;

    public UserStoreTokenAuthenticator(UserStore<U> users, TokenGenerator tokenGen) {
        this.users = users;
        this.tokenGen = tokenGen;
    }
    
    @Override
    public Token generateToken(String username, String password, int ttl) throws InvalidCredentialsException {
        U user = users.authenticate(username, password);
        return tokenGen.generateToken(ByteBuffer.wrap(user.getUsername().getBytes()), ttl);
    }

    @Override
    public U getUser(Token token) throws InvalidTokenException, ExpiredTokenException {
        try {
            ByteBuffer message = tokenGen.getMessage(token);
            byte[] messageBytes = new byte[message.remaining()];
            message.get(messageBytes);
            
            return users.getUser(new String(messageBytes));
        } catch (UnknownUserException ex) {
            throw new InvalidTokenException("No user exists for this token", ex);
        }
    }
}
