package com.origingame.server.dao;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.origingame.server.dao.jaxb.ServerPersistence;
import com.origingame.server.exception.GameException;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Liub
 * Date: 2014/11/19
 */
public class ServerPersistenceResolver {

    private static final Logger log = LoggerFactory.getLogger(ServerPersistenceResolver.class);

    private static ServerPersistenceResolver INSTANCE = new ServerPersistenceResolver();
    private ServerPersistenceResolver() {}
    public static ServerPersistenceResolver getInstance() {
        return INSTANCE;
    }

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private Map<String, JedisPool> realDbMap = new HashMap<>();

    private JedisPool centerDbPool;

    private List<JedisPool> playerShardPool = new ArrayList<>();

    private int playerRoundStep;

    private int playerShardSize;

    private int playerDbSize;

    public void init() {
        if (!initialized.compareAndSet(false, true)) return;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ServerPersistence.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            ServerPersistence serverPersistence = (ServerPersistence) jaxbUnmarshaller.unmarshal(Thread.currentThread().getContextClassLoader().getResourceAsStream("server-persistence.xml"));

            ServerPersistence.DbList dbList = serverPersistence.getDbList();
            for(ServerPersistence.DbList.Db db : dbList.getDb()) {
                JedisPool jedisPool = new JedisPool(new GenericObjectPool.Config(), db.getIp(), db.getPort(), 2000, db.getPassword(), db.getDb());
                realDbMap.put(db.getName(), jedisPool);
            }

            ServerPersistence.CenterDb centerDb = serverPersistence.getCenterDb();
            centerDbPool = realDbMap.get(centerDb.getRef());
            Preconditions.checkNotNull(centerDbPool);

            ServerPersistence.PlayerDbList playerDbList = serverPersistence.getPlayerDbList();
            playerDbSize = playerDbList.getPlayerDb().size();
            playerRoundStep = playerDbList.getRoundStep();
            playerShardSize = playerDbList.getShardSize();
            if(playerDbSize % 2 != 0) {
                throw new GameException("初始化失败,playerDbList数量必须是2的x次方");
            }

            //举个栗子.playerDbSize配置的是32,而playerDb只配两个,其实是每个playerDb要承担16个虚拟节点的工作.(有可能这两个playerDb还只对应一个物理节点,my god..)
            int playerDbMapCount = playerShardSize / playerDbSize;
            for(ServerPersistence.PlayerDbList.PlayerDb playerDb : playerDbList.getPlayerDb()) {
                JedisPool jedisPool = realDbMap.get(playerDb.getRef());
                for (int j = 0; j < playerDbMapCount; j++) {
                    playerShardPool.add(jedisPool);
                }
            }
            if(playerShardPool.size() != playerShardSize) {
                throw new GameException(String.format("初始化失败,playerShardPool数量[%d]不等于playerShardSize[%d]", playerShardPool.size(), playerShardSize));
            }

        } catch (Exception e) {
            log.error("初始化失败", e);
            throw new GameException(e);
        }

    }

    public PlayerDb selectPlayerDb(int playerId) {

        int index = findPlayerDbIndexById(playerId);
        JedisPool jedisPool = playerShardPool.get(index);

        return new PlayerDb(jedisPool, index);

    }

    public int findPlayerDbIndexById(int id) {
        return id / playerRoundStep % playerShardSize;
    }

    public CenterDb selectCenterDb() {

        return new CenterDb(centerDbPool);
    }

    public void destroy() {
        if(centerDbPool != null) {
            centerDbPool.destroy();
        }
        for(JedisPool jedisPool : realDbMap.values()) {
            jedisPool.destroy();
        }
    }
}
