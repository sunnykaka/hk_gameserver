package com.origingame.server.main;

import com.origingame.config.GlobalConfig;
import com.origingame.server.action.ActionResolver;
import com.origingame.server.dao.ServerPersistenceResolver;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class World {

    private static World INSTANCE = new World();
    private World() {}
    public static World getInstance() {
        return INSTANCE;
    }

    private AtomicBoolean initialized = new AtomicBoolean(false);

    public void init() {
        if(!initialized.compareAndSet(false, true)) return;

        //初始化Action信息
        ActionResolver.getInstance().init(GlobalConfig.ACTION_BASE_PACKAGE);
        //初始化数据库连接
        ServerPersistenceResolver.getInstance().init(GlobalConfig.SERVER_PERSISTENCE_FILE_PATH);

    }

    public void destroy() {
        ServerPersistenceResolver.getInstance().destroy();
    }

    public static Date now() {
        return new Date();
    }






}
