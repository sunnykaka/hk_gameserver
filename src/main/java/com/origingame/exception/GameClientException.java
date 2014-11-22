package com.origingame.exception;


/**
 * User: liubin
 * Date: 14-3-5
 */
public class GameClientException extends GameException {

    public GameClientException() {
    }

    public GameClientException(String message) {
        super(message);
    }

    public GameClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameClientException(Throwable cause) {
        super(cause);
    }

    public GameClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
