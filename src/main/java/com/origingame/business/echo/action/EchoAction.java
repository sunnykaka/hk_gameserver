package com.origingame.business.echo.action;

import com.origingame.config.GlobalConfig;
import com.origingame.message.EchoProtos;
import com.origingame.server.action.annotation.Action;
import com.origingame.server.action.annotation.MessageType;
import com.origingame.server.context.GameContext;

/**
 * User: Liub
 * Date: 2014/11/21
 */
@Action
public class EchoAction {

    @MessageType(GlobalConfig.PROTOBUF_MESSAGE_PACKAGE_NAME + "Echo")
    public EchoProtos.Echo echo(GameContext ctx, EchoProtos.Echo message) {

        EchoProtos.Echo.Builder echo = EchoProtos.Echo.newBuilder();
        echo.setMessage(message.getMessage());
        return echo.build();
    }


}
