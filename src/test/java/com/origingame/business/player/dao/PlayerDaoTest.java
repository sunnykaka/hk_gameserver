package com.origingame.business.player.dao;

import com.google.protobuf.Message;
import com.origingame.BaseNettyTest;
import com.origingame.business.player.model.OwnedItem;
import com.origingame.business.player.model.OwnedItemCollection;
import com.origingame.business.player.model.Player;
import com.origingame.business.player.util.OwnedItemCollectionUtil;
import com.origingame.client.main.ClientSession;
import com.origingame.client.protocol.ClientResponseWrapper;
import com.origingame.message.EchoProtos;
import com.origingame.model.PlayerItemProtos;
import com.origingame.model.PlayerPropertyProtos;
import com.origingame.persist.PlayerItemCollectionProtos;
import com.origingame.server.dao.DbMediator;
import com.origingame.server.main.World;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;


/**
 *
 * 运行之前要先启动服务器
 * User: Liub
 * Date: 2014/11/21
 */
public class PlayerDaoTest extends BaseNettyTest {

    private final Logger log = LoggerFactory.getLogger(PlayerDaoTest.class);

    private PlayerDao playerDao = World.getBean(PlayerDao.class);

    @Test
    public void testOwnedItem() throws Exception {

        DbMediator dbMediator = new DbMediator();
        Player player = playerDao.create(dbMediator, RandomStringUtils.randomAlphabetic(8));
        int playerId = player.getId();
        assertThat(player.getId(), greaterThan(0));
        OwnedItem<PlayerPropertyProtos.PlayerProperty.Builder> propertyOwnedItem = player.getProperty();
        PlayerPropertyProtos.PlayerProperty.Builder property = propertyOwnedItem.get();
        int gem = 10;
        int level = 20;
        String nickname = "张三haha";
        property.setGem(gem);
        property.setLevel(level);
        property.setNickname(nickname);
        propertyOwnedItem.markUpdated();
        PlayerPropertyProtos.PlayerProperty propertyMessage = property.build();
        playerDao.save(dbMediator, player);

        Player loadedPlayer = playerDao.load(dbMediator, playerId);
        PlayerPropertyProtos.PlayerProperty.Builder loadedProperty = loadedPlayer.getProperty().get();
        PlayerPropertyProtos.PlayerProperty loadedPropertyMessage = loadedProperty.build();

        assertThat(loadedPropertyMessage, is(propertyMessage));
        assertThat(loadedPropertyMessage.toByteArray(), is(propertyMessage.toByteArray()));
        assertThat(loadedProperty.getId(), is(playerId));
        assertThat(loadedProperty.getGem(), is(gem));
        assertThat(loadedProperty.getLevel(), is(level));
        assertThat(loadedProperty.getNickname(), is(nickname));
        assertThat(loadedPlayer.getId(), is(playerId));

    }


    @Test
    public void testNormalOwnedItemCollection() throws Exception {

        DbMediator dbMediator = new DbMediator();
        Player player = playerDao.create(dbMediator, RandomStringUtils.randomAlphabetic(8));
        int playerId = player.getId();
        assertThat(player.getId(), greaterThan(0));

        OwnedItemCollection<PlayerItemProtos.PlayerItem, PlayerItemProtos.PlayerItem.Builder, PlayerItemCollectionProtos.PlayerItemCollection.Builder> itemCol = player.getItemCol();

        //正常保存2个item测试
        String itemId1 = "item1-11";
        int number1 = 5;
        String itemId2 = "item2-22";
        int number2 = 144;
        String itemId3 = "item3-33";
        int number3 = 278;

        PlayerItemProtos.PlayerItem.Builder playerItem1 = OwnedItemCollectionUtil.createPlayerItem(dbMediator);
        playerItem1.setItemId(itemId1).setNumber(number1);

        PlayerItemProtos.PlayerItem.Builder playerItem2 = OwnedItemCollectionUtil.createPlayerItem(dbMediator);
        playerItem2.setItemId(itemId2).setNumber(number2);

        PlayerItemProtos.PlayerItem.Builder playerItem3 = OwnedItemCollectionUtil.createPlayerItem(dbMediator);
        playerItem3.setItemId(itemId3).setNumber(number3);

        itemCol.addItem(playerItem1.getId(), playerItem1);
        itemCol.addItem(playerItem2.getId(), playerItem2);

        playerDao.save(dbMediator, player);


        Player loadedPlayer = playerDao.load(dbMediator, playerId);
        OwnedItemCollection<PlayerItemProtos.PlayerItem, PlayerItemProtos.PlayerItem.Builder, PlayerItemCollectionProtos.PlayerItemCollection.Builder> loadedItemCol = loadedPlayer.getItemCol();

        assertPlayerItemEquals(player, loadedPlayer);


        //删除第一个item,修改第二个item,再添加一个item测试
        loadedItemCol.deleteItem(playerItem1.getId());
        PlayerItemProtos.PlayerItem.Builder loadedPlayerItem2 = itemCol.get(playerItem2.getId());
        String newItemId2 = "item231123";
        loadedPlayerItem2.setItemId(newItemId2);

        loadedItemCol.addItem(playerItem3.getId(), playerItem3);

        playerDao.save(dbMediator, loadedPlayer);

        player = loadedPlayer;
        loadedPlayer = playerDao.load(dbMediator, playerId);

        assertPlayerItemEquals(player, loadedPlayer);
        assertThat(loadedPlayer.getItemCol().getItemMap().size(), is(2));

    }


