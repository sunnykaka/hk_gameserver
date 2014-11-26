package com.origingame.business.player.dao;

import com.origingame.business.player.model.Player;
import com.origingame.exception.GameBusinessException;
import com.origingame.message.BaseMsgProtos;
import com.origingame.model.PlayerModelProtos;
import com.origingame.model.PlayerPropertyProtos;
import com.origingame.server.dao.DbMediator;
import com.origingame.server.util.IdGenerator;
import com.origingame.server.util.RedisUtil;
import redis.clients.jedis.Jedis;


import static com.origingame.server.util.RedisUtil.*;
/**
 * User: Liub
 * Date: 2014/11/25
 */
public class PlayerDao {

    public byte[] loadField(DbMediator dbMediator, int playerId, String fieldName) {

        Jedis jedis = dbMediator.selectPlayerDb(playerId).getJedis();
        byte[] bytes = jedis.hget(buildPlayerKey(playerId), buildSingleByteKey(fieldName));
        return bytes;
    }

    public byte[] saveField(DbMediator dbMediator, int playerId, String fieldName, byte[] bytes) {

        Jedis jedis = dbMediator.selectPlayerDb(playerId).getJedis();
        jedis.hset(buildPlayerKey(playerId), buildSingleByteKey(fieldName), bytes);
        return bytes;
    }


    private byte[] buildPlayerKey(int playerId) {

        return RedisUtil.buildByteKey("p", String.valueOf(playerId));
    }

    public void save(DbMediator dbMediator, Player player) {
//        Jedis jedis = dbMediator.selectPlayerDb(player.getId()).getJedis();
//        byte[] playerKey = buildPlayerKey(player.getId());

        if(player.getProperty().isUpdated()) {
            saveField(dbMediator, player.getId(), player.getProperty().getKey(), player.getProperty().serializeToBytes());
        }

        if(player.getItemCol().isUpdated()) {
            saveField(dbMediator, player.getId(), player.getItemCol().getKey(), player.getItemCol().serializeToBytes());
        }
    }

    public static Player create(DbMediator dbMediator, String uniqueId) {
        int id = IdGenerator.nextId(dbMediator, PlayerModelProtos.PlayerModel.class);
        Player player = new Player(dbMediator, id, true);

        PlayerPropertyProtos.PlayerProperty.Builder property = player.getProperty().get();
        property.setId(id);
        property.setUniqueId(uniqueId);

        player.getProperty().markUpdated();
        return player;
    }

    public static Player load(DbMediator dbMediator, int id) {
        Player player = new Player(dbMediator, id, false);
        if(player.getProperty().load() == null) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.NO_ENTITY_FOR_ID, String.format("不存在的类[Player], id[%d]", id));
        }
        return player;
    }

}
