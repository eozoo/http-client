package org.springframework.feign.invoke.method;

import feign.Feign;
import feign.Util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.*;

import static feign.Util.checkState;

/**
 *
 * @author shanhuiming
 *
 */
public abstract class AbstractFeignContract implements FeignContract {

    @Override
    public List<FeignMethodMetadata> parseAndValidateMetadata(Class<?> targetType) {
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

    @Deprecated
    public FeignMethodMetadata parseAndValidateMetadata(Method method) {
        return parseAndValidateMetadata(method.getDeclaringClass(), method);
    }

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

    protected void processAnnotationOnClass(FeignMethodMetadata data, Class<?> clz) {

    }

    protected abstract void processAnnotationOnMethod(FeignMethodMetadata data, Annotation annotation, Method method);

    protected abstract boolean processAnnotationsOnParameter(FeignMethodMetadata data, Annotation[] annotations, int paramIndex);


    protected Collection<String> addTemplatedParam(Collection<String> possiblyNull, String name) {
        if (possiblyNull == null) {
            possiblyNull = new ArrayList<>();
        }
        possiblyNull.add(String.format("{%s}", name));
        return possiblyNull;
    }

    protected void nameParam(FeignMethodMetadata data, String name, int i) {
        Collection<String> names =
                data.indexToName().containsKey(i) ? data.indexToName().get(i) : new ArrayList<String>();
        names.add(name);
        data.indexToName().put(i, names);
    }
}
