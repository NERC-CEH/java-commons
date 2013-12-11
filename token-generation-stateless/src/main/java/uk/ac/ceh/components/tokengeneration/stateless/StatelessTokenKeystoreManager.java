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
        
        generateKeys();
        if(!readStoredKeys()) {
            saveKeys();
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
     * new ones. A new encryption key and hmac. Use the #saveKeys method to save the keys to 
     * the keystore
     * @throws StatelessTokenKeystoreManagerException 
     */
    public final void generateKeys() throws StatelessTokenKeystoreManagerException {
        try {
            this.key = KeyGenerator.getInstance(StatelessTokenGenerator.SECRET_KEY_ALGORITHM).generateKey();
            this.hmac = KeyGenerator.getInstance(StatelessTokenGenerator.MAC_ALGORITHM).generateKey();
        }
        catch(NoSuchAlgorithmException ex) {
            throw new StatelessTokenKeystoreManagerException(ex);
        }
    }
    
    /**
     * Save the keys which are currently set on this StatelessTokenKeystoreManager to the 
     * keystore file
     * @throws StatelessTokenKeystoreManagerException if it was not possible to save the keystore 
     */
    public final void saveKeys() throws StatelessTokenKeystoreManagerException {
        KeyStore keystore = loadKeyStore();
        
        PasswordProtection passwordProtection = new PasswordProtection(password);
        try (FileOutputStream out = new FileOutputStream(keyFile)) {
            keystore.setEntry(keyAlias, new SecretKeyEntry(key), passwordProtection);
            keystore.setEntry(hmacAlias, new SecretKeyEntry(hmac), passwordProtection);
            keystore.store(out, password);
        }
        catch(KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
            throw new StatelessTokenKeystoreManagerException(ex);
        }
    }
    
    /**
     * Read the entries from the keystore
     * @return true if all entries were present and read, false otherwise
     * @throws StatelessTokenKeystoreManagerException 
     */
    public final boolean readStoredKeys() throws StatelessTokenKeystoreManagerException {
        try {
            KeyStore keystore = loadKeyStore();

            PasswordProtection passwordProtection = new PasswordProtection(password);
            Entry hmacEntry = keystore.getEntry(hmacAlias, passwordProtection),
                    keyEntry = keystore.getEntry(keyAlias, passwordProtection);

            if(hmacEntry != null) {
                hmac = ((SecretKeyEntry)hmacEntry).getSecretKey();
            }

            if(keyEntry != null) {
                key = ((SecretKeyEntry)keyEntry).getSecretKey();
            }
            //return whether or not we succeded in loading the keys from the keystore
            return keyEntry != null || hmacEntry != null;
        }
        catch(KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException ex) {
            throw new StatelessTokenKeystoreManagerException(ex);
        }
    }
    
    private KeyStore loadKeyStore() throws StatelessTokenKeystoreManagerException {
        try{
            KeyStore keystore = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);
            if(keyFile.exists()) {
                try (FileInputStream in = new FileInputStream(keyFile)) {
                    keystore.load(in, password);
                }
            }
            else {
                keystore.load(null, password);
            }
            return keystore;
        }
        catch(KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            throw new StatelessTokenKeystoreManagerException(ex);
        }
    }
}
