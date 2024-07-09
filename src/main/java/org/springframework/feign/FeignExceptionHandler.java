package org.springframework.feign;

import org.springframework.feign.invoke.RemoteException;

/**
 *
 * @author shanhuiming
 *
 */
public interface FeignExceptionHandler {

    void handle(RemoteException e);
}
