package uk.ac.ceh.components.vocab;

/**
 *
 * @author cjohn
 */
public class VocabularyException extends Exception {

    public VocabularyException(String mess) {
        super(mess);
    }

    public VocabularyException(Throwable ex) {
        super(ex);
    }
    
    public VocabularyException(String mess, Throwable ex) {
        super(mess, ex);
    }
}
