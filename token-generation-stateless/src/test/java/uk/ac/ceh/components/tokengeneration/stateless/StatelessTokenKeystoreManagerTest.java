package uk.ac.ceh.components.tokengeneration.stateless;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author cjohn
 */
public class StatelessTokenKeystoreManagerTest {
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void createANewKeystore() throws StatelessTokenKeystoreManagerException, IOException {
        //Given
        File keystoreFile = new File(folder.getRoot(), "newfile");
        
        //When
        StatelessTokenKeystoreManager keystore = new StatelessTokenKeystoreManager(keystoreFile);
                
        //Then
        assertNotNull("A HMac key should exist", keystore.getHMacKey());
        assertNotNull("An encryption key should exist", keystore.getKey());
        assertTrue("The keystore file should now exist", keystoreFile.exists());
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
    
    @Test
    public void createTwoManagersFromSameKeystore() throws StatelessTokenKeystoreManagerException {
        //Given
        File keystoreFile = new File(folder.getRoot(), "newfile");
        char[] password = "password".toCharArray();
        StatelessTokenKeystoreManager keystore1a = new StatelessTokenKeystoreManager(keystoreFile, password, "hmac1", "token1");
        StatelessTokenKeystoreManager keystore2a = new StatelessTokenKeystoreManager(keystoreFile, password, "hmac2", "token2");
        
        //When
        StatelessTokenKeystoreManager keystore1b = new StatelessTokenKeystoreManager(keystoreFile, password, "hmac1", "token1");
        StatelessTokenKeystoreManager keystore2b = new StatelessTokenKeystoreManager(keystoreFile, password, "hmac2", "token2");
        
        //Then
        assertEquals("Expected to find the same key", keystore1a.getKey(), keystore1b.getKey());
        assertEquals("Expected to find the same key", keystore2a.getKey(), keystore2b.getKey());
        assertEquals("Expected to find the same key", keystore1a.getHMacKey(), keystore1b.getHMacKey());
        assertEquals("Expected to find the same key", keystore2a.getHMacKey(), keystore2b.getHMacKey());   
    }
    
    @Test
    public void checkThatKeysPersist() throws StatelessTokenKeystoreManagerException {
        //Given
        File keystoreFile = new File(folder.getRoot(), "newfile");
        char[] password = "password".toCharArray();
        StatelessTokenKeystoreManager manager = new StatelessTokenKeystoreManager(keystoreFile, password, "hmac1", "token1");
        
        //When
        StatelessTokenKeystoreManager managerAgain = new StatelessTokenKeystoreManager(keystoreFile, password, "hmac1", "token1");
        
        //Then
        assertEquals("Expected to find the same key", manager.getKey(), managerAgain.getKey());
        assertEquals("Expected to find the same key", manager.getHMacKey(), managerAgain.getHMacKey());
    }
    
    @Test
    public void checkThatWeCanShareTheHMacKey() throws StatelessTokenKeystoreManagerException {
        //Given
        File keystoreFile = new File(folder.getRoot(), "newfile");
        char[] password = "password".toCharArray();
        StatelessTokenKeystoreManager manager = new StatelessTokenKeystoreManager(keystoreFile, password, "hmac1", "token1");
        
        //When
        StatelessTokenKeystoreManager managerAgain = new StatelessTokenKeystoreManager(keystoreFile, password, "hmac1", "token2");
        
        //Then
        assertEquals("Expected to find the same key", manager.getHMacKey(), managerAgain.getHMacKey());
        assertThat("Expected the keys to be different", manager.getKey(), is(not(managerAgain.getKey())));
    }
    
    @Test
    public void checkThatWeCanShareTheTokenKey() throws StatelessTokenKeystoreManagerException {
        //Given
        File keystoreFile = new File(folder.getRoot(), "newfile");
        char[] password = "password".toCharArray();
        StatelessTokenKeystoreManager manager = new StatelessTokenKeystoreManager(keystoreFile, password, "hmac1", "token1");
        
        //When
        StatelessTokenKeystoreManager managerAgain = new StatelessTokenKeystoreManager(keystoreFile, password, "hmac2", "token1");
        
        //Then
        assertEquals("Expected to find the same key", manager.getKey(), managerAgain.getKey());
        assertThat("Expected the hmac keys to be different", manager.getHMacKey(), is(not(managerAgain.getHMacKey())));
    }
}
