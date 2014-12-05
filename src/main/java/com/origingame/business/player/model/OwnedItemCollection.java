package com.origingame.business.player.model;

import com.google.common.base.Preconditions;
import com.google.protobuf.Message;
import com.origingame.business.player.dao.PlayerDao;
import com.origingame.exception.GameDaoException;
import com.origingame.server.main.World;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Liub
 * Date: 2014/11/25
 */
public class OwnedItemCollection<T extends Message, B extends Message.Builder, CB extends Message.Builder> {

    private static final Logger log = LoggerFactory.getLogger(OwnedItemCollection.class);

    private PlayerDao playerDao = World.getBean(PlayerDao.class);

    private boolean initialized = false;

    private byte[] targetBytes;

    private CB target;

    private List<T> items;

    private Map<Integer, B> itemMap = new HashMap<>();

    private Class<CB> collectionBuilderClass;

    private Class<B> builderClass;

    private boolean updated;

    private String key;

    private int id;

    public OwnedItemCollection(Player player, String key, CB target) {
        this.id = player.getId();
        this.key = key;
        this.target = target;

        if(player.isCreated()) {
            //新创建的对象,不从数据库加载
            initialized = true;
        }

//        System.out.println(getClass().getGenericSuperclass());
//        Type[] actualTypeArguments = ((ParameterizedType)(getClass().getGenericSuperclass())).getActualTypeArguments();
    }

    @SuppressWarnings("unchecked")
    public <TB extends Message.Builder, TCB extends Message.Builder>  OwnedItemCollection initParameterizedType(Class<TB> builderClass, Class<TCB> collectionBuilderClass) {
        this.builderClass = (Class<B>)builderClass;
        this.collectionBuilderClass = (Class<CB>)collectionBuilderClass;

        return this;
    }

    public B get(int id) {
        if(id <= 0) return null;
        load();

        return itemMap.get(id);
    }

    public boolean addItem(int id, B builder) {
        if(itemMap.containsKey(id)) return false;
        itemMap.put(id, builder);
        markUpdated();
        return true;
    }

    public boolean deleteItem(int id) {
        Object o = itemMap.remove(id);
        if(o != null) {
            markUpdated();
            return true;
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    public byte[] load() {
        if(initialized) return targetBytes;

        targetBytes = playerDao.loadField(id, key);
        if(targetBytes != null) {
            try {
                target.mergeFrom(targetBytes);

                items = (List<T>)MethodUtils.getAccessibleMethod(collectionBuilderClass, "getItemsList", null).invoke(target);
                for(T item : items) {
                    B builder = (B)item.toBuilder();
                    itemMap.put((Integer)(MethodUtils.getAccessibleMethod(builderClass, "getId", null).invoke(builder)), builder);
                }

            } catch (Exception e) {
                log.error("playerId[{}], key[{}]在加载数据的时候发生错误", id, key);
                throw new GameDaoException(e);
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

    //TODO 优化点:如果只对集合中少数几个数据修改的话,可以直接修改源pb集合,提升效率
    public byte[] serializeToBytes() {
        Preconditions.checkArgument(isInitialized());

        if(updated) {
            try {
                MethodUtils.getAccessibleMethod(collectionBuilderClass, "clearItems", null).invoke(target);
                items = new ArrayList<>(itemMap.size());
                if(!itemMap.isEmpty()) {
                    for(B builder : itemMap.values()) {
                        items.add((T)builder.build());
                    }
                    MethodUtils.getAccessibleMethod(collectionBuilderClass, "addAllItems", Iterable.class).invoke(target, items);
                }
            } catch (Exception e) {
                log.error("playerId[{}], key[{}]在序列化数据的时候发生错误", id, key);
                throw new GameDaoException(e);
            }
        }

        targetBytes = target.build().toByteArray();
        return targetBytes;
    }

    public Map<Integer, B> getItemMap() {
        load();

        return itemMap;
    }

    public List<T> getItems() {
        load();

        return items;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public byte[] getTargetBytes() {
        load();

        return targetBytes;
    }

    public boolean isEmpty() {
        load();

        return itemMap.isEmpty();
    }


    public String getKey() {
        return key;
    }
}
