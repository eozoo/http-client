package com.cowave.zoo.http.client.invoke.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 *
 * @author shanhuiming
 *
 */
public class DefaultMethodInvoker implements MethodInvoker {

    private final MethodHandle unboundHandle;

    // handle is effectively final after bindTo has been called.
    private MethodHandle handle;

    public DefaultMethodInvoker(Method defaultMethod) {
        try {
            Class<?> declaringClass = defaultMethod.getDeclaringClass();
            MethodHandles.Lookup lookup;
            try {
                // Java 9及以上使用公开API，避免反射访问JDK私有字段被模块封装阻止
                Method privateLookup = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
                lookup = (MethodHandles.Lookup) privateLookup.invoke(null, declaringClass, MethodHandles.lookup());
            } catch (NoSuchMethodException exception) {
                // 保留Java 8的默认方法调用方式
                Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                constructor.setAccessible(true);
                lookup = constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE);
            }
            this.unboundHandle = lookup.unreflectSpecial(defaultMethod, declaringClass);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void bindTo(Object proxy) {
        if(handle != null) {
            throw new IllegalStateException("Attempted to rebind a default method handler that was already bound");
        }
        handle = unboundHandle.bindTo(proxy);
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {
        if(handle == null) {
            throw new IllegalStateException("Default method handler invoked before proxy has been bound.");
        }
        return handle.invokeWithArguments(argv == null ? new Object[0] : argv);
    }
}
