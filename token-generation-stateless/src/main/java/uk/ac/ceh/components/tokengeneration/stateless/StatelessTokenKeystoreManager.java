package uk.ac.ceh.components.tokengeneration.stateless;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * The following StatelessTokenKeyContainer will wrap up a given file which represents
 * a java keystore and obtain the specified keys from it
 * @author Christopher Johnson
 */
public class StatelessTokenKeystoreManager implements StatelessTokenKeyContainer {
    private static final char[] DEFAULT_KEYSTORE_PASSWORD = "changeit".toCharArray();
    private static final String DEFAULT_MAC_ALIAS = "token-hmac";
    private static final String DEFAULT_KEY_ALIAS = "token-key";
    private static final String DEFAULT_KEYSTORE_TYPE = "JCEKS";
    
    private final File keyFile;
    private final char[] password;
    private final String hmacAlias, keyAlias;
    
    private SecretKey key, hmac;
    
    public StatelessTokenKeystoreManager(File keyFile) throws StatelessTokenKeystoreManagerException {
        this(keyFile, DEFAULT_KEYSTORE_PASSWORD, DEFAULT_MAC_ALIAS, DEFAULT_KEY_ALIAS);
    }
    
    public StatelessTokenKeystoreManager(File keyFile, char[] password) throws StatelessTokenKeystoreManagerException {
        this(keyFile, password, DEFAULT_MAC_ALIAS, DEFAULT_KEY_ALIAS);
    }
    
    public StatelessTokenKeystoreManager(File keyFile, char[] password, String hmacAlias, String keyAlias) throws StatelessTokenKeystoreManagerException {
        this.keyFile = keyFile;
        this.password = password;
        this.hmacAlias = hmacAlias;
        this.keyAlias = keyAlias;
        
        if(keyFile.exists()) {
            KeystoreLoader keyPair = new KeystoreLoader();
            
            if(keyPair.isSetupWithKeys()) {
                keyPair.loadKeys();
            }
        }
        else {
            //Ensure that the parent folder for the key file exists. Only do this once
            File parent = keyFile.getParentFile();
            if(parent != null && !parent.exists() && !parent.mkdirs())
                throw new RuntimeException("Failed to create parent file");

            generateKeys(); //generate keys and save these to the key file
        }
    }
    
    @Override
    public SecretKey getHMacKey() {
        return hmac;
    }
    
    @Override
    public SecretKey getKey() {
        return key;
    }
    
    /**
     * The following method will reset the secrets of this StatelessTokenKeystoreManager with
     * new ones. A new encryption key and hmac. These will be saved to the
     * key file for this StatelessTokenKeystoreManager
     * @throws NoSuchAlgorithmException 
     */
    public final void generateKeys() throws StatelessTokenKeystoreManagerException {
        try {
            this.key = KeyGenerator.getInstance(StatelessTokenGenerator.SECRET_KEY_ALGORITHM).generateKey();
            this.hmac = KeyGenerator.getInstance(StatelessTokenGenerator.MAC_ALGORITHM).generateKey();
            saveKeys();
        }
        catch(NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException ex) {
            throw new StatelessTokenKeystoreManagerException(ex);
        }
    }
    
    private void saveKeys() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        try (FileOutputStream out = new FileOutputStream(keyFile)) {
            KeyStore ks = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);
            PasswordProtection passwordProtection = new PasswordProtection(password);
            ks.load(null, password);
            ks.setEntry(keyAlias, new SecretKeyEntry(key), passwordProtection);
            ks.setEntry(hmacAlias, new SecretKeyEntry(hmac), passwordProtection);
            ks.store(out, password);
        }
    }
    
    private class KeystoreLoader {
        private final Entry hmacEntry, keyEntry;
        
        public KeystoreLoader() throws StatelessTokenKeystoreManagerException {
            try (FileInputStream in = new FileInputStream(keyFile)) {
                KeyStore ks = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);
                PasswordProtection passwordProtection = new PasswordProtection(password);
                ks.load(in, password);
                
                hmacEntry = ks.getEntry(hmacAlias, passwordProtection);
                keyEntry = ks.getEntry(keyAlias, passwordProtection);
            }
            catch(IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableEntryException | KeyStoreException ex) {
                throw new StatelessTokenKeystoreManagerException(ex);
            }
        }
        
        public boolean isSetupWithKeys() {
            return hmacEntry != null && keyEntry != null;
        }
        
        public void loadKeys() {
            if(!(hmacEntry instanceof SecretKeyEntry && keyEntry instanceof SecretKeyEntry)) {
                throw new IllegalArgumentException("Either the key hmac or aes key in the key store is not a secret key.");
            }
            
            //Load the keys to the manager
            StatelessTokenKeystoreManager.this.hmac = ((SecretKeyEntry)hmacEntry).getSecretKey();
            StatelessTokenKeystoreManager.this.key = ((SecretKeyEntry)keyEntry).getSecretKey();
        }
    }
}
