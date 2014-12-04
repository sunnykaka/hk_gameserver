package com.origingame.item.resolver;


import com.origingame.item.model.DropList;
import com.origingame.item.model.ExpendItem;
import com.origingame.item.model.Task;
import com.origingame.server.dao.CenterDb;
import com.origingame.server.dao.PlayerDb;
import com.origingame.server.dao.ServerPersistenceResolver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 * Unit test for simple App.
 */
public class ItemSpecManagerTest {

    private ItemSpecManager itemSpecManager = ItemSpecManager.getInstance();


    @BeforeTest
    public static void init() {
    }

    @AfterTest
    public static void destroy() {
    }

    @Test
    public void test() throws Exception{

        itemSpecManager.init(this.getClass().getPackage().getName().replaceAll("\\.", "/") + "/test-item.xml");
//        ServerPersistenceResolver.getInstance().init("test-server-persistence.xml");

        assertThat(itemSpecManager.getItemGroupMap().size(), is(7));
        assertThat(itemSpecManager.getItemSpecMap().size(), is(16));

        ExpendItem expendItem3 = itemSpecManager.getItemSpec("ExpendItem-3");

        assertThat(expendItem3.getClassName(), is("com.origingame.item.model.ExpendItem"));
        assertThat(expendItem3.getName(), is("秋刀鱼(小)"));

        Task task = itemSpecManager.getItemSpec("Task-1-1");
        assertThat(task.getNextTasks().size(), is(2));
        assertThat(task.getNextTasks().get(0), is("Task-1-2"));


        ExpendItem expendItem1 = itemSpecManager.getItemSpec("ExpendItem-1");
        assertThat((Integer)expendItem1.getResource().get(ResourceAttr.Type.GEM), is(1));
        assertThat((Integer)expendItem1.getResource().get(ResourceAttr.Type.GOLD), is(2));
        assertThat((Integer)expendItem1.getResource().get(ResourceAttr.Type.VIT), is(3));

        //[['ExpendItem-1','1'],['ExpendItem-2','2','100']]
        DropList dropList1 = itemSpecManager.getItemSpec("DropList-1");
        assertThat(dropList1.getDrops().getEntries().size(), is(2));

        assertThat(dropList1.getDrops().getEntries().get(0).getItemId(), is("ExpendItem-1"));
        assertThat(dropList1.getDrops().getEntries().get(0).getNumber(), is(1));
        assertThat(dropList1.getDrops().getEntries().get(0).getProb(), is(100));

        assertThat(dropList1.getDrops().getEntries().get(1).getItemId(), is("ExpendItem-2"));
        assertThat(dropList1.getDrops().getEntries().get(1).getNumber(), is(2));
        assertThat(dropList1.getDrops().getEntries().get(1).getProb(), is(100));

    }


}
