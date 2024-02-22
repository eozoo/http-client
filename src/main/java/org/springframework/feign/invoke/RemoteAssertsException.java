package org.springframework.feign.invoke;

/**
 *
 * @author shanhuiming
 *
 */
public class RemoteAssertsException extends RuntimeException {

    private static final long serialVersionUID = 2986068598247844886L;

    public RemoteAssertsException(String message) {
        super(message);
    }

    public RemoteAssertsException(String message, Throwable cause) {
        super(message, cause);
    }
}
