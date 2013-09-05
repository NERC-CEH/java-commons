package uk.ac.ceh.components.vocab;

import java.util.List;

/**
 *
 * @author CJOHN
 */
public interface Vocabulary {
    public String getUrl();
    public List<Concept> getAllConcepts();
}