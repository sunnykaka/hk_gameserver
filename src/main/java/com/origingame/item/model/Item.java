package com.origingame.item.model;

import com.origingame.item.resolver.ItemSpec;
import com.origingame.item.resolver.ResourceAttr;

import java.util.Map;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class Item extends ItemSpec {

    private boolean usable;

    private boolean sellable;

    private boolean givable;

    private boolean stack;

    private String icon;

    private int level;

    private ResourceAttr resource;

    public boolean isUsable() {
        return usable;
    }

    public void setUsable(boolean usable) {
        this.usable = usable;
    }

    public boolean isSellable() {
        return sellable;
    }

    public void setSellable(boolean sellable) {
        this.sellable = sellable;
    }

    public boolean isGivable() {
        return givable;
    }

    public void setGivable(boolean givable) {
        this.givable = givable;
    }

    public boolean isStack() {
        return stack;
    }

    public void setStack(boolean stack) {
        this.stack = stack;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public ResourceAttr getResource() {
        return resource;
    }

    public void setResource(ResourceAttr resource) {
        this.resource = resource;
    }
}
