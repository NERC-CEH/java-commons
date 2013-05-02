package uk.ac.ceh.components.userstore.springsecurity;

import java.security.Principal;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * By registering this ArgumentResolver in your spring configuration, spring mvc 
 * methods which have an argument annotated with {@link ActiveUser} will be able
 * to obtain the application domain Principle Object if the other classes in this
 * package are registered in spring security.
 * @author cjohn
 */
public class ActiveUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {  
    @Override
    public boolean supportsParameter(MethodParameter mp) {
        return mp.hasParameterAnnotation(ActiveUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavc, NativeWebRequest webRequest, WebDataBinderFactory wdbf) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null) {
            return authentication.getPrincipal();
        }
        else {
            return null;
        }
    }
}
