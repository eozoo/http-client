package org.springframework.feign.invoke;

import feign.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignImplement extends Feign {
    private final FeignParseHandlersByName targetToHandlersByName;
    private final InvocationHandlerFactory factory;

    FeignImplement(FeignParseHandlersByName targetToHandlersByName, InvocationHandlerFactory factory) {
        this.targetToHandlersByName = targetToHandlersByName;
        this.factory = factory;
    }

    /**
     * creates an api binding to the {@code target}. As this invokes reflection, care should be taken
     * to cache the result.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T newInstance(Target<T> target) {
        Map<String, InvocationHandlerFactory.MethodHandler> nameToHandler = targetToHandlersByName.apply(target);
        Map<Method, InvocationHandlerFactory.MethodHandler> methodToHandler = new LinkedHashMap<>();
        List<FeignMethodHandler> defaultMethodHandlers = new LinkedList<>();

        for (Method method : target.type().getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            } else if(Util.isDefault(method)) {
                FeignMethodHandler handler = new FeignMethodHandler(method);
                defaultMethodHandlers.add(handler);
                methodToHandler.put(method, handler);
            } else {
                methodToHandler.put(method, nameToHandler.get(Feign.configKey(target.type(), method)));
            }
        }

        InvocationHandler handler = factory.create(target, methodToHandler);
        T proxy = (T) Proxy.newProxyInstance(target.type().getClassLoader(), new Class<?>[]{target.type()}, handler);

        for(FeignMethodHandler defaultMethodHandler : defaultMethodHandlers) {
            defaultMethodHandler.bindTo(proxy);
        }
        return proxy;
    }
}
