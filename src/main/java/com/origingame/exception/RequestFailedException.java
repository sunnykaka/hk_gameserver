package com.origingame.exception;


/**
 * User: liubin
 * Date: 14-3-5
 */
public class RequestFailedException extends GameClientException {

    public RequestFailedException() {
    }

    public RequestFailedException(String message) {
        super(message);
    }

    public RequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestFailedException(Throwable cause) {
        super(cause);
    }

    public RequestFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
