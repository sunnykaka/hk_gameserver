package com.origingame.exception;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class ItemResolveException extends GameException {

    public ItemResolveException() {
    }

    public ItemResolveException(String message) {
        super(message);
    }

    public ItemResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemResolveException(Throwable cause) {
        super(cause);
    }

    public ItemResolveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
