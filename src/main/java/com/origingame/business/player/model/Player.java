package com.origingame.business.player.model;

import com.origingame.model.PlayerItemProtos;
import com.origingame.model.PlayerModelProtos;
import com.origingame.model.PlayerPropertyProtos;
import com.origingame.persist.PlayerItemCollectionProtos;

/**
 * User: Liub
 * Date: 2014/11/25
 */
public class Player {

    private int id;

    private boolean created = false;

    private OwnedItem<PlayerPropertyProtos.PlayerProperty.Builder> property;

    private OwnedItemCollection<PlayerItemProtos.PlayerItem, PlayerItemProtos.PlayerItem.Builder, PlayerItemCollectionProtos.PlayerItemCollection.Builder> itemCol;

    @SuppressWarnings("unchecked")
    public Player(int id, boolean created) {
        this.id = id;
        this.created = created;

        property = new OwnedItem<>(this, "property", PlayerPropertyProtos.PlayerProperty.newBuilder());

        itemCol = new OwnedItemCollection<>(this, "itemCol", PlayerItemCollectionProtos.PlayerItemCollection.newBuilder())
                .initParameterizedType(PlayerItemProtos.PlayerItem.Builder.class, PlayerItemCollectionProtos.PlayerItemCollection.Builder.class);

    }

    public OwnedItem< PlayerPropertyProtos.PlayerProperty.Builder> getProperty() {
        return property;
    }

    public OwnedItemCollection<PlayerItemProtos.PlayerItem, PlayerItemProtos.PlayerItem.Builder,
            PlayerItemCollectionProtos.PlayerItemCollection.Builder> getItemCol() {
        return itemCol;
    }

    public int getId() {
        return id;
    }

    public boolean isCreated() {
        return created;
    }

    public PlayerModelProtos.PlayerModel toModel() {
        PlayerModelProtos.PlayerModel.Builder playerModel = PlayerModelProtos.PlayerModel.newBuilder();
        playerModel.setId(getId());
        playerModel.setProperty(getProperty().get());
        if(itemCol.isInitialized() && !itemCol.isEmpty()) {
            playerModel.addAllItems(itemCol.getItems());
        }
        return playerModel.build();
    }

}
