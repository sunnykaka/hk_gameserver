package com.origingame.config;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class GlobalConfig {

    public static final int GAME_SESSION_TIMEOUT = 900;

    public static final String REDIS_KEY_SEPERATOR = ":";

    public static final String ENCODING = "UTF-8";

    public static final long LOCK_FIRST_WAIT_TIME = 100;

    public static final int LOCK_SPIN_MAX_COUNT = 7;

    public static final int LOCK_EXPIRE_TIME_IN_SECONDS = 30;

    public static final String ACTION_BASE_PACKAGE = "com.origingame.business";

    public static final String SERVER_PERSISTENCE_FILE_PATH = "server-persistence.xml";

    public static final String PROTOBUF_MESSAGE_PACKAGE_NAME = "com.origingame.message.";

    public static final long CLIENT_REQUEST_TIMEOUT = 20000L;



}

