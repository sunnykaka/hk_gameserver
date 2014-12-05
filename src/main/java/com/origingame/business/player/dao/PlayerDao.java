package com.origingame.business.player.dao;

import com.origingame.business.player.mock.CenterPlayer;
import com.origingame.business.player.model.Player;
import com.origingame.exception.GameBusinessException;
import com.origingame.message.BaseMsgProtos;
import com.origingame.model.PlayerModelProtos;
import com.origingame.model.PlayerPropertyProtos;
import com.origingame.server.context.GameContextHolder;
import com.origingame.server.util.IdGenerator;
import com.origingame.server.util.RedisUtil;
import com.origingame.server.util.SimpleRedisAccess;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import static com.origingame.server.util.RedisUtil.buildSingleByteKey;
/**
 * User: Liub
 * Date: 2014/11/25
 */
public class PlayerDao {

    public byte[] loadField(int playerId, String fieldName) {

        Jedis jedis = GameContextHolder.getDbMediator().selectShardDb(playerId).getJedis();
        byte[] bytes = jedis.hget(buildPlayerKey(playerId), buildSingleByteKey(fieldName));
        return bytes;
    }

    public byte[] saveField(int playerId, String fieldName, byte[] bytes) {

        Jedis jedis = GameContextHolder.getDbMediator().selectShardDb(playerId).getJedis();
        jedis.hset(buildPlayerKey(playerId), buildSingleByteKey(fieldName), bytes);
        return bytes;
    }


    private byte[] buildPlayerKey(int playerId) {

        return RedisUtil.buildByteKey("p", String.valueOf(playerId));
    }

    public Player createByCenterPlayer(CenterPlayer centerPlayer) {

        Player player = create(centerPlayer.getCenterPlayerId(), centerPlayer.getUsername(), centerPlayer.getPassword());

        String outerId = centerPlayer.getCenterPlayerId();
        SimpleRedisAccess.createIndex(Player.class, "outerId", outerId, String.valueOf(player.getId()));

        return player;
    }


    public void save(Player player) {

        if(player.getProperty().isUpdated()) {
            saveField(player.getId(), player.getProperty().getKey(), player.getProperty().serializeToBytes());
        }

        if(player.getItemCol().isUpdated()) {
            saveField(player.getId(), player.getItemCol().getKey(), player.getItemCol().serializeToBytes());
        }
    }

    public Player create(String outerId, String username, String password) {
        int id = IdGenerator.nextId(GameContextHolder.getDbMediator(), PlayerModelProtos.PlayerModel.class);
        Player player = new Player(id, true);

        PlayerPropertyProtos.PlayerProperty.Builder property = player.getProperty().get();
        property.setId(id);
        property.setUsername(username);
        property.setPassword(password);
        property.setOuterId(outerId);

        player.getProperty().markUpdated();
        return player;
    }

    public Player load(int id) {
        Player player = new Player(id, false);
        if(player.getProperty().load() == null) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.NO_ENTITY_FOR_ID, String.format("玩家不存在, id[%d]", id));
        }
        return player;
    }

    public Player getByOuterId(String outerId) {
        if(StringUtils.isBlank(outerId)) {
            return null;
        }
        String playerId = SimpleRedisAccess.getIndexValue(Player.class, "outerId", outerId);
        if(StringUtils.isBlank(playerId)) {
            //TODO 需要去账户中心拿到玩家信息
        }
        return load(Integer.parseInt(playerId));
    }
}
