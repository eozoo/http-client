package org.springframework.feign.invoke.method;

import feign.*;
import org.springframework.feign.annotation.Host;
import org.springframework.feign.annotation.MultipartFile;
import org.springframework.feign.annotation.MultipartForm;
import org.springframework.feign.annotation.Options;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static feign.Util.checkState;
import static feign.Util.emptyToNull;

/**
 *
 * @author shanhuiming
 *
 */
public class DefaultFeignContract extends AbstractFeignContract {

    @Override
    protected void processAnnotationOnClass(FeignMethodMetadata data, Class<?> targetType) {
        if (targetType.isAnnotationPresent(Headers.class)) {
            String[] headersOnType = targetType.getAnnotation(Headers.class).value();
            checkState(headersOnType.length > 0,
                    "Headers annotation was empty on type %s.", targetType.getName());

            Map<String, Collection<String>> headers = toMap(headersOnType);
            headers.putAll(data.template().headers());
            data.template().headers(null); // to clear
            data.template().headers(headers);
        }
    }

    @Override
    protected void processAnnotationOnMethod(FeignMethodMetadata metadata, Annotation methodAnnotation, Method method) {
        Class<? extends Annotation> annotationType = methodAnnotation.annotationType();
        if (annotationType == RequestLine.class) {
            String requestLine = ((RequestLine) methodAnnotation).value();
            checkState(emptyToNull(requestLine) != null,
                    "RequestLine annotation was empty on method %s.", method.getName());

            if (requestLine.indexOf(' ') == -1) {
                checkState(requestLine.indexOf('/') == -1,
                        "RequestLine annotation didn't start with an HTTP verb on method %s.", method.getName());
                metadata.template().method(requestLine);
                return;
            }

            metadata.template().method(requestLine.substring(0, requestLine.indexOf(' ')));
            if (requestLine.indexOf(' ') == requestLine.lastIndexOf(' ')) {
                // no HTTP version is ok
                metadata.template().append(requestLine.substring(requestLine.indexOf(' ') + 1));
            } else {
                // skip HTTP version
                metadata.template().append(requestLine.substring(requestLine.indexOf(' ') + 1, requestLine.lastIndexOf(' ')));
            }
            metadata.template().decodeSlash(((RequestLine) methodAnnotation).decodeSlash());

        } else if (annotationType == Body.class) {
            String body = ((Body) methodAnnotation).value();
            checkState(emptyToNull(body) != null,
                    "Body annotation was empty on method %s.", method.getName());

            if (body.indexOf('{') == -1) {
                metadata.template().body(body);
            } else {
                metadata.template().bodyTemplate(body);
            }
        } else if (annotationType == Headers.class) {
            String[] headersOnMethod = ((Headers) methodAnnotation).value();
            checkState(headersOnMethod.length > 0,
                    "Headers annotation was empty on method %s.", method.getName());
            metadata.template().headers(toMap(headersOnMethod));
        } else if (annotationType == Options.class) {
            Options options = (Options) methodAnnotation;
            metadata.readTimeoutMillis(options.readTimeoutMillis());
            metadata.connectTimeoutMillis(options.connectTimeoutMillis());
        }
    }

    @Override
    protected boolean processAnnotationsOnParameter(FeignMethodMetadata metadata, Annotation[] annotations, int paramIndex) {
        boolean isHttpAnnotation = false;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType == Param.class) {
                String name = ((Param) annotation).value();
                checkState(emptyToNull(name) != null,
                        "Param annotation was empty on param %s.", paramIndex);

                nameParam(metadata, name, paramIndex);
                Class<? extends Param.Expander> expander = ((Param) annotation).expander();
                if (expander != Param.ToStringExpander.class) {
                    metadata.indexToExpanderClass().put(paramIndex, expander);
                }

                isHttpAnnotation = true;
                String varName = '{' + name + '}';
                if (!metadata.template().url().contains(varName)
                        && !searchMapValuesContainsExact(metadata.template().queries(), varName)
                        && !searchMapValuesContainsSubstring(metadata.template().headers(), varName)) {
                    metadata.formParams().add(name);
                }
            } else if (annotationType == QueryMap.class) {
                checkState(metadata.queryMapIndex() == null,
                        "QueryMap annotation was present on multiple parameters.");
                metadata.queryMapIndex(paramIndex);
                metadata.queryMapEncoded(((QueryMap) annotation).encoded());
                isHttpAnnotation = true;
            } else if (annotationType == HeaderMap.class) {
                checkState(metadata.headerMapIndex() == null,
                        "HeaderMap annotation was present on multiple parameters.");
                metadata.headerMapIndex(paramIndex);
                isHttpAnnotation = true;
            } else if(annotationType == Host.class) {
                metadata.hostIndex(paramIndex);
                isHttpAnnotation = true;
            } else if(annotationType == MultipartForm.class) {
                metadata.multipartFormIndex(paramIndex);
                isHttpAnnotation = true;
            } else if(annotationType == MultipartFile.class) {
                MultipartFile multipartFile = (MultipartFile) annotation;
                metadata.multipartFileIndex(paramIndex);
                metadata.multipartFileName(multipartFile.fileName());
                metadata.multipartFileBoundary(multipartFile.boundary());
                isHttpAnnotation = true;
            }
        }
        return isHttpAnnotation;
    }

    private static <K, V> boolean searchMapValuesContainsExact(Map<K, Collection<V>> map, V search) {
        Collection<Collection<V>> values = map.values();
        for (Collection<V> entry : values) {
            if (entry.contains(search)) {
                return true;
            }
        }
        return false;
    }

    private static boolean searchMapValuesContainsSubstring(Map<String, Collection<String>> map, String search) {
        Collection<Collection<String>> values = map.values();
        for (Collection<String> entry : values) {
            for (String value : entry) {
                if (value.contains(search)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Map<String, Collection<String>> toMap(String[] input) {
        Map<String, Collection<String>> result = new LinkedHashMap<>(input.length);
        for (String header : input) {
            int colon = header.indexOf(':');
            String name = header.substring(0, colon);
            if (!result.containsKey(name)) {
                result.put(name, new ArrayList<>(1));
            }
            result.get(name).add(header.substring(colon + 2));
        }
        return result;
    }
}
