package org.springframework.feign.invoke;

/**
 *
 * @author shanhuiming
 *
 */
public class RemoteAssertsException extends RemoteException {

    public RemoteAssertsException(String message) {
        super(message);
    }

    public RemoteAssertsException(String message, Throwable cause) {
        super(message, cause);
    }
}
