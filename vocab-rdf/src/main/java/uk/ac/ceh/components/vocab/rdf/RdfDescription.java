package uk.ac.ceh.components.vocab.rdf;

import java.net.URI;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;
import uk.ac.ceh.components.vocab.Concept;

/**
 *
 * @author Christopher Johnson
 */
@Data
public class RdfDescription implements Concept {
    @XmlAttribute
    private URI about;
    
    @XmlElement(namespace="http://www.w3.org/2004/02/skos/core#")
    private String definition, prefLabel;

    @Override
    public String getTerm() {
        return prefLabel;
    }

    @Override
    public String getUri() {
        return about.toString();
    }
    
    public void afterUnmarshal(Unmarshaller u, Object parent) {
        RdfResponse response = (RdfResponse)parent;
        if(response.getBase() != null) { //If the response has a base, use it to resolve the relative uri
            this.about = response.getBase().resolve(about);
        }
    }
}