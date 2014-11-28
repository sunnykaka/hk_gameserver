package com.origingame.server.main;

import com.origingame.config.GlobalConfig;
import com.origingame.item.resolver.ItemSpecResolver;
import com.origingame.server.action.ActionResolver;
import com.origingame.server.dao.ServerPersistenceResolver;
import com.origingame.server.registry.DescriptorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class World {

    private static final Logger log = LoggerFactory.getLogger(World.class);

    private static World INSTANCE = new World();
    private World() {}
    public static World getInstance() {
        return INSTANCE;
    }

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private static ConcurrentMap<String,Object> beans = new ConcurrentHashMap<>();

    public void init() throws Exception {
        if(!initialized.compareAndSet(false, true)) return;

        try {
            //初始化Action信息
            ActionResolver.getInstance().init(GlobalConfig.ACTION_BASE_PACKAGE);
            //初始化数据库连接
            ServerPersistenceResolver.getInstance().init(GlobalConfig.SERVER_PERSISTENCE_FILE_PATH);
            //注册protobuf生成类
            DescriptorRegistry.getInstance().init(GlobalConfig.PROTO_BUF_MESSAGE_BASE_PACKAGE);
            //读取item定义文件
            ItemSpecResolver.getInstance().init(GlobalConfig.ITEM_SPEC_FILE_PATH);


        } catch (Exception e) {
            log.error("初始化失败", e);
            throw e;
        }

    }

    public void destroy() {
        ServerPersistenceResolver.getInstance().destroy();
    }

    public static Date now() {
        return new Date();
    }

    /**
     * 通过这个方法维护的单例对象不能在构造函数内进行初始化的操作.
     * 因为有可能会被初始化多次
     * @param cls
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> cls) {
        String name = cls.getName();
        Object obj = beans.get(name);
        if(obj != null) {
            return (T)obj;
        }
        try {
            obj = cls.newInstance();
            Object newObj = beans.putIfAbsent(name, obj);
            obj = newObj == null ? obj : newObj;
        } catch (InstantiationException e) {
            log.error("", e);
        } catch (IllegalAccessException e) {
            log.error("", e);
        }
        return (T)obj;
    }





}
