package com.origingame.business.player.mock;

import com.origingame.exception.GameBusinessException;
import com.origingame.message.BaseMsgProtos;
import com.origingame.server.util.IdGenerator;
import com.origingame.server.util.RedisUtil;
import com.origingame.server.util.SimpleRedisAccess;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CenterPlayer {
        
    private String centerPlayerId;

    private String username;

    private String password;


    public String getCenterPlayerId() {
        return centerPlayerId;
    }

    public void setCenterPlayerId(String centerPlayerId) {
        this.centerPlayerId = centerPlayerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static CenterPlayer create(Jedis jedis, String username, String password, boolean trail) {
        int centerPlayerId = IdGenerator.nextId(jedis, CenterPlayer.class);
        if(trail) {
            username = "U" + centerPlayerId;
            password = RandomStringUtils.randomAlphanumeric(6);
        } else {
            String usernameIndexValue = SimpleRedisAccess.getIndexValue(CenterPlayer.class, "username", username);
            boolean usernameExist = !StringUtils.isBlank(usernameIndexValue);
            if(usernameExist) {
                //用户名重复
                throw new GameBusinessException(BaseMsgProtos.ResponseStatus.USERNAME_EXIST);
            }
        }

        CenterPlayer centerPlayer = new CenterPlayer();
        centerPlayer.setCenterPlayerId(String.valueOf(centerPlayerId));
        centerPlayer.setUsername(username);
        centerPlayer.setPassword(password);

        centerPlayer.create(jedis);
        return centerPlayer;
    }

    private boolean create(Jedis jedis) {
        //key为username,value为centerPlayerId的index
//        String usernameIndexKey = RedisUtil.buildKey("i", CenterPlayer.class.getSimpleName(), "username");
//        if(!Long.valueOf(1).equals(jedis.hsetnx(usernameIndexKey, username, centerPlayerId))) {
//            //创建失败,用户名已被占用
//            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.USERNAME_EXIST);
//        }

        if(!SimpleRedisAccess.createIndexNX(CenterPlayer.class, "username", username, centerPlayerId)) {
            //创建失败,用户名已被占用
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.USERNAME_EXIST);
        }


        String centerPlayerKey = RedisUtil.buildKey(CenterPlayer.class.getSimpleName(), centerPlayerId);
        Map<String, String> map = new HashMap<>();
        map.put("centerPlayerId", centerPlayerId);
        map.put("username", username);
        map.put("password", password);
        RedisUtil.checkHmsetResponse(jedis.hmset(centerPlayerKey, map));

        return true;
    }

    public static CenterPlayer load(Jedis jedis, String username, String password) {

        String centerPlayerId = SimpleRedisAccess.getIndexValue(CenterPlayer.class, "username", username);
        if(StringUtils.isBlank(centerPlayerId)) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.USERNAME_OR_PASSWORD_INCORRECT);
        }

        String centerPlayerKey = RedisUtil.buildKey(CenterPlayer.class.getSimpleName(), centerPlayerId);
        List<String> centerPlayerProps = jedis.hmget(centerPlayerKey, "centerPlayerId", "username", "password");
        if(centerPlayerProps == null || centerPlayerProps.isEmpty()) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.NO_ENTITY_FOR_ID, "id:" + centerPlayerId);
        }

        CenterPlayer centerPlayer = new CenterPlayer();
        centerPlayer.setCenterPlayerId(centerPlayerId);
        centerPlayer.setUsername(centerPlayerProps.get(1));
        centerPlayer.setPassword(centerPlayerProps.get(2));

        if(!centerPlayer.getPassword().equals(password)) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.USERNAME_OR_PASSWORD_INCORRECT);
        }

        return centerPlayer;
    }
}
