package uk.ac.ceh.components.tokengeneration;

/**
 * Implementations of the following interface are responsible for generating 
 * Tokens from a byte arrays and converting those tokens back into byte arrays
 * @author Christopher Johnson
 */
public interface TokenGenerator {
    /**
     * The following method will transform the given token into a byte array.
     * @param token The token to transform into a byte array
     * @return The byte array which was used to create the given token
     * @throws InvalidTokenException if the token can not be processed by this 
     *  TokenGenerator
     * @throws ExpiredTokenException if the Token which was created from the 
     *  #generateToken method had a ttl specified which has now expired
     */
    byte[] getMessage(Token token) throws InvalidTokenException, ExpiredTokenException;
    
    /**
     * Creates a Token which can be converted back into a byte array at a later 
     * time
     * @param message The message which is to be wrapped up as a token
     * @param ttl The length of time (in milliseconds) which this token should be
     *  valid for
     * @return the token representation of the message
     */
    Token generateToken(byte[] message, int ttl);
}
