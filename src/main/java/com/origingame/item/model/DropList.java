package com.origingame.item.model;

import com.origingame.item.resolver.DropAttr;
import com.origingame.item.resolver.ItemSpec;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class DropList extends ItemSpec {


    private DropAttr dropAttr;

    private int number;


    public DropAttr getDropAttr() {
        return dropAttr;
    }

    public void setDropAttr(DropAttr dropAttr) {
        this.dropAttr = dropAttr;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
