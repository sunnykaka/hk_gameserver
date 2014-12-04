package com.origingame.business.echo.action;

import com.origingame.business.player.model.Player;
import com.origingame.config.GlobalConfig;
import com.origingame.message.EchoProtos;
import com.origingame.message.PlayerEchoProtos;
import com.origingame.server.action.annotation.Action;
import com.origingame.server.action.annotation.CheckPlayer;
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

    @MessageType(GlobalConfig.PROTOBUF_MESSAGE_PACKAGE_NAME + "PlayerEchoReq")
    @CheckPlayer(lock = true)
    public PlayerEchoProtos.PlayerEchoResp playerEcho(GameContext ctx, PlayerEchoProtos.PlayerEchoReq message) {

        Player player = ctx.getPlayer();
        PlayerEchoProtos.PlayerEchoResp.Builder playerResp = PlayerEchoProtos.PlayerEchoResp.newBuilder();
        playerResp.setMessage(message.getMessage());
        playerResp.setPlayerId(player.getId());
        playerResp.setUsername(player.getProperty().get().getUsername());
        playerResp.setPassword(player.getProperty().get().getPassword());

        return playerResp.build();
    }


}
