package uk.ac.ceh.components.dynamo;

import uk.ac.ceh.components.dynamo.annotations.ServiceURL;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 *
 * @author Christopher Johnson
 */
public class ServiceURLArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String URL_ENCODING = "UTF-8";
    
    private final List<QueryParameterResolver> queryParameterResolvers;
    
    public ServiceURLArgumentResolver(List<QueryParameterResolver> queryParameterResolvers) {
        this.queryParameterResolvers = queryParameterResolvers;
    }
    
    @Override
    public boolean supportsParameter(MethodParameter mp) {
        return mp.hasParameterAnnotation(ServiceURL.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavc, NativeWebRequest webRequest, WebDataBinderFactory wdbf) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        StringBuilder toReturn = new StringBuilder(request.getRequestURL()).append("?");
        Set<String> mapServiceApplicableQueryParams = getMapServiceApplicableQueryParams(methodParameter.getMethod());
        Map<String, String[]> parameters = request.getParameterMap();
        for(Map.Entry<String, String[]> paramEntry : parameters.entrySet()) {
            String paramKey = paramEntry.getKey();
            if(mapServiceApplicableQueryParams.contains(paramKey)) {
                for(String paramValue : paramEntry.getValue()) {
                    toReturn
                        .append(URLEncoder.encode(paramKey, URL_ENCODING))
                        .append("=")
                        .append(URLEncoder.encode(paramValue, URL_ENCODING))
                        .append("&");
                }
            }
        }
        return toReturn.toString();
    }
    
    private Set<String> getMapServiceApplicableQueryParams(Method method) {
        Set<String> queryParams = new HashSet<>();
        
        for(MethodParameter methodParameter: getMethodParameters(method)) {
            for(QueryParameterResolver resolver : queryParameterResolvers) {
                if(resolver.supports(methodParameter)) {
                    queryParams.addAll(resolver.getUtilisedQueryParameters(methodParameter));
                }
            }
        }
        return queryParams;
    }
    
    private List<MethodParameter> getMethodParameters(Method method) {
        List<MethodParameter> toReturn = new ArrayList<>();
        for(int i=0; i<method.getParameterTypes().length; i++) {
            toReturn.add(new MethodParameter(method, i));
        }
        return toReturn;
    }
}