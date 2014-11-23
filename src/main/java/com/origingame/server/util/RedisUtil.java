package com.origingame.server.util;

import com.google.protobuf.ByteString;
import com.origingame.config.GlobalConfig;
import com.origingame.exception.GameDaoException;
import com.origingame.exception.GameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class RedisUtil {

    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    public static String buildKey(String... parts) {
        if(parts == null || parts.length == 0) {
            throw new GameException("构建redis的key不能为空");
        }

        StringBuilder sb = new StringBuilder();
        for(String part : parts) {
            sb.append(part).append(GlobalConfig.REDIS_KEY_SEPERATOR);
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public static byte[] buildByteKey(String... parts) {
        try {
            return buildKey(parts).getBytes(GlobalConfig.ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.error("", e);
            return null;
        }
    }

    public static void checkSetResponse(String resp) {
        if(!"OK".equals(resp)) {
            throw new GameDaoException(String.format("请求预期返回[%s],实际返回[%s]", "OK", resp));
        }
    }

    public static boolean byteStringEquals(ByteString one, ByteString other) {
        if(one == null || other == null) return false;
        return one.equals(other);
    }
}
