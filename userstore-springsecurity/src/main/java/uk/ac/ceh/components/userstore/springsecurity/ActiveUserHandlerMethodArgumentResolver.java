
package uk.ac.ceh.components.userstore.springsecurity;

import java.security.Principal;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 *
 * @author cjohn
 */
public class ActiveUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {  
    @Override
    public boolean supportsParameter(MethodParameter mp) {
        return mp.hasParameterAnnotation(ActiveUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavc, NativeWebRequest webRequest, WebDataBinderFactory wdbf) throws Exception {
        Principal principal = webRequest.getUserPrincipal();
        return ((Authentication) principal).getPrincipal();
    }

}
