package uk.ac.ceh.components.tokengeneration.stateless;

import javax.crypto.SecretKey;

/**
 * The following interface specifics how to get obtain the keys required by the
 * StatelessTokenKeyGenerator
 * @author Christopher Johnson
 */
public interface StatelessTokenKeyContainer {
    SecretKey getHMacKey();
    SecretKey getKey();
}
