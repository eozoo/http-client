package org.springframework.feign.invoke;

/**
 *
 * @author shanhuiming
 *
 */
public class RemoteException extends RuntimeException {

    protected RemoteException(String message) {
        super(message);
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
