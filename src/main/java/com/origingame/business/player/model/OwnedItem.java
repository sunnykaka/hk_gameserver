package com.origingame.business.player.model;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.origingame.business.player.dao.PlayerDao;
import com.origingame.exception.GameDaoException;
import com.origingame.server.dao.DbMediator;
import com.origingame.server.main.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Liub
 * Date: 2014/11/25
 */
public class OwnedItem<B extends Message.Builder> {

    private static final Logger log = LoggerFactory.getLogger(OwnedItem.class);

    private PlayerDao playerDao = World.getBean(PlayerDao.class);

    private DbMediator dbMediator;

    private boolean initialized = false;

    private byte[] targetBytes;

    private B target;

    private boolean updated;

    private String key;

    private int id;

//    private Player player;

    public OwnedItem(Player player, String key, B target) {
//        this.player = player;
        this.dbMediator = player.getDbMediator();
        this.id = player.getId();
        this.key = key;
        this.target = target;

        if(player.isCreated()) {
            //新创建的对象,不从数据库加载
            initialized = true;
        }
    }


    public B get() {
        load();

        return target;
    }

    public int getId() {
        return id;
    }

    public byte[] load() {
        if(initialized) return targetBytes;

        this.targetBytes = playerDao.loadField(dbMediator, id, key);
        if(targetBytes != null) {
            try {
                target.mergeFrom(targetBytes);
            } catch (InvalidProtocolBufferException e) {
                log.error("", e);
                new GameDaoException(e);
            }
        }
        initialized = true;
        return targetBytes;
    }

    public void markUpdated() {
        updated = true;
    }

    public boolean isUpdated() {
        return updated;
    }

    public String getKey() {
        return key;
    }

    public byte[] serializeToBytes() {
        Preconditions.checkArgument(isInitialized());
        targetBytes = target.build().toByteArray();
        return targetBytes;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
