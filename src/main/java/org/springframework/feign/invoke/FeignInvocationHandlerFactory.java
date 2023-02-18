package org.springframework.feign.invoke;

import feign.InvocationHandlerFactory;
import feign.Target;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignInvocationHandlerFactory implements InvocationHandlerFactory {

    @Override
    public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
        return new FeignInvocationHandler(target, dispatch);
    }
}
