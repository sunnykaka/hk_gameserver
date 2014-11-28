package com.origingame.item.model;

import com.origingame.item.resolver.DropAttr;
import com.origingame.item.resolver.ItemSpec;
import com.origingame.item.resolver.ResourceAttr;

import java.util.List;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class Task extends ItemSpec {

    private String taskInfo;

    private List<String> nextTasks;

    private ResourceAttr resourceAttr;

    private String condition;

    private DropAttr drops;

    private int needLevel;

    private boolean daily;


    public String getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(String taskInfo) {
        this.taskInfo = taskInfo;
    }

    public List<String> getNextTasks() {
        return nextTasks;
    }

    public void setNextTasks(List<String> nextTasks) {
        this.nextTasks = nextTasks;
    }

    public ResourceAttr getResourceAttr() {
        return resourceAttr;
    }

    public void setResourceAttr(ResourceAttr resourceAttr) {
        this.resourceAttr = resourceAttr;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public DropAttr getDrops() {
        return drops;
    }

    public void setDrops(DropAttr drops) {
        this.drops = drops;
    }

    public int getNeedLevel() {
        return needLevel;
    }

    public void setNeedLevel(int needLevel) {
        this.needLevel = needLevel;
    }

    public boolean isDaily() {
        return daily;
    }

    public void setDaily(boolean daily) {
        this.daily = daily;
    }
}
