package uk.ac.ceh.components.dynamo;

import java.util.Set;
import org.springframework.core.MethodParameter;

/**
 *
 * @author Christopher Johnson
 */
public interface QueryParameterResolver {
    boolean supports(MethodParameter methodParameter);
    
    Set<String> getUtilisedQueryParameters(MethodParameter methodParameter);
}
