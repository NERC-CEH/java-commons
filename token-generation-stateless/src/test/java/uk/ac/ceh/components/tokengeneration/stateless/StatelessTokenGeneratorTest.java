package uk.ac.ceh.components.tokengeneration.stateless;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.components.tokengeneration.ExpiredTokenException;
import uk.ac.ceh.components.tokengeneration.InvalidTokenException;
import uk.ac.ceh.components.tokengeneration.Token;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Define the tests for the StatelessTokenGenerator
 * @author Christopher Johnson
 */
public class StatelessTokenGeneratorTest {
    private StatelessTokenKeyContainer keys;
    private StatelessTokenGenerator generator;
    
    @Before
    public void initialize() throws StatelessTokenKeystoreManagerException, IOException, NoSuchAlgorithmException {
        //Mock a StatelssTokenKeyContainer and stub it's methods
        keys = stubStatelessTokenKeyContainer();
        generator = new StatelessTokenGenerator(keys);
    }
    
    @Test
    public void createTokenAndGetMessage() throws InvalidTokenException, ExpiredTokenException {
        //GIVEN
        String myMessage = "My Secret Message";
        int ttl = 1000;
        
        //WHEN
        Token generateToken = generator.generateToken(myMessage.getBytes(), ttl);
        byte[] message = generator.getMessage(generateToken);
        
        //THEN
        assertArrayEquals(
                "The message used to create the token differed from what was obtained", 
                myMessage.getBytes(), message
        );
    }
    
    @Test(expected=ExpiredTokenException.class)
    public void useValidTokenAfterExpirey() throws InvalidTokenException, ExpiredTokenException, InterruptedException {
        //Given
        Token generateToken = generator.generateToken("Token message".getBytes(), 1);
        
        //When
        Thread.sleep(25); //wait 25 milliseconds
        
        //Then
        byte[] messageFromToken = generator.getMessage(generateToken);
        fail("Expected Token Expiry Exception");
    }
    
    @Test(expected=InvalidTokenException.class)
    public void useRandomlyCreatedToken() throws InvalidTokenException, ExpiredTokenException, InterruptedException {
        //Given
        Token randomToken = new Token(new byte[]{0x01, 0x03, 0x54});
        
        //When
        byte[] message = generator.getMessage(randomToken);
        
        //Then
        fail("No user should have been obtained with random token");
    }
    
    @Test(expected=InvalidTokenException.class)
    public void useManipulatedToken() throws InvalidTokenException, ExpiredTokenException, InterruptedException {
        //Given
        Token generateToken = generator.generateToken("tester".getBytes(), 1000); //get real token
        byte[] tokenData = generateToken.getBytes();
        tokenData[0] = (byte) (tokenData[0] + 1);
        Token manipulatedToken = new Token(tokenData);
        
        //When
        byte[] user = generator.getMessage(manipulatedToken);
        
        //Then
        fail("No user should have been obtained with manipulated token");
    }
    
    @Test
    public void createTokenFromMessageWithObscureCharacters() throws UnsupportedEncodingException, InvalidTokenException, ExpiredTokenException {
        //Given
        String weirdMessage = "\u514B\u91CC\u65AF@\u7D04\u7FF0\u905C";
        
        //When
        Token generateToken = generator.generateToken(weirdMessage.getBytes("UTF-8"), 1000); //get real token
        String message = new String(generator.getMessage(generateToken), "UTF-8");
        
        //Then
        assertEquals("Expected to get a message with unicode chars inside", weirdMessage, message);
    }
    
    @Test
    public void decryptTokenWithADifferentInstanceOfStatelessTokenGenerator() throws InvalidTokenException, ExpiredTokenException {
        //Given
        String myMessage = "My Secret Message";
        StatelessTokenGenerator anotherGenerator = new StatelessTokenGenerator(keys);
       
        //When
        Token generateToken = generator.generateToken(myMessage.getBytes(), 1000);
        String messageFromDifferentGenerator = new String(anotherGenerator.getMessage(generateToken));
        
        //Then
        assertEquals("Expected to be able to get same message from a different generator", 
                myMessage, messageFromDifferentGenerator);
    }
    
    @Test(expected=InvalidTokenException.class)
    public void decryptTokenWithADifferentKeys() throws InvalidTokenException, ExpiredTokenException, NoSuchAlgorithmException {
        //Given
        String myMessage = "My Secret Message";
        StatelessTokenGenerator anotherGenerator = new StatelessTokenGenerator(stubStatelessTokenKeyContainer());
       
        //When
        Token generateToken = generator.generateToken(myMessage.getBytes(), 1000);
        String messageFromDifferentGenerator = new String(anotherGenerator.getMessage(generateToken));
        
        //Then
        fail("Should not be able to decrpyt this message");
    }
    
    private static StatelessTokenKeyContainer stubStatelessTokenKeyContainer() throws NoSuchAlgorithmException {
        StatelessTokenKeyContainer toReturn = mock(StatelessTokenKeyContainer.class);
        when(toReturn.getKey()).thenReturn(KeyGenerator
                .getInstance(StatelessTokenGenerator.SECRET_KEY_ALGORITHM)
                .generateKey());
        
        when(toReturn.getHMacKey()).thenReturn(KeyGenerator
                .getInstance(StatelessTokenGenerator.MAC_ALGORITHM)
                .generateKey());
        
        return toReturn;
    }
}
