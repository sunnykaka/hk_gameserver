package com.origingame.server.lock;

import com.origingame.config.GlobalConfig;
import com.origingame.server.util.RedisUtil;
import com.origingame.util.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * User: Liub
 * Date: 2014/11/18
 */
public class RedisLock {

    private static final Logger log = LoggerFactory.getLogger(RedisLock.class);

    private Jedis jedis;

//    private String id;
//
//    private String key;

    private String redisKey;

    private boolean lockSuccess;

    private long startLockTime;

    private static final long estimateLatencyMilliseconds = 50;

    private RedisLock() {}

    public static RedisLock newLock(Jedis jedis, String key, String id) {
        RedisLock lock = new RedisLock();
        lock.jedis = jedis;
//        lock.id = id;
//        lock.key = key;
        lock.redisKey = RedisUtil.buildKey("lock", key, id);
        return lock;
    }

    public boolean lock() {

        long timeToWait = GlobalConfig.LOCK_FIRST_WAIT_TIME;
        int remainSpinCount = GlobalConfig.LOCK_SPIN_MAX_COUNT;
        String resp = jedis.set(redisKey, "1", "nx", "ex", GlobalConfig.LOCK_EXPIRE_TIME_IN_SECONDS);

        while(checkRespIfSuccess(resp) && remainSpinCount > 0) {
            long remainTime = timeToWait;
            do {
                long before = World.now().getTime();
                try {
                    Thread.sleep(remainTime);
                } catch (InterruptedException e) {
                    log.error("自旋的时候被打断", e);
                }
                remainTime = remainTime + before - World.now().getTime();
            } while(remainTime > estimateLatencyMilliseconds);
            resp = jedis.set(redisKey, "1", "nx", "ex", GlobalConfig.LOCK_EXPIRE_TIME_IN_SECONDS);
            timeToWait = timeToWait << 1;
            remainSpinCount --;
        }

        lockSuccess = checkRespIfSuccess(resp);
        if(lockSuccess) {
            startLockTime = World.now().getTime();
        }
        return lockSuccess;
    }

    public void release() {
        if(lockSuccess && World.now().getTime() + estimateLatencyMilliseconds < startLockTime + GlobalConfig.LOCK_EXPIRE_TIME_IN_SECONDS * 1000) {
            jedis.del(redisKey);
        }
    }

    private boolean checkRespIfSuccess(String resp) {
        return "OK".equals(resp);
    }

}
