package com.origingame.exception;


/**
 * User: liubin
 * Date: 14-3-5
 */
public class GameDaoException extends GameException {

    public GameDaoException() {
    }

    public GameDaoException(String message) {
        super(message);
    }

    public GameDaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameDaoException(Throwable cause) {
        super(cause);
    }

    public GameDaoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
