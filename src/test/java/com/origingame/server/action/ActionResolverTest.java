package com.origingame.server.action;


import com.google.common.collect.Sets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 * Unit test for simple App.
 */
public class ActionResolverTest {


    @BeforeClass
    public static void init() {
    }

    @AfterClass
    public static void destroy() {
    }

    @Test
    public void test() throws Exception{

        ActionResolver.getInstance().init(this.getClass().getPackage().getName());
        Map<String, Method> actionMethodMap = ActionResolver.getInstance().actionMethodMap;
        Map<String, Object> actionObjectMap = ActionResolver.getInstance().actionObjectMap;
        Map<String, String> messageTypeActionRelationMap = ActionResolver.getInstance().messageTypeActionRelationMap;
        Set<String> readonlyActionMethodSet = ActionResolver.getInstance().lockFreeActionMethodSet;

        assertThat(actionMethodMap.keySet(), is((Set<String>) Sets.newHashSet("type1", "type2", "type3")));

        assertThat(actionObjectMap.keySet(), is((Set<String>)Sets.newHashSet(TestAction.class.getName())));

        assertThat(messageTypeActionRelationMap.keySet(), is((Set<String>)Sets.newHashSet("type1", "type2", "type3")));
        assertThat(messageTypeActionRelationMap.size(), is(3));
        for(String className : messageTypeActionRelationMap.values()) {
            assertThat(className, is(TestAction.class.getName()));
        }

        assertThat(readonlyActionMethodSet, is((Set<String>)Sets.newHashSet("type2", "type3")));

    }




}
