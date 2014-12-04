package com.origingame.business.player.dao;

import com.origingame.business.player.mock.CenterPlayer;
import com.origingame.business.player.model.Player;
import com.origingame.exception.GameBusinessException;
import com.origingame.message.BaseMsgProtos;
import com.origingame.model.PlayerModelProtos;
import com.origingame.model.PlayerPropertyProtos;
import com.origingame.server.context.GameContext;
import com.origingame.server.dao.DbMediator;
import com.origingame.server.util.IdGenerator;
import com.origingame.server.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
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

    public Player createByCenterPlayer(DbMediator dbMediator, CenterPlayer centerPlayer) {

        Player player = create(dbMediator, centerPlayer.getCenterPlayerId(), centerPlayer.getUsername(), centerPlayer.getPassword());

        String outerId = centerPlayer.getCenterPlayerId();
        String outerIdIndexKey = RedisUtil.buildKey("i", Player.class.getSimpleName(), "outerId");
        Jedis jedis = dbMediator.selectCenterDb().getJedis();
        jedis.hset(outerIdIndexKey, outerId, String.valueOf(player.getId()));

        return player;
    }


    public void save(DbMediator dbMediator, Player player) {

        if(player.getProperty().isUpdated()) {
            saveField(dbMediator, player.getId(), player.getProperty().getKey(), player.getProperty().serializeToBytes());
        }

        if(player.getItemCol().isUpdated()) {
            saveField(dbMediator, player.getId(), player.getItemCol().getKey(), player.getItemCol().serializeToBytes());
        }
    }

    public Player create(DbMediator dbMediator, String outerId, String username, String password) {
        int id = IdGenerator.nextId(dbMediator, PlayerModelProtos.PlayerModel.class);
        Player player = new Player(dbMediator, id, true);

        PlayerPropertyProtos.PlayerProperty.Builder property = player.getProperty().get();
        property.setId(id);
        property.setUsername(username);
        property.setPassword(password);
        property.setOuterId(outerId);

        player.getProperty().markUpdated();
        return player;
    }

    public Player load(DbMediator dbMediator, int id) {
        Player player = new Player(dbMediator, id, false);
        if(player.getProperty().load() == null) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.NO_ENTITY_FOR_ID, String.format("玩家不存在, id[%d]", id));
        }
        return player;
    }

    public Player getByOuterId(DbMediator dbMediator, String outerId) {
        if(StringUtils.isBlank(outerId)) {
            return null;
        }
        String outerIdIndexKey = RedisUtil.buildKey("i", Player.class.getSimpleName(), "outerId");
        Jedis jedis = dbMediator.selectCenterDb().getJedis();
        String playerId = jedis.hget(outerIdIndexKey, outerId);
        return load(dbMediator, Integer.parseInt(playerId));
    }
}
