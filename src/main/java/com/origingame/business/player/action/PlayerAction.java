package com.origingame.business.player.action;

import com.origingame.business.player.manager.PlayerManager;
import com.origingame.business.player.model.Player;
import com.origingame.config.GlobalConfig;
import com.origingame.message.EchoProtos;
import com.origingame.message.LoginProtos;
import com.origingame.message.RegisterProtos;
import com.origingame.persist.GameSessionProtos;
import com.origingame.server.action.annotation.Action;
import com.origingame.server.action.annotation.MessageType;
import com.origingame.server.context.GameContext;
import com.origingame.server.main.World;
import com.origingame.server.session.GameSession;

/**
 * User: Liub
 * Date: 2014/11/21
 */
@Action
public class PlayerAction {

    private PlayerManager playerManager = World.getBean(PlayerManager.class);

    /**
     * 注册
     * @param ctx
     * @param message
     * @return
     */
    @MessageType(GlobalConfig.PROTOBUF_MESSAGE_PACKAGE_NAME + "RegisterReq")
    public RegisterProtos.RegisterResp register(GameContext ctx, RegisterProtos.RegisterReq message) {

        Player player = playerManager.register(ctx, message);

        RegisterProtos.RegisterResp.Builder registerResp = RegisterProtos.RegisterResp.newBuilder();
        registerResp.setPlayerId(player.getId());
        registerResp.setUsername(player.getProperty().get().getUsername());
        if(message.getTrial()) {
            registerResp.setPassword(player.getProperty().get().getPassword());
        }

        return registerResp.build();

    }


    /**
     * 登录
     * @param ctx
     * @param message
     * @return
     */
    @MessageType(GlobalConfig.PROTOBUF_MESSAGE_PACKAGE_NAME + "LoginReq")
    public LoginProtos.LoginResp login(GameContext ctx, LoginProtos.LoginReq message) {
        String username = message.getUsername();
        String password = message.getPassword();

        Player player = playerManager.login(ctx, username, password);

        LoginProtos.LoginResp.Builder loginResp = LoginProtos.LoginResp.newBuilder();
        loginResp.setPlayerId(player.getId());
        loginResp.setPlayer(player.toModel());
        return loginResp.build();

    }

}
