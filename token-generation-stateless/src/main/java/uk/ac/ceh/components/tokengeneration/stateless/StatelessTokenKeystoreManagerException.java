package uk.ac.ceh.components.tokengeneration.stateless;

/**
 * The following exception can be thrown my the StatelessTokenKeystoreManager. It
 * wraps up more specific java.crypto exceptions.
 * @author Christopher Johnson
 */
public class StatelessTokenKeystoreManagerException extends Exception {
    public StatelessTokenKeystoreManagerException() {
        super();
    }
    
    public StatelessTokenKeystoreManagerException(String mess) {
        super(mess);
    }
    
    public StatelessTokenKeystoreManagerException(Throwable cause) {
        super(cause);
    }
    
    public StatelessTokenKeystoreManagerException(String mess, Throwable cause) {
        super(mess, cause);
    }
}
