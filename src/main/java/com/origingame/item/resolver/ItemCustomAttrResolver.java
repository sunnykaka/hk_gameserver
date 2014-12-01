package com.origingame.item.resolver;

import com.origingame.exception.ItemResolveException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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


    public void saveCustomAttr(ItemSpec itemSpec, Map<String, String> attributeMap) throws Exception {
        for(Map.Entry<String, Class<? extends ItemAttr>> entry : itemAttrClassMap.entrySet()) {
            String attrName = entry.getKey();
            if(!attributeMap.containsKey(attrName)) continue;
            String attrValue = attributeMap.get(attrName);
            if(!StringUtils.isBlank(attrValue)) {
                //set itemAttr to itemSpec
                ItemAttr itemAttr;
                try {
                    itemAttr = entry.getValue().newInstance().parseAttrValue(attrValue);
                } catch (ItemResolveException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("", e);
                    throw new ItemResolveException(String.format("自定义对象属性解析的时候发生错误, id[%s], attrName[%s], attrValue[%s]", itemSpec.getId(), attrName, attrValue));
                }
                String fieldName = ItemSpecManager.toCamelCase(attrName);
                BeanUtils.setProperty(itemSpec, fieldName, itemAttr);
            }
            attributeMap.remove(attrName);
        }
    }

}
