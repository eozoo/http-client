package org.springframework.feign.invoke;

/**
 *
 * @author shanhuiming
 *
 */
public class RemoteException extends RuntimeException {

    private final String url;

    private final int status;

    private final int code;

    public RemoteException(String url, int status, int code, String message) {
        super(message);
        this.url = url;
        this.status = status;
        this.code = code;
    }

    public RemoteException(String url, int status, int code, String message, Throwable cause) {
        super(message, cause);
        this.url = url;
        this.status = status;
        this.code = code;
    }

    public String url(){
        return url;
    }

    public int status(){
        return status;
    }

    public int code(){
        return code;
    }
}
