package com.origingame.server.dao;

/**
 * User: Liub
 * Date: 2014/11/18
 */
public class Db {

    private DbType type;




    static enum DbType {

        CENTER,

        PLAYER,

        SESSION;
    }
}
