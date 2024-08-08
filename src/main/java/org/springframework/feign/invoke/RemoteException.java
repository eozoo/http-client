package org.springframework.feign.invoke;

/**
 *
 * @author shanhuiming
 *
 */
public class RemoteException extends RuntimeException {

    private final String url;

    private final Integer status;

    private final String code;

    public RemoteException(String url, String message) {
        super(message);
        this.url = url;
        this.status = null;
        this.code = null;
    }

    public RemoteException(String url, Integer status, String code, String message) {
        super(message);
        this.url = url;
        this.status = status;
        this.code = code;
    }

    public RemoteException(String url, Integer status, String code, String message, Throwable cause) {
        super(message, cause);
        this.url = url;
        this.status = status;
        this.code = code;
    }

    public String url(){
        return url;
    }

    public Integer status(){
        return status;
    }

    public String code(){
        return code;
    }
}
