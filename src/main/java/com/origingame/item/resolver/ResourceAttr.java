package com.origingame.item.resolver;

import com.origingame.exception.ItemResolveException;
import com.origingame.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class ResourceAttr implements ItemAttr{

    private Map<Type, Object> resourceMap = new HashMap<>();

    public void addResource(String typeStr, Object value) {
        Type type = Type.fromString(typeStr);
        if(type == null) {
            throw new ItemResolveException(String.format("发现不存在的resource:{%s: %s}", typeStr, value));
        }
        resourceMap.put(type, value);
    }

    public Map<Type, Object> getResourceMap() {
        return resourceMap;
    }

    @Override
    public String getAttrName() {
        return "resource";
    }

    @Override
    public ItemAttr parseAttrValue(String attrValue) throws Exception {

        Map<String, String> map = JsonUtil.json2Object(attrValue, Map.class);
        for(Map.Entry<String, String> entry : map.entrySet()) {
            addResource(entry.getKey(), entry.getValue());
        }

        return this;
    }

    public Object get(Type type) {
        return resourceMap.get(type);
    }

    public enum Type {

        /**
         * 金币
         */
        GOLD("gold"),

        /**
         * 宝石
         */
        GEM("gem"),


        /**
         * 体力
         */
        VIT("vit"),

        /**
         * 经验
         */
        EXP("exp");

        public String value;

        Type(String value) {
            this.value = value;
        }

        public static Type fromString(String value) {
            if(StringUtils.isBlank(value)) {
                return null;
            }
            for(Type enumValue : values()) {
                if(enumValue.value.equalsIgnoreCase(value)) {
                    return enumValue;
                }
            }
            return null;
        }

    }



}
