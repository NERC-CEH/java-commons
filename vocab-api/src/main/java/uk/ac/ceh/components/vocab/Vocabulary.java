package uk.ac.ceh.components.vocab;

import java.util.List;

/**
 *
 * @author CJOHN
 */
public interface Vocabulary {
    public String getUrl();
    public String getName();
    public List<Concept> getAllConcepts() throws VocabularyException;
}