package com.origingame.server.exception;


import com.origingame.message.BaseMsgProtos;

/**
 * User: liubin
 * Date: 14-3-5
 */
public class GameBusinessException extends GameException {

    private BaseMsgProtos.ResponseStatus responseStatus;

    private String msg;

    public GameBusinessException(BaseMsgProtos.ResponseStatus responseStatus, String msg) {
        this.responseStatus = responseStatus;
        this.msg = msg;
    }

    public GameBusinessException(BaseMsgProtos.ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }


    public BaseMsgProtos.ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public String getMsg() {
        return msg;
    }

}
