package com.cowave.zoo.http.client.invoke.proxy;

/**
 *
 * @author shanhuiming
 *
 */
public interface MethodInvoker {

    Object invoke(Object[] argv) throws Throwable;
}
