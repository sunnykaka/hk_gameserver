package com.origingame.item.resolver;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public interface ItemAttr {

    String getAttrName();


    ItemAttr parseAttrValue(String attrValue);
}
