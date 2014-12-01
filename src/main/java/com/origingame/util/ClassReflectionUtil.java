package com.origingame.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.origingame.exception.ItemResolveException;
import com.origingame.item.resolver.ItemSpec;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 使用标准JSON Lib的反射机制的工具类。包括：
 * fromMap(Class<?> clazz, Map<String, ?> map) 根据Map设置对象的属性；
 * setProperty(Object object, Field field, Object value) 根据属性定义和值设置对象属性；
 * setProperty(Object object, String key, Object value) 根据属性名称和值设置对象属性；
 * getProperty(Object object, String key) 根据名字获得对象的属性值(get方法的值、或者根据名字直接获得)；
 */
public class ClassReflectionUtil {
    /**
     * 根据map设置Class对象实例的属性，目前用在ItemGroup和ItemSpec子类对象实例属性的设置
     *
     * @param clazz 需要设置属性的class对象
     * @param map   属性
     * @return Object Class对象的实例设置属性的返回对象
     * @throws Exception
     */
    public static ItemSpec setNormalAttributes(ItemSpec object, Map<String, String> map) throws Exception {
        Class<? extends ItemSpec> clazz = object.getClass();
        //获得该Class的所有属性定义
        List<Field> fields = ReflectionUtil.getAllFields(clazz);
        Map<String, Field> fieldMap = new HashMap<>();
        for(Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        for(Map.Entry<String, String> entry : map.entrySet()) {
            String key = toCamelCase(entry.getKey());
            String value = entry.getValue();

            if(!fieldMap.containsKey(key)) {
                throw new ItemResolveException(String.format("item_sepc定义了不存在的属性, item_spec[id=%s, attrName=%s]", map.get("id"), key));
            }

            writeProperty(object, key, value);
        }
        return object;
    }

    public static void writeProperty(Object object, String key, Object value) throws Exception {
        BeanUtils.setProperty(object, key, value);
    }

    public static Object readProperty(Object object, String key) throws Exception {
        return BeanUtils.getProperty(object, key);
    }

    private static String toCamelCase(String value) {
        String[] strings = StringUtils.split(value.toLowerCase(), "_");
        if(strings.length == 1) {
            return StringUtils.capitalize(strings[0]);
        } else {
            for (int i = 0; i < strings.length; i++){
                strings[i] = StringUtils.capitalize(strings[i]);
            }
            return StringUtils.join(strings);
        }
    }

    private static String buildGetMethodName(String fieldName) {
        return "get" + StringUtils.capitalize(toCamelCase(fieldName));
    }

    private static String buildSetMethodName(String fieldName) {
        return "set" + StringUtils.capitalize(toCamelCase(fieldName));
    }


}