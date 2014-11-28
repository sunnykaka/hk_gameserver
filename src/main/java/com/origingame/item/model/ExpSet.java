package com.origingame.item.model;

import com.origingame.item.resolver.DropAttr;
import com.origingame.item.resolver.ItemSpec;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class ExpSet extends ItemSpec {

    private int exp;

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }
}
