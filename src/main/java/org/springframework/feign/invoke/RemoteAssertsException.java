package org.springframework.feign.invoke;

/**
 *
 * @author shanhuiming
 *
 */
public class RemoteAssertsException extends RemoteException {

    public RemoteAssertsException(String url, int httpStatus, int respCode, String message) {
        super(url, httpStatus, respCode, message);
    }

    public RemoteAssertsException(String url, int httpStatus, int respCode, String message, Throwable cause) {
        super(url, httpStatus, respCode, message, cause);
    }
}
