package com.origingame.server.main;

import com.origingame.config.GlobalConfig;
import com.origingame.server.action.ActionResolver;
import com.origingame.server.dao.ServerPersistenceResolver;
import com.origingame.server.registry.DescriptorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
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

    public void init() throws Exception {
        if(!initialized.compareAndSet(false, true)) return;

        try {
            //初始化Action信息
            ActionResolver.getInstance().init(GlobalConfig.ACTION_BASE_PACKAGE);
            //初始化数据库连接
            ServerPersistenceResolver.getInstance().init(GlobalConfig.SERVER_PERSISTENCE_FILE_PATH);
            //注册protobuf生成类
            DescriptorRegistry.getInstance().init(GlobalConfig.PROTO_BUF_MESSAGE_BASE_PACKAGE);
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






}
