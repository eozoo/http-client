package org.springframework.feign.invoke;

/**
 *
 * @author shanhuiming
 *
 */
public class RemoteException extends RuntimeException {

    private static final long serialVersionUID = 2986068598247844886L;

    protected RemoteException(String message) {
        super(message);
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
