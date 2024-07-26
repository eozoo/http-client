package org.springframework.feign.invoke;

/**
 *
 * @author shanhuiming
 *
 */
public class RemoteAssertsException extends RemoteException {

    public RemoteAssertsException(String url, int status, int code, String message) {
        super(url, status, code, message);
    }

    public RemoteAssertsException(String url, int status, int code, String message, Throwable cause) {
        super(url, status, code, message, cause);
    }
}
