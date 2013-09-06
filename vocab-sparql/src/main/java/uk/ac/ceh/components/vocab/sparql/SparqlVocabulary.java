package uk.ac.ceh.components.vocab.sparql;

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
public class SparqlVocabulary implements Vocabulary {
    private static final String HARVEST_QUERY = "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
                                                "SELECT DISTINCT ?Concept ?prefLabel " +
                                                "WHERE " +
                                                "{ ?Concept ?x skos:Concept . " +
                                                "?Concept skos:prefLabel ?prefLabel}";
    
    
    private final WebResource resource;
    private final String url;
    
    public SparqlVocabulary(String url) {
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
            SparqlResponse sparql = resource.queryParam("query", HARVEST_QUERY)
                                            .accept("application/sparql-result+xml")
                                            .get(SparqlResponse.class);
            return transformResponse(sparql);
        }
        catch(UniformInterfaceException | ClientHandlerException ex) {
            throw new VocabularyException("Unable to get concepts for sparql vocab", ex);
        }
    }
    
    private List<Concept> transformResponse(SparqlResponse sparql) {
        List<Concept> toReturn = new ArrayList<>();
        for(SparqlResult result : sparql.getResults()) {
            SparqlConcept toAdd = new SparqlConcept();
            //Sparql returns a list of binings for each result, we need to flatten 
            //this list to create a single SolrOntologyIndex (uri,term pair)
            //which we can then push into solr
            for(SparqlResult.Binding binding: result.getBindings()) {
                if(binding.getUri() != null) {
                    toAdd.setUri(binding.getUri());
                }
                if(binding.getTerm() != null) {
                    toAdd.setTerm(binding.getTerm());
                }
                toReturn.add(toAdd);
            }
        }
        return toReturn;
    }
}
