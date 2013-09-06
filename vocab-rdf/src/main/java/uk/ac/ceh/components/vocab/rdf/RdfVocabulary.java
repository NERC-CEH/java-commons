package uk.ac.ceh.components.vocab.rdf;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import java.util.List;
import uk.ac.ceh.components.vocab.Concept;
import uk.ac.ceh.components.vocab.Vocabulary;
import uk.ac.ceh.components.vocab.VocabularyException;

/**
 *
 * @author Christopher Johnson
 */
public class RdfVocabulary implements Vocabulary {
    private final WebResource resource;
    private final String url;
    
    public RdfVocabulary(String url) {
        this.resource = Client.create().resource(url);
        this.url = url;
    }
    
    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public List<Concept> getAllConcepts() throws VocabularyException {
        try {
            RdfResponse rdf = resource.accept("application/rdf+xml").get(RdfResponse.class);
            return new ArrayList<Concept>(rdf.getDescriptions());
        }
        catch(UniformInterfaceException | ClientHandlerException ex) {
            throw new VocabularyException("Unable to get concepts for sparql vocab", ex);
        }
    }
}
