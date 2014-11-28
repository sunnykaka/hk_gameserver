package com.origingame.item.resolver;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class ItemCustomAttrResolver {
    private static final Logger log = LoggerFactory.getLogger(ItemCustomAttrResolver.class);

    private static ItemCustomAttrResolver INSTANCE = new ItemCustomAttrResolver();
    public static ItemCustomAttrResolver getInstance() {
        return INSTANCE;
    }

    private Map<String, Class<? extends ItemAttr>> itemAttrClassMap = new HashMap<>();

    private ItemCustomAttrResolver() {
        try {

            register(DropAttr.class);
            register(ResourceAttr.class);

        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void register(Class<? extends ItemAttr> clazz) throws IllegalAccessException, InstantiationException {
        String attrName = clazz.newInstance().getAttrName();
        itemAttrClassMap.put(attrName, clazz);
    }


    public void saveCustomAttr(ItemSpec itemSpec, Map<String, String> attributeMap) throws IllegalAccessException, InstantiationException {
        for(Map.Entry<String, Class<? extends ItemAttr>> entry : itemAttrClassMap.entrySet()) {
            String attrName = entry.getKey();
            if(!attributeMap.containsKey(attrName)) continue;
            String attrValue = attributeMap.get(attrName);
            ItemAttr itemAttr = entry.getValue().newInstance().parseAttrValue(attrValue);
            //TODO set itemAttr to itemSpec
        }

    }



}
