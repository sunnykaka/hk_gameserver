package com.origingame.business.player.action;

import com.origingame.BaseNettyTest;
import com.origingame.business.player.dao.PlayerDao;
import com.origingame.client.main.ClientSession;
import com.origingame.client.protocol.ClientResponseWrapper;
import com.origingame.message.BaseMsgProtos;
import com.origingame.message.RegisterProtos;
import com.origingame.server.main.World;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 *
 * 运行之前要先启动服务器
 * User: Liub
 * Date: 2014/11/21
 */
public class PlayerRegisterActionTest extends BaseNettyTest {

    private final Logger log = LoggerFactory.getLogger(PlayerRegisterActionTest.class);

    private PlayerDao playerDao = World.getBean(PlayerDao.class);

    @Test
    public void testTrailRegister() throws Exception {

        ClientSession clientSession = null;

        try {
            clientSession = initSession();

            List<Integer> playerIds = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                RegisterProtos.RegisterReq.Builder registerReq = RegisterProtos.RegisterReq.newBuilder();
                registerReq.setTrial(true);
                ClientResponseWrapper clientResponseWrapper = clientSession.sendMessage(registerReq.build());
                RegisterProtos.RegisterResp registerResp = (RegisterProtos.RegisterResp) clientResponseWrapper.getMessage();
                playerIds.add(registerResp.getPlayerId());
                assertThat(registerResp.getPlayerId(), greaterThan(0));
                assertThat(registerResp.getUsername(), not(isEmptyOrNullString()));
                assertThat(registerResp.getPassword(), not(isEmptyOrNullString()));
            }

            for(int i=0; i<playerIds.size() - 1; i++) {
                assertThat(playerIds.get(i+1), is(playerIds.get(i) + 1));
            }

        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
        }

    }

    @Test
    public void testUsernamePasswordRegister() throws Exception {

        ClientSession clientSession = null;

        try {
            clientSession = initSession();

            String username = RandomStringUtils.randomAlphabetic(6);
            String password = RandomStringUtils.randomAlphabetic(8);
            RegisterProtos.RegisterReq.Builder registerReq = RegisterProtos.RegisterReq.newBuilder();
            registerReq.setUsername(username);
            registerReq.setPassword(password);
            ClientResponseWrapper clientResponseWrapper = clientSession.sendMessage(registerReq.build());
            RegisterProtos.RegisterResp registerResp = (RegisterProtos.RegisterResp) clientResponseWrapper.getMessage();
            assertThat(registerResp.getPlayerId(), greaterThan(0));
            assertThat(registerResp.getUsername(), is(username));
            assertThat(registerResp.getPassword(), isEmptyOrNullString());

        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
        }

    }

    @Test
    public void testUsernameExist() throws Exception {

        ClientSession clientSession = null;

        try {
            clientSession = initSession();

            String username = RandomStringUtils.randomAlphabetic(6);
            String password = RandomStringUtils.randomAlphabetic(8);
            RegisterProtos.RegisterReq.Builder registerReq = RegisterProtos.RegisterReq.newBuilder();
            registerReq.setUsername(username);
            registerReq.setPassword(password);
            ClientResponseWrapper clientResponseWrapper = clientSession.sendMessage(registerReq.build());
            RegisterProtos.RegisterResp registerResp = (RegisterProtos.RegisterResp) clientResponseWrapper.getMessage();
            assertThat(registerResp.getPlayerId(), greaterThan(0));
            assertThat(registerResp.getUsername(), is(username));
            assertThat(registerResp.getPassword(), isEmptyOrNullString());

            registerReq = RegisterProtos.RegisterReq.newBuilder();
            registerReq.setUsername(username);
            registerReq.setPassword(password);
            clientResponseWrapper = clientSession.sendMessage(registerReq.build());
            registerResp = (RegisterProtos.RegisterResp) clientResponseWrapper.getMessage();
            assertThat(registerResp, nullValue());
            assertThat(clientResponseWrapper.isSuccess(), is(true));
            assertThat(clientResponseWrapper.getResponseMsg().getStatus(), is(BaseMsgProtos.ResponseStatus.USERNAME_EXIST));


        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
        }

    }





}