    @Test
    public void testEmptyOwnedItemCollection() throws Exception {

        DbMediator dbMediator = new DbMediator();
        Player player = playerDao.create(dbMediator, RandomStringUtils.randomAlphabetic(8));
        int playerId = player.getId();
        assertThat(player.getId(), greaterThan(0));

        //playerItem一开始是空的
        playerDao.save(dbMediator, player);

        Player loadedPlayer = playerDao.load(dbMediator, playerId);
        OwnedItemCollection<PlayerItemProtos.PlayerItem, PlayerItemProtos.PlayerItem.Builder, PlayerItemCollectionProtos.PlayerItemCollection.Builder> loadedItemCol = loadedPlayer.getItemCol();
        assertPlayerItemEquals(player, loadedPlayer);
        assertThat(loadedPlayer.getItemCol().getItemMap().size(), is(0));

        //添加一个playerItem
        PlayerItemProtos.PlayerItem.Builder playerItem1 = OwnedItemCollectionUtil.createPlayerItem(dbMediator);
        playerItem1.setItemId("item-12312dd").setNumber(89);
        loadedItemCol.addItem(playerItem1.getId(), playerItem1);
        playerDao.save(dbMediator, loadedPlayer);

        player = loadedPlayer;
        loadedPlayer = playerDao.load(dbMediator, playerId);
        loadedItemCol = loadedPlayer.getItemCol();
        assertPlayerItemEquals(player, loadedPlayer);
        assertThat(loadedPlayer.getItemCol().getItemMap().size(), is(1));

        //删除一个playerItem,使OwnedItemCollection为空
        loadedItemCol.deleteItem(playerItem1.getId());
        playerDao.save(dbMediator, loadedPlayer);

        player = loadedPlayer;
        loadedPlayer = playerDao.load(dbMediator, playerId);
        loadedItemCol = loadedPlayer.getItemCol();
        assertPlayerItemEquals(player, loadedPlayer);
        assertThat(loadedPlayer.getItemCol().getItemMap().size(), is(0));

        //又加一个playerItem
        PlayerItemProtos.PlayerItem.Builder playerItem2 = OwnedItemCollectionUtil.createPlayerItem(dbMediator);
        playerItem2.setItemId("item-122ffsdd").setNumber(998);
        loadedItemCol.addItem(playerItem2.getId(), playerItem2);
        playerDao.save(dbMediator, loadedPlayer);

        player = loadedPlayer;
        loadedPlayer = playerDao.load(dbMediator, playerId);
        assertPlayerItemEquals(player, loadedPlayer);
        assertThat(loadedPlayer.getItemCol().getItemMap().size(), is(1));
    }


    private void assertPlayerItemEquals(Player before, Player after) {

        OwnedItemCollection<PlayerItemProtos.PlayerItem, PlayerItemProtos.PlayerItem.Builder, PlayerItemCollectionProtos.PlayerItemCollection.Builder> beforeItemCol = before.getItemCol();
        OwnedItemCollection<PlayerItemProtos.PlayerItem, PlayerItemProtos.PlayerItem.Builder, PlayerItemCollectionProtos.PlayerItemCollection.Builder> afterItemCol = after.getItemCol();

        assertThat(afterItemCol.getTargetBytes(), is(beforeItemCol.getTargetBytes()));
//        assertThat(loadedItemCol.getItemMap().entrySet(), everyItem(isIn(itemCol.getItemMap().entrySet())));
        assertThat(afterItemCol.getItemMap().size(), is(beforeItemCol.getItemMap().size()));
        for(Map.Entry<Integer, PlayerItemProtos.PlayerItem.Builder> entry : beforeItemCol.getItemMap().entrySet()) {
            Integer key = entry.getKey();
            PlayerItemProtos.PlayerItem.Builder value = entry.getValue();
            assertThat(afterItemCol.get(key).build(), is(value.build()));
        }

        assertThat(after.toModel(), is(before.toModel()));


//        assertThat(loadedPropertyMessage, is(propertyMessage));
//        assertThat(loadedPropertyMessage.toByteArray(), is(propertyMessage.toByteArray()));
//        assertThat(loadedProperty.getId(), is(playerId));
//        assertThat(loadedProperty.getGem(), is(gem));
//        assertThat(loadedProperty.getLevel(), is(level));
//        assertThat(loadedProperty.getNickname(), is(nickname));
//        assertThat(loadedPlayer.getId(), is(playerId));


    }



}
