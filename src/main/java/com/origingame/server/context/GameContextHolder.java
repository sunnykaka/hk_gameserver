package com.origingame.server.context;

import com.origingame.server.dao.DbMediator;

/**
 * User: Liub
 * Date: 2014/12/5
 */
public class GameContextHolder {

    private static final ThreadLocal<GameContext> HOLDER = new ThreadLocal<>();

    public static GameContext get() {
        return HOLDER.get();
    }

    public static DbMediator getDbMediator() {
        GameContext ctx = get();
        return ctx == null ? null : ctx.getDbMediator();
    }

    public static void destroy() {
        GameContext ctx = get();
        if(ctx == null) return;
        HOLDER.remove();
        ctx.destroy();
    }

    public static GameContext init() {
        GameContext ctx = get();
        if(ctx != null) return ctx;
        ctx = new GameContext();
        HOLDER.set(ctx);
        return ctx;
    }
}
