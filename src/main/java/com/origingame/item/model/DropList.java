package com.origingame.item.model;

import com.origingame.item.resolver.DropAttr;
import com.origingame.item.resolver.ItemSpec;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class DropList extends ItemSpec {


    private DropAttr drops;

    private int number;

    public DropAttr getDrops() {
        return drops;
    }

    public void setDrops(DropAttr drops) {
        this.drops = drops;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
