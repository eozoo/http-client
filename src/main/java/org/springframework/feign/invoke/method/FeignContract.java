package org.springframework.feign.invoke.method;

import feign.*;
import org.springframework.feign.annotation.Host;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.*;

import static feign.Util.checkState;
import static feign.Util.emptyToNull;

/**
 *
 * @author shanhuiming
 *
 */
public interface FeignContract {

    List<FeignMethodMetadata> parseAndValidatateMetadata(Class<?> targetType);

    abstract class BaseContract implements FeignContract {

        @Override
        public List<FeignMethodMetadata> parseAndValidatateMetadata(Class<?> targetType) {
            checkState(targetType.getTypeParameters().length == 0,
                    "Parameterized types unsupported: %s", targetType.getSimpleName());
            checkState(targetType.getInterfaces().length <= 1,
                    "Only single inheritance supported: %s", targetType.getSimpleName());

            if (targetType.getInterfaces().length == 1) {
                checkState(targetType.getInterfaces()[0].getInterfaces().length == 0,
                        "Only single-level inheritance supported: %s", targetType.getSimpleName());
            }

            Map<String, FeignMethodMetadata> result = new LinkedHashMap<>();
            for (Method method : targetType.getMethods()) {
                if (method.getDeclaringClass() == Object.class
                        || (method.getModifiers() & Modifier.STATIC) != 0
                        || Util.isDefault(method)) {
                    continue;
                }

                FeignMethodMetadata metadata = parseAndValidateMetadata(targetType, method);
                checkState(!result.containsKey(metadata.configKey()),
                        "Overrides unsupported: %s", metadata.configKey());
                result.put(metadata.configKey(), metadata);
            }
            return new ArrayList<>(result.values());
        }

        /**
         * @deprecated use {@link #parseAndValidateMetadata(Class, Method)} instead.
         */
        @Deprecated
        public FeignMethodMetadata parseAndValidatateMetadata(Method method) {
            return parseAndValidateMetadata(method.getDeclaringClass(), method);
        }

        /**
         * Called indirectly by {@link #parseAndValidatateMetadata(Class)}.
         */
        protected FeignMethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
            FeignMethodMetadata metadata = new FeignMethodMetadata();
            metadata.returnType(Types.resolve(targetType, targetType, method.getGenericReturnType()));
            metadata.configKey(Feign.configKey(targetType, method));

            // class注解
            if(targetType.getInterfaces().length == 1) {
                processAnnotationOnClass(metadata, targetType.getInterfaces()[0]);
            }
            processAnnotationOnClass(metadata, targetType);

            // method注解
            for (Annotation methodAnnotation : method.getAnnotations()) {
                processAnnotationOnMethod(metadata, methodAnnotation, method);
            }
            checkState(metadata.template().method() != null,
                    "Method %s not annotated with HTTP method type (ex. GET, POST)", method.getName());

            // parameter注解
            Class<?>[] parameterTypes = method.getParameterTypes();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                boolean isHttpAnnotation = false;
                if (parameterAnnotations[i] != null) {
                    isHttpAnnotation = processAnnotationsOnParameter(metadata, parameterAnnotations[i], i);
                }

                if (parameterTypes[i] == URI.class) {
                    metadata.urlIndex(i);
                } else if (!isHttpAnnotation) {
                    checkState(metadata.formParams().isEmpty(),
                                "Body parameters cannot be used with form parameters.");
                    checkState(metadata.bodyIndex() == null,
                                "Method has too many Body parameters: %s", method);
                    metadata.bodyIndex(i);
                    metadata.bodyType(Types.resolve(targetType, targetType, method.getGenericParameterTypes()[i]));
                }
            }

            if (metadata.headerMapIndex() != null) {
                checkState(Map.class.isAssignableFrom(parameterTypes[metadata.headerMapIndex()]),
                        "HeaderMap parameter must be a Map: %s", parameterTypes[metadata.headerMapIndex()]);
            }

            if (metadata.queryMapIndex() != null) {
                checkState(Map.class.isAssignableFrom(parameterTypes[metadata.queryMapIndex()]),
                        "QueryMap parameter must be a Map: %s", parameterTypes[metadata.queryMapIndex()]);
            }

            return metadata;
        }

        /**
         * Called by parseAndValidateMetadata twice, first on the declaring class, then on the
         * target type (unless they are the same).
         *
         * @param data       metadata collected so far relating to the current java method.
         * @param clz        the class to process
         */
        protected void processAnnotationOnClass(FeignMethodMetadata data, Class<?> clz) {

        }

        /**
         * @param data       metadata collected so far relating to the current java method.
         * @param annotation annotations present on the current method annotation.
         * @param method     method currently being processed.
         */
        protected abstract void processAnnotationOnMethod(FeignMethodMetadata data, Annotation annotation, Method method);

        /**
         * @param data        metadata collected so far relating to the current java method.
         * @param annotations annotations present on the current parameter annotation.
         * http-relevant annotation.
         */
        protected abstract boolean processAnnotationsOnParameter(FeignMethodMetadata data, Annotation[] annotations, int paramIndex);


        protected Collection<String> addTemplatedParam(Collection<String> possiblyNull, String name) {
            if (possiblyNull == null) {
                possiblyNull = new ArrayList<>();
            }
            possiblyNull.add(String.format("{%s}", name));
            return possiblyNull;
        }

        /**
         * links a parameter name to its index in the method signature.
         */
        protected void nameParam(FeignMethodMetadata data, String name, int i) {
            Collection<String>
                    names =
                    data.indexToName().containsKey(i) ? data.indexToName().get(i) : new ArrayList<String>();
            names.add(name);
            data.indexToName().put(i, names);
        }
    }

    class Default extends FeignContract.BaseContract {
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
        protected void processAnnotationOnMethod(FeignMethodMetadata data, Annotation methodAnnotation, Method method) {
            Class<? extends Annotation> annotationType = methodAnnotation.annotationType();
            if (annotationType == RequestLine.class) {
                String requestLine = ((RequestLine) methodAnnotation).value();
                checkState(emptyToNull(requestLine) != null,
                        "RequestLine annotation was empty on method %s.", method.getName());

                if (requestLine.indexOf(' ') == -1) {
                    checkState(requestLine.indexOf('/') == -1,
                            "RequestLine annotation didn't start with an HTTP verb on method %s.", method.getName());
                    data.template().method(requestLine);
                    return;
                }

                data.template().method(requestLine.substring(0, requestLine.indexOf(' ')));
                if (requestLine.indexOf(' ') == requestLine.lastIndexOf(' ')) {
                    // no HTTP version is ok
                    data.template().append(requestLine.substring(requestLine.indexOf(' ') + 1));
                } else {
                    // skip HTTP version
                    data.template().append(requestLine.substring(requestLine.indexOf(' ') + 1, requestLine.lastIndexOf(' ')));
                }

                data.template().decodeSlash(((RequestLine) methodAnnotation).decodeSlash());

            } else if (annotationType == Body.class) {
                String body = ((Body) methodAnnotation).value();
                checkState(emptyToNull(body) != null,
                        "Body annotation was empty on method %s.", method.getName());

                if (body.indexOf('{') == -1) {
                    data.template().body(body);
                } else {
                    data.template().bodyTemplate(body);
                }
            } else if (annotationType == Headers.class) {
                String[] headersOnMethod = ((Headers) methodAnnotation).value();
                checkState(headersOnMethod.length > 0,
                        "Headers annotation was empty on method %s.", method.getName());
                data.template().headers(toMap(headersOnMethod));
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
}
