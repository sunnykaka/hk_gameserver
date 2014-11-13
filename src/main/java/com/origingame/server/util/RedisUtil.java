package com.origingame.server.util;

import com.google.common.base.Preconditions;
import com.origingame.config.GlobalConfig;
import com.origingame.server.exception.GameException;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class RedisUtil {

    public static String buildKey(String... parts) {
        if(parts == null || parts.length == 0) {
            throw new GameException("构建redis的key不能为空");
        }

        StringBuilder sb = new StringBuilder();
        for(String part : parts) {
            sb.append(part).append(GlobalConfig.REDIS_KEY_SEPERATOR);
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

}
