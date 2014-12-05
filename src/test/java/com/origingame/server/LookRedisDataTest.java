package com.origingame.server;


import com.origingame.BaseNettyTest;
import com.origingame.business.player.dao.PlayerDao;
import com.origingame.business.player.model.Player;
import com.origingame.server.main.World;
import org.testng.annotations.Test;


/**
 * Unit test for simple App.
 */
public class LookRedisDataTest extends BaseNettyTest {


    @Test
    public void test() throws Exception{

        Player player = World.getBean(PlayerDao.class).load(16);
        System.out.println(player.getProperty().get().getSessionId());

    }


}
