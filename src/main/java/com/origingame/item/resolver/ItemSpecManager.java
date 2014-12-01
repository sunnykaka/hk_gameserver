package com.origingame.item.resolver;

import com.google.common.base.Preconditions;
import com.origingame.exception.ItemResolveException;
import com.origingame.util.JsonUtil;
import com.origingame.util.ReflectionUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Liub
 * Date: 2014/11/27
 */
public class ItemSpecManager {

    public static final int MAX_DROP_PROB = 100;

    private static final Logger log = LoggerFactory.getLogger(ItemSpecManager.class);

    private static ItemSpecManager INSTANCE = new ItemSpecManager();
    private ItemSpecManager() {}
    public static ItemSpecManager getInstance() {
        return INSTANCE;
    }

    private Map<String, ItemGroup> itemGroupMap = new HashMap<>();
    private Map<String, ItemSpec> itemSpecMap = new HashMap<>();

    public void init(String filePath) throws Exception {

        log.info("读取item定义文件[{}]", filePath);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath));

        //得到所有group节点
        NodeList groupNodeList = document.getElementsByTagName("group");
        for (int i = 0; i < groupNodeList.getLength(); i++) {
            Element groupElement = (Element)groupNodeList.item(i);
            String groupId = groupElement.getAttribute("id");
            Preconditions.checkArgument(!StringUtils.isBlank(groupId));
            //创建itemGroup对象
            ItemGroup itemGroup = createItemGroup(groupId);
            ////得到group下面的item_spec节点
            NodeList itemSpecNodeList = groupElement.getElementsByTagName("item_spec");

            for(int j = 0; j < itemSpecNodeList.getLength(); j++) {
                Element itemSpecElement = (Element)itemSpecNodeList.item(j);
                NamedNodeMap attributes = itemSpecElement.getAttributes();
                Map<String, String> attributesMap = new HashMap<>();
                //将item_spec的所有属性按照key,value放入map
                for(int k = 0; k < attributes.getLength(); k++) {
                    Node itemSpecAttribute = attributes.item(k);
                    if(attributesMap.put(itemSpecAttribute.getNodeName(), itemSpecAttribute.getNodeValue()) != null) {
                        throw new ItemResolveException(String.format("item_spec的attribute定义重复,attributeName[%s], attributeValue[%s]", itemSpecAttribute.getNodeName(), itemSpecAttribute.getTextContent()));
                    }

                }
                //创建itemSpec对象
                createItemSpec(attributesMap, itemGroup);
            }

        }

    }

    private void createItemSpec(Map<String, String> attributesMap, ItemGroup itemGroup) throws Exception {
        //根据类名实例化item对象
        String className = attributesMap.get("class_name");
        String id = attributesMap.get("id");
        Preconditions.checkNotNull(className);
        Preconditions.checkNotNull(id);
        ItemSpec itemSpec = (ItemSpec)Class.forName(className).newInstance();
        itemSpec.setClassName(className);
        itemSpec.setId(id);
        //需要把该key从map中删掉,以免影响后面其他属性的解析
        attributesMap.remove("class_name");
        attributesMap.remove("id");
        itemSpec.setItemGroup(itemGroup);
        //根据xml中的属性,设置对象的值
        setItemSpecAttributes(itemSpec, attributesMap);

        Preconditions.checkNotNull(itemSpec.getId());
        if(itemSpecMap.put(itemSpec.getId(), itemSpec) != null) {
            throw new ItemResolveException(String.format("item_spec的id定义重复,id[%s]", itemSpec.getId()));
        }

    }

    private void setItemSpecAttributes(ItemSpec itemSpec, Map<String, String> attributesMap) throws Exception {

        //首先设置自定义的对象类型的值
        ItemCustomAttrResolver.getInstance().saveCustomAttr(itemSpec, attributesMap);

        //设置一般属性的值
        setNormalAttributes(itemSpec, attributesMap);

    }

    public void setNormalAttributes(ItemSpec itemSpec, Map<String, String> attributesMap) throws Exception {
        Class<? extends ItemSpec> clazz = itemSpec.getClass();
        //获得该Class的所有属性定义
        List<Field> fields = ReflectionUtil.getAllFields(clazz);
        Map<String, Field> fieldMap = new HashMap<>();
        for(Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        for(Map.Entry<String, String> entry : attributesMap.entrySet()) {
            String key = toCamelCase(entry.getKey());
            String value = entry.getValue();

            if(!fieldMap.containsKey(key)) {
                throw new ItemResolveException(String.format("item_spec定义了不存在的属性, item_spec[id=%s, attrName=%s]", attributesMap.get("id"), key));
            }
            //空字符串直接忽略
            if(StringUtils.isBlank(value)) {
                continue;
            }

            Class<?> fieldType = fieldMap.get(key).getType();
            if(java.lang.Enum.class.isAssignableFrom(fieldType)) {
                //枚举属性
                Method valueOfMethod = fieldType.getMethod("valueOf", int.class);
                if(valueOfMethod != null) {
                    BeanUtils.setProperty(itemSpec, key, valueOfMethod.invoke(null, Integer.parseInt(value)));
                } else if((valueOfMethod = fieldType.getMethod("fromString", String.class)) != null){
                    BeanUtils.setProperty(itemSpec, key, valueOfMethod.invoke(null, value));
                } else {
                    throw new ItemResolveException(String.format("枚举类%s既没有valueOf(int)方法,也没有fromString(String)方法", fieldType.getName()));
                }
            } else {
                if(value.startsWith("[") && value.endsWith("]")) {
                    //json数组类型
                    BeanUtils.setProperty(itemSpec, key, JsonUtil.jsonToList(value, String.class));
                } else {
                    //一般的类型
                    BeanUtils.setProperty(itemSpec, key, value);
                }
            }
        }
    }


    private ItemGroup createItemGroup(String groupId) {
        ItemGroup itemGroup = new ItemGroup(groupId);
        if(itemGroupMap.put(groupId, itemGroup) != null) {
            throw new ItemResolveException(String.format("item_group的id定义重复,id[%s]", groupId));
        }
        return itemGroup;
    }

    public static String toCamelCase(String value) {
        String[] strings = StringUtils.split(value.toLowerCase(), "_");
        if(strings.length == 1) {
            return strings[0];
        } else {
            for (int i = 1; i < strings.length; i++){
                strings[i] = StringUtils.capitalize(strings[i]);
            }
            return StringUtils.join(strings);
        }
    }

    public <T extends ItemSpec> T getItemSpec(String id) {
        return (T)itemSpecMap.get(id);
    }

    public Map<String, ItemGroup> getItemGroupMap() {
        return itemGroupMap;
    }

    public Map<String, ItemSpec> getItemSpecMap() {
        return itemSpecMap;
    }

}
