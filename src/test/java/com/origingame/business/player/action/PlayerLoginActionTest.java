package com.origingame.business.player.action;

import com.origingame.BaseNettyTest;
import com.origingame.business.player.dao.PlayerDao;
import com.origingame.client.main.ClientSession;
import com.origingame.client.protocol.ClientResponseWrapper;
import com.origingame.message.*;
import com.origingame.model.PlayerModelProtos;
import com.origingame.server.main.World;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;


/**
 *
 * 运行之前要先启动服务器
 * User: Liub
 * Date: 2014/11/21
 */
public class PlayerLoginActionTest extends BaseNettyTest {

    private final Logger log = LoggerFactory.getLogger(PlayerLoginActionTest.class);

    private PlayerDao playerDao = World.getBean(PlayerDao.class);


    /**
     * 1.正常登录并发echo.校验返回结果正确,并且数据库的数据已更新.
     * 2.用户名错误,校验返回结果.密码错误,校验返回结果
     * 3.连续登录10次并发echo,校验返回结果.
     * 4.会话A登录一次发送echo,然后会话B登录一次发送echo,会话A再发送echo,校验返回结果应该提示A会话无效
     * 5.同一会话,先登录A,再登录B,修改会话设置成A的ID,再发echo,检验返回结果提示会话无效.
     *
     */

    /**
     *
     * @throws Exception
     */
    @Test
    public void testLogin() throws Exception {

        ClientSession clientSession = null;

        try {
            clientSession = initSession();

            String username = RandomStringUtils.randomAlphabetic(6);
            String password = RandomStringUtils.randomAlphabetic(8);

            register(clientSession, username, password);

            for (int i = 0; i < 10; i++) {
                ClientResponseWrapper clientResponseWrapper = clientSession.login(username, password);
                checkLoginResp(clientSession, username, password, clientResponseWrapper);
                sendAndCheckPlayerEcho(clientSession);
            }

        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
        }

    }

    @Test
    public void testUsernameOrPasswordErrorLogin() throws Exception {

        ClientSession clientSession = null;

        try {
            clientSession = initSession();

            String username = RandomStringUtils.randomAlphabetic(6);
            String password = RandomStringUtils.randomAlphabetic(8);

            register(clientSession, username, password);

            ClientResponseWrapper clientResponseWrapper = clientSession.login("not exist username", password);
            assertThat(clientResponseWrapper.isSuccess(), is(true));
            assertThat(clientResponseWrapper.getResponseMsg().getStatus(), is(BaseMsgProtos.ResponseStatus.USERNAME_OR_PASSWORD_INCORRECT));
            assertThat(clientResponseWrapper.getMessage(), is(nullValue()));

            clientResponseWrapper = clientSession.login(username, "not exist password");
            assertThat(clientResponseWrapper.isSuccess(), is(true));
            assertThat(clientResponseWrapper.getResponseMsg().getStatus(), is(BaseMsgProtos.ResponseStatus.USERNAME_OR_PASSWORD_INCORRECT));
            assertThat(clientResponseWrapper.getMessage(), is(nullValue()));

            clientResponseWrapper = clientSession.login(username, password);
            checkLoginResp(clientSession, username, password, clientResponseWrapper);
            sendAndCheckPlayerEcho(clientSession);

        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
        }

    }

    @Test
    public void testMultiSessionWithPlayer() throws Exception {

        ClientSession clientSession = null;
        ClientSession clientSession2 = null;

        try {
            clientSession = initSession();

            String username = RandomStringUtils.randomAlphabetic(6);
            String password = RandomStringUtils.randomAlphabetic(8);
            register(clientSession, username, password);

            //session1登录
            ClientResponseWrapper clientResponseWrapper = clientSession.login(username, password);
            checkLoginResp(clientSession, username, password, clientResponseWrapper);
            sendAndCheckPlayerEcho(clientSession);

            //session2登录
            clientSession2 = initSession();
            clientResponseWrapper = clientSession2.login(username, password);
            checkLoginResp(clientSession2, username, password, clientResponseWrapper);
            sendAndCheckPlayerEcho(clientSession2);

            //session1再发echo,应该失败
            clientResponseWrapper = sendPlayerEcho(clientSession, "你好啊123");
            assertThat(clientResponseWrapper.isSuccess(), is(true));
            assertThat(clientResponseWrapper.getResponseMsg(), notNullValue());
            assertThat(clientResponseWrapper.getMessage(), nullValue());

            assertThat(clientResponseWrapper.getResponseMsg().getStatus(), is(BaseMsgProtos.ResponseStatus.MULTI_SESSION_WITH_PLAYER));

            //重新用session1再登录,成功
            clientResponseWrapper = clientSession.login(username, password);
            checkLoginResp(clientSession, username, password, clientResponseWrapper);
            sendAndCheckPlayerEcho(clientSession);


        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
            if(clientSession2 != null) {
                clientSession2.destroy();
            }

        }

    }

