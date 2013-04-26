package uk.ac.ceh.components.tokengeneration.stateless;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cjohn
 */
public class StatelessTokenKeystoreManagerTest {
    @Test
    public void createANewKeystore() throws StatelessTokenKeystoreManagerException {
        //Given
        File fileThatDoesNotExists = new File("doesNotExist");
        assertFalse(fileThatDoesNotExists.exists());
        
        //When
        StatelessTokenKeystoreManager keystore = new StatelessTokenKeystoreManager(fileThatDoesNotExists);
                
        //Then
        assertNotNull("A HMac key should exist", keystore.getHMacKey());
        assertNotNull("An encryption key should exist", keystore.getKey());
        assertTrue("The keystore file should now exist", fileThatDoesNotExists.exists());
        fileThatDoesNotExists.delete();
    }
    
    @Test
    public void loadAnExistingKeystore() throws URISyntaxException, StatelessTokenKeystoreManagerException {
        //Given
        URL existingKeystore = StatelessTokenKeystoreManagerTest.class.getResource("keystore.jce");
        File keyStoreFile = new File(existingKeystore.toURI());
        
        //When
        StatelessTokenKeystoreManager keystore = new StatelessTokenKeystoreManager(keyStoreFile);
        
        //Then
        assertNotNull("A HMac key should exist", keystore.getHMacKey());
        assertNotNull("An encryption key should exist", keystore.getKey());
    }
}
