package org.springframework.feign.invoke;

/**
 *
 * @author shanhuiming
 *
 */
public class RemoteException extends RuntimeException {

    private final String url;

    private final int httpStatus;

    private final int respCode;

    public RemoteException(String url, int httpStatus, int respCode, String message) {
        super(message);
        this.url = url;
        this.httpStatus = httpStatus;
        this.respCode = respCode;
    }

    public RemoteException(String url, int httpStatus, int respCode, String message, Throwable cause) {
        super(message, cause);
        this.url = url;
        this.httpStatus = httpStatus;
        this.respCode = respCode;
    }

    public String url(){
        return url;
    }

    public int httpStatus(){
        return httpStatus;
    }

    public int respCode(){
        return respCode;
    }
}
