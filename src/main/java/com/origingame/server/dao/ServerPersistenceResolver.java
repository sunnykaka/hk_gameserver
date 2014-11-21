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

    protected Map<String, JedisPool> realDbMap = new HashMap<>();

    protected JedisPool centerDbPool;

    protected List<JedisPool> playerShardPool = new ArrayList<>();

    protected int playerRoundStep;

    protected int playerShardSize;

    protected int playerDbSize;

    public void init(String configFilePath) {
        if (!initialized.compareAndSet(false, true)) return;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ServerPersistence.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            log.info("读取文件[{}]的dao层配置", configFilePath);
            ServerPersistence serverPersistence = (ServerPersistence) jaxbUnmarshaller.unmarshal(Thread.currentThread().getContextClassLoader().getResourceAsStream(configFilePath));

            ServerPersistence.DbList dbList = serverPersistence.getDbList();
            Preconditions.checkState(!dbList.getDb().isEmpty());
            log.info("实际物理db数量: {}", dbList.getDb().size());
            for(ServerPersistence.DbList.Db db : dbList.getDb()) {
                JedisPool jedisPool = new JedisPool(new GenericObjectPool.Config(), db.getIp(), db.getPort(), 2000, db.getPassword(), db.getDb());
                realDbMap.put(db.getName(), jedisPool);
            }

            ServerPersistence.CenterDb centerDb = serverPersistence.getCenterDb();
            centerDbPool = realDbMap.get(centerDb.getRef());
            Preconditions.checkNotNull(centerDbPool);
            log.info("centerDb name: {}", centerDb.getRef());

            ServerPersistence.PlayerDbList playerDbList = serverPersistence.getPlayerDbList();
            playerDbSize = playerDbList.getPlayerDb().size();
            playerRoundStep = playerDbList.getRoundStep();
            playerShardSize = playerDbList.getShardSize();
            if(playerDbSize % 2 != 0) {
                throw new GameException("初始化失败,playerDbList数量必须是2的x次方");
            }

            //举个栗子.playerDbSize配置的是32,而playerDb只配两个,其实是每个playerDb要承担16个虚拟节点的工作.(有可能这两个playerDb还只对应一个物理节点,my god..)
            int playerDbMapCount = playerShardSize / playerDbSize;
            log.info("playerDbSize[{}], playerRoundStep[{}], playerShardSize[{}], playerDbMapCount[{}]", playerDbSize, playerRoundStep, playerShardSize, playerDbMapCount);

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
        for(JedisPool jedisPool : realDbMap.values()) {
            jedisPool.destroy();
        }
    }
}
