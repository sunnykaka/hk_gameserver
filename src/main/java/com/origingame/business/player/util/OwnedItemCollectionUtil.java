package com.origingame.business.player.util;

import com.origingame.model.PlayerItemProtos;
import com.origingame.server.dao.DbMediator;
import com.origingame.server.util.IdGenerator;

/**
 * User: Liub
 * Date: 2014/11/26
 */
public class OwnedItemCollectionUtil {

    public static PlayerItemProtos.PlayerItem.Builder createPlayerItem(DbMediator dbMediator) {
        int id = IdGenerator.nextId(dbMediator, PlayerItemProtos.PlayerItem.class);
        PlayerItemProtos.PlayerItem.Builder playerItem = PlayerItemProtos.PlayerItem.newBuilder();
        playerItem.setId(id);
        return playerItem;
    }

}
