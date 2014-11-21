package com.origingame.server.action;

import com.origingame.server.action.annotation.Action;
import com.origingame.server.action.annotation.MessageType;
import com.origingame.server.action.annotation.Readonly;

/**
 * User: Liub
 * Date: 2014/11/21
 */
@Action
public class TestAction {

    @MessageType("type1")
    public void method1() {}

    @MessageType("type2")
    @Readonly
    public void method2() {}

    @MessageType("type3")
    @Readonly
    public void method3() {}

    public void method4() {}

}
