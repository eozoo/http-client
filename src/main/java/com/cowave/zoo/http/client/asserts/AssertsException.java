package com.cowave.zoo.http.client.asserts;

/**
 *
 * @author shanhuiming
 *
 */
public class AssertsException extends RuntimeException {

    public AssertsException(String message, Object... args) {
        super(I18Messages.translateIfNeed(message, args));
    }

    public AssertsException(Throwable cause, String message, Object... args) {
        super(I18Messages.translateIfNeed(message, args), cause);
    }
}
