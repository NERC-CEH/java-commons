package uk.ac.ceh.components.tokengeneration;

import java.nio.ByteBuffer;

/**
 * Implementations of the following interface are responsible for generating 
 * Tokens from a ByteBuffer and converting those tokens back into ByteBuffers
 * @author Christopher Johnson
 */
public interface TokenGenerator {
    /**
     * The following method will transform the given token into a byte buffer.
     * @param token The token to transform into a byte buffer
     * @return The bytebuffer which was used to create the given token
     * @throws InvalidTokenException if the token can not be processed by this 
     *  TokenGenerator
     * @throws ExpiredTokenException if the Token which was created from the 
     *  #generateToken method had a ttl specified which has now expired
     */
    ByteBuffer getMessage(Token token) throws InvalidTokenException, ExpiredTokenException;
    
    /**
     * Creates a Token which can be converted back into a ByteBuffer at a later 
     * time
     * @param messageBuf The message which is to be wrapped up as a token
     * @param ttl The length of time (in milliseconds) which this token should be
     *  valid for
     * @return the token representation of the messageBuffer
     * @throws TokenGenerationException if it is not possible to process the 
     *  messageBuf into a Token, the ttl can not be ensured or the TokenGenerator
     *  is mis-configured.
     */
    Token generateToken(ByteBuffer messageBuf, int ttl) throws TokenGenerationException;
}
