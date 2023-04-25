package org.springframework.feign.invoke;

import feign.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import static feign.Util.checkNotNull;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignInvocationHandler implements InvocationHandler {

    @SuppressWarnings("rawtypes")
    private final Target target;
    
    private final Map<Method, InvocationHandlerFactory.MethodHandler> dispatch;

    @SuppressWarnings("rawtypes")
    FeignInvocationHandler(Target target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch) {
        this.target = checkNotNull(target, "target");
        this.dispatch = checkNotNull(dispatch, "dispatch for %s", target);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("equals".equals(method.getName())) {
            try {
                Object otherHandler = args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                return equals(otherHandler);
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else if ("hashCode".equals(method.getName())) {
            return hashCode();
        } else if ("toString".equals(method.getName())) {
            return toString();
        }
        return dispatch.get(method).invoke(args);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FeignInvocationHandler) {
            FeignInvocationHandler other = (FeignInvocationHandler) obj;
            return target.equals(other.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public String toString() {
        return target.toString();
    }
}