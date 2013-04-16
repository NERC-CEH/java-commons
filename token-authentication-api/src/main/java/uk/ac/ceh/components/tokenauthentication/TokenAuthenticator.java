package uk.ac.ceh.components.tokenauthentication;

import uk.ac.ceh.components.tokengeneration.ExpiredTokenException;
import uk.ac.ceh.components.tokengeneration.InvalidTokenException;
import uk.ac.ceh.components.tokengeneration.Token;
import uk.ac.ceh.components.userstore.InvalidCredentialsException;

/**
 * The following interface defines the methods required in order to be a 
 * TokenAuthenticator
 * @author Christopher Johnson
 */
public interface TokenAuthenticator<U> {
    /**
     * Generates a Token for a given username and password. Token will be valid
     * for ttl milliseconds
     * @param username
     * @param password
     * @param ttl Milliseconds for how long this token is required 
     * @return A generated token for the user
     * @throws InvalidCredentialsException
     */
    public Token generateToken(String username, String password, int ttl) throws InvalidCredentialsException;
    
    /**
     * Gets the user associated with a particular token
     * @param token The token of which to obtain the user for
     * @return A user associated with the passed in token
     * @throws InvalidTokenException If the token is not valid to this TokenAuthenticator
     * @throws ExpiredTokenException If the token is no longer valid
     */
    public U getUser(Token token) throws InvalidTokenException, ExpiredTokenException;
}