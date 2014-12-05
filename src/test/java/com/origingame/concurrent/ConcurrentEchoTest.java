package com.origingame.concurrent;

import com.google.common.base.Stopwatch;
import com.origingame.BaseNettyTest;
import com.origingame.business.player.action.PlayerLoginActionTest;
import com.origingame.client.main.ClientSession;
import com.origingame.client.protocol.ClientResponseWrapper;
import com.origingame.message.EchoProtos;
import com.origingame.message.PlayerEchoProtos;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;


/**
 *
 * 运行之前需要重新启动服务器
 *
 * User: Liub
 * Date: 2014/11/21
 */
public class ConcurrentEchoTest extends PlayerLoginActionTest {

    private final Logger log = LoggerFactory.getLogger(ConcurrentEchoTest.class);

    private int runThreadCount = 100;
    private int runTimeCount = 100;

    private List<Integer> countList = Collections.synchronizedList(new ArrayList<Integer>());

    /**
     * @throws Exception
     */
    @Test
    public void test() throws Exception {

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < runThreadCount ; i++) {
            Thread t = new Thread(new EchoRunnable(i));
            threads.add(t);
        }

        Stopwatch sw = Stopwatch.createStarted();
        for(Thread t : threads) {
            t.start();
        }
        for(Thread t : threads) {
            t.join();
        }
        sw.stop();

        Collections.sort(countList);

        log.info("echo test, 线程数量{}, 每个线程请求次数{}, 总耗时{}", runThreadCount, runTimeCount, sw.toString());

        assertThat(countList.size(), is(runThreadCount * runTimeCount));
        assertThat(countList.get(0), is(1));
        assertThat(countList.get(countList.size() - 1), is(runThreadCount * runTimeCount));

    }


    class EchoRunnable implements Runnable {

        private int i;

        EchoRunnable(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            ClientSession clientSession = null;

            try {
                clientSession = initSession();

                for (int j = 0; j < runTimeCount; j++) {

                    String username = RandomStringUtils.randomAlphabetic(10);
                    String password = RandomStringUtils.randomAlphabetic(10);

                    register(clientSession, username, password);

                    ClientResponseWrapper clientResponseWrapper = clientSession.login(username, password);
                    checkLoginResp(clientSession, username, password, clientResponseWrapper);
                    String msg = "你好啊123" + String.valueOf(i) + String.valueOf(j);
                    clientResponseWrapper = sendPlayerEcho(clientSession, msg);
                    PlayerEchoProtos.PlayerEchoResp playerEchoResp = (PlayerEchoProtos.PlayerEchoResp) clientResponseWrapper.getMessage();

                    assertThat(playerEchoResp.getMessage(), is(msg));
                    assertThat(playerEchoResp.getPlayerId(), is(clientSession.getPlayerId()));
                    assertThat(playerEchoResp.getUsername(), is(clientSession.getPlayer().getProperty().getUsername()));

                    countList.add(playerEchoResp.getCount());

                }

            } finally {
                if(clientSession != null) {
                    clientSession.destroy();
                }
            }

        }
    }

}