    @Test
    public void testMultiPlayerWithSession() throws Exception {

        ClientSession clientSession = null;
        ClientSession clientSession2 = null;

        try {
            clientSession = initSession();

            String username1 = RandomStringUtils.randomAlphabetic(6);
            String password1 = RandomStringUtils.randomAlphabetic(8);
            String username2 = RandomStringUtils.randomAlphabetic(6);
            String password2 = RandomStringUtils.randomAlphabetic(8);


            //player1登录注册
            register(clientSession, username1, password1);
            ClientResponseWrapper clientResponseWrapper = clientSession.login(username1, password1);
            checkLoginResp(clientSession, username1, password1, clientResponseWrapper);
            sendAndCheckPlayerEcho(clientSession);
            PlayerModelProtos.PlayerModel player1 = clientSession.getPlayer();
            int playerId1 = clientSession.getPlayerId();

            //player2登录注册
            register(clientSession, username2, password2);
            clientResponseWrapper = clientSession.login(username2, password2);
            checkLoginResp(clientSession, username2, password2, clientResponseWrapper);
            sendAndCheckPlayerEcho(clientSession);

            //把player2的id设置到session再发echo,应该失败
            clientSession.setPlayer(player1);
            clientSession.setPlayerId(playerId1);
            clientResponseWrapper = sendPlayerEcho(clientSession, "你好啊123");
            assertThat(clientResponseWrapper.isSuccess(), is(true));
            assertThat(clientResponseWrapper.getResponseMsg(), notNullValue());
            assertThat(clientResponseWrapper.getMessage(), nullValue());

            assertThat(clientResponseWrapper.getResponseMsg().getStatus(), is(BaseMsgProtos.ResponseStatus.MULTI_PLAYER_WITH_SESSION));

        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
            if(clientSession2 != null) {
                clientSession2.destroy();
            }

        }

    }


    protected void checkLoginResp(ClientSession clientSession, String username, String password, ClientResponseWrapper clientResponseWrapper) {
        LoginProtos.LoginResp loginResp = (LoginProtos.LoginResp)clientResponseWrapper.getMessage();
        PlayerModelProtos.PlayerModel player = loginResp.getPlayer();
        assertThat(player, notNullValue());
        assertThat(player.getProperty().getUsername(), is(username));
        assertThat(player.getProperty().getPassword(), is(password));
        assertThat(player.getProperty().getSessionId(), is(clientSession.getSessionId()));
        assertThat(player.getProperty().getOuterId(), notNullValue());
    }

    protected PlayerEchoProtos.PlayerEchoResp sendAndCheckPlayerEcho(ClientSession clientSession) {
        String msg = "你好啊123";
        ClientResponseWrapper clientResponseWrapper = sendPlayerEcho(clientSession, msg);
        PlayerEchoProtos.PlayerEchoResp playerEchoResp = (PlayerEchoProtos.PlayerEchoResp) clientResponseWrapper.getMessage();
        assertThat(playerEchoResp.getMessage(), is(msg));

        assertThat(playerEchoResp.getPlayerId(), is(clientSession.getPlayerId()));
        assertThat(playerEchoResp.getUsername(), is(clientSession.getPlayer().getProperty().getUsername()));
        assertThat(playerEchoResp.getPassword(), is(clientSession.getPlayer().getProperty().getPassword()));

        return playerEchoResp;
    }

    protected ClientResponseWrapper sendPlayerEcho(ClientSession clientSession, String msg) {
        PlayerEchoProtos.PlayerEchoReq.Builder playerEchoReq = PlayerEchoProtos.PlayerEchoReq.newBuilder();
        playerEchoReq.setMessage(msg);
        return  clientSession.sendMessage(playerEchoReq.build());
    }



    protected ClientResponseWrapper register(ClientSession clientSession, String username, String password) {
        RegisterProtos.RegisterReq.Builder registerReq = RegisterProtos.RegisterReq.newBuilder();
        registerReq.setUsername(username);
        registerReq.setPassword(password);
        ClientResponseWrapper clientResponseWrapper = clientSession.sendMessage(registerReq.build());
        RegisterProtos.RegisterResp registerResp = (RegisterProtos.RegisterResp) clientResponseWrapper.getMessage();
        assertThat(registerResp.getPlayerId(), greaterThan(0));
        assertThat(registerResp.getUsername(), is(username));
        assertThat(registerResp.getPassword(), isEmptyOrNullString());

        return clientResponseWrapper;
    }



}
