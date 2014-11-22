package com.origingame.exception;

import com.origingame.server.protocol.GameProtocol;

/**
 * User: Liub
 * Date: 2014/11/12
 */
public class GameProtocolException extends GameException {

    public GameProtocol.Status status;
    public GameProtocol protocol;


    public GameProtocolException() {
    }

    public GameProtocolException(String message) {
        super(message);
    }

    public GameProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameProtocolException(Throwable cause) {
        super(cause);
    }

    public GameProtocolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public GameProtocolException(GameProtocol.Status status, GameProtocol protocol) {
        this(String.format("status[%s], protocol[%s]", status, protocol));
        this.status = status;
        this.protocol = protocol;
    }

    public GameProtocol.Status getStatus() {
        return status;
    }

    public GameProtocol getProtocol() {
        return protocol;
    }
}
