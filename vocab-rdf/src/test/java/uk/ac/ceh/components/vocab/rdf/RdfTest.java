/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ceh.components.vocab.rdf;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.Test;

public class RdfTest {
    private JAXBContext context;
    
    public RdfTest() throws JAXBException {
        this.context = JAXBContext.newInstance(RdfResponse.class);
    }

    @Test
    public void loadPollutantConcepts() throws URISyntaxException, JAXBException {
        File pollutant = new File(getClass().getResource("pollutant.rdf").toURI());
        Unmarshaller unmarshaller = context.createUnmarshaller();
        RdfResponse response = (RdfResponse)unmarshaller.unmarshal(pollutant);
        
        System.out.println(response.getBase());
        
        List<RdfDescription> descriptions = response.getDescriptions();
        
        System.out.println("about to get descriptions");
        for (RdfDescription description : descriptions) {
            System.out.println(description.getTerm());
            System.out.println(description.getUri());
        }
    }
}