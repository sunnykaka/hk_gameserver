package com.origingame.util;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * 反射机制的工具类。包括：
 * getAllMethods(Class<?> clazz) 获得类的所有方法(共有、私有以及父类)；
 * getAllMethods(Class<?> clazz, String regex) 根据正则表达式获得所有符合的方法(共有、私有以及父类)；
 * getAllFields(Class<?> clazz) 获得类的所有属性(共有、私有以及父类)；
 * getAllEntityFields(Class<?> clazz) 获得含有@Entity注释的类的所有属性(共有、私有以及父类)；
 * fromMap(Class<?> clazz, Map<String, ?> map) 根据Map设置对象的属性；
 * setProperty(Object object, Field field, Object value) 根据属性定义和值设置对象属性；
 * setProperty(Object object, String key, Object value) 根据属性名称和值设置对象属性；
 * getProperty(Object object, String key) 根据名字获得对象的属性值(get方法的值、或者根据名字直接获得)；
 */
public class ReflectionUtil {
	private static Map<String, Map<String, Method>> METHODS = new ConcurrentHashMap<String, Map<String, Method>>();
	private static Map<String, List<Field>> FIELDS = new ConcurrentHashMap<String, List<Field>>();
	private static Map<String, List<Field>> ENTITY_FIELDS = new ConcurrentHashMap<String, List<Field>>();
	
	/**
	 * 通过传入的Class对象获得该Class的所有方法
	 * @param clazz Class对象
	 * @return Map<String, Method> 方法名与java.lang.reflect.Method的键值对
	 */
	public static Map<String, Method> getAllMethods(Class<?> clazz){
		//如果存在所有方法的集合，则直接返回
		String className = clazz.getName();
		if(METHODS.containsKey(className)){
			return Collections.unmodifiableMap(METHODS.get(className));
		}
		//方法栈
		Stack<Map<String, Method>> stack = new Stack<Map<String, Method>>();
		Map<String, Method> methods = new HashMap<String, Method>();
		//getDeclaredMethods()获得当前类的所有方法，包括共有以及私有方法。
		//用getMethod()方法可以获得继承方法但是不能获得私有的方法
		//所以这里用到stack是保证由子类覆盖父类的方法而不是父类覆盖子类
		for(Method method : clazz.getDeclaredMethods()){
			if(Modifier.isStatic(method.getModifiers())){
				continue;
			}
			methods.put(method.getName(), method);
		}
		//当前类方法入栈
		stack.push(methods);
		//如果存在父类则将父类的方法入栈
		//while循环保证所有方法都能入栈
		Class<?> superClass = clazz.getSuperclass();
        while(superClass != null){
        	methods = new HashMap<String, Method>();
        	for(Method method : superClass.getDeclaredMethods()){
        		if(Modifier.isStatic(method.getModifiers())){
    				continue;
    			}
        		methods.put(method.getName(), method);
    		}
        	stack.push(methods);
        	//获得父类
        	superClass = superClass.getSuperclass();
        }
        methods = new HashMap<String, Method>();
        //将栈中所有方法put到Map中
    	while(!stack.isEmpty()){
        	methods.putAll(stack.pop());
        }
    	METHODS.put(className, methods);
        return methods;
	}
	
	/**
	 * 通过传入的Class对象获得该Class的所有方法，方法名通过regex进行正则匹配
	 * @param clazz Class对象
	 * @param regex 方法名的正则匹配字符串
	 * @return Map<String, Method> 方法名与java.lang.reflect.Method的键值对
	 */
	public static Map<String, Method> getAllMethods(Class<?> clazz, String regex){		
		Map<String, Method> results = new HashMap<String, Method>();
		Map<String, Method> methods = getAllMethods(clazz);
		String methodName = null;
		for(Method method : methods.values()){
			//如果方法名符合正则表达式则压入栈
			methodName = method.getName();
			if(methodName.matches(regex)){
				results.put(methodName, method);
			}
		}
        return results;
	}
	
	/**
	 * 通过传入的Class对象获得该Class的所有属性名
	 * @param clazz Class对象
	 * @return List<Field> 属性的列表
	 */
	public static List<Field> getAllFields(Class<?> clazz){
		//如果存在所有属性的列表，则直接返回
		String className = clazz.getName();
		if(FIELDS.containsKey(className)){
			return Collections.unmodifiableList(FIELDS.get(className));
		}
		//属性栈
		Stack<List<Field>> stack = new Stack<List<Field>>();
		List<Field> fields = new ArrayList<Field>();
		//getDeclaredFields()获得当前类的所有属性，包括共有以及私有方法。
		//用getFields()方法可以获得继承属性但是不能获得私有的属性
		//所以这里用到stack是保证由子类覆盖父类的属性而不是父类覆盖子类
		for(Field field : clazz.getDeclaredFields()){
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			field.setAccessible(true);
			fields.add(field);
		}
		stack.push(fields);
		//如果存在父类则将父类的方法入栈
		//while循环保证所有父类属性都能入栈			
		Class<?> superClass = clazz.getSuperclass();
        while(superClass != null){
        	fields = new ArrayList<Field>();
        	for(Field field : superClass.getDeclaredFields()){
        		if(Modifier.isStatic(field.getModifiers())){
    				continue;
    			}
    			field.setAccessible(true);
        		fields.add(field);			
    		}
    		stack.push(fields);
        	superClass = superClass.getSuperclass();
        }
        fields = new ArrayList<Field>();
        //将属性栈中的值pop出来
        while(!stack.isEmpty()){
        	fields.addAll(stack.pop());
        }
        FIELDS.put(className, fields);
        return fields;
	}
	
	/**
	 * 通过传入的含有@Entity注释的Class对象获得该Class的所有属性名
	 * @param clazz 带有@Entity注释的Class对象
	 * @return List<Field> 持久化属性的列表
	 */
	public static List<Field> getAllEntityFields(Class<?> clazz){
		//如果存在所有属性的列表，则直接返回
		String className = clazz.getName();
		if(ENTITY_FIELDS.containsKey(className)){
			return Collections.unmodifiableList(ENTITY_FIELDS.get(className));
		}
		//属性栈
		Stack<List<Field>> stack = new Stack<List<Field>>();
		List<Field> fields = new ArrayList<Field>();
		//getDeclaredFields()获得当前类的所有属性，包括共有以及私有方法。
		//用getFields()方法可以获得继承属性但是不能获得私有的属性
		//所以这里用到stack是保证由子类覆盖父类的属性而不是父类覆盖子类
//		if(Serializer.class.isAssignableFrom(clazz)){
//			for(Field field : clazz.getDeclaredFields()){
//				if(Modifier.isStatic(field.getModifiers())
////					|| field.isAnnotationPresent(Transient.class)
//					) {
//					continue;
//				}
//				field.setAccessible(true);
//				fields.add(field);
//			}
//			stack.push(fields);
//		}
		//如果存在父类则将父类的方法入栈
		//while循环保证所有父类属性都能入栈			
		Class<?> superClass = clazz.getSuperclass();
        while(superClass != null){
//        	if(!Serializer.class.isAssignableFrom(superClass)){
//        		superClass = superClass.getSuperclass();
//        		continue;
//        	}
        	fields = new ArrayList<Field>();
        	for(Field field : superClass.getDeclaredFields()){
        		if(Modifier.isStatic(field.getModifiers())
//    				|| field.isAnnotationPresent(Transient.class)
    				) {
    				continue;
    			}		
        		fields.add(field);
    		}
    		stack.push(fields);
        	superClass = superClass.getSuperclass();
        }
        fields = new ArrayList<Field>();
        //将属性栈中的值pop出来        
        while(!stack.isEmpty()){
        	fields.addAll(stack.pop());
        }
        ENTITY_FIELDS.put(className, fields);
        return fields;
	}
	
	/**
	 * 根据map设置Class对象实例的属性，目前用在ItemGroup和ItemSpec子类对象实例属性的设置
	 * @param clazz 需要设置属性的class对象
	 * @param map 属性
	 * @return Object Class对象的实例设置属性的返回对象
	 * @throws Exception
	 */
	public static Object fromMap(Class<?> clazz, Map<String, ?> map) throws Exception{
		//获得具体Class对象的实例，比如：CropItemSpec
		Object object = clazz.newInstance();
		//获得该Class的所有属性定义
		List<Field> fields = getAllFields(clazz);
		String key;
		for(Field field : fields){
			//获得属性名
			key = field.getName();
			if(!map.containsKey(key)){
				continue;
			}
			//根据属性定义和属性值设置对象的值
			setProperty(object, field, map.get(key));
		}
		return object;
	}
	
	/**
	 * 提供一个简单的深拷贝方法
	 * @param target 拷贝的目标对象
	 * @return T 拷贝的新对象
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object> T clone(T target) throws Exception{
		Class<T> clazz = (Class<T>) target.getClass();
		//获得具体Class对象的实例，比如：CropItemSpec
		T object = clazz.newInstance();
		//获得该Class的所有属性定义
		List<Field> fields = ReflectionUtil.getAllFields(clazz);
		for(Field field : fields){
			//根据属性定义和属性值设置对象的值
			field.set(object, field.get(target));
		}
		return object;
	}
	
	/**
	 * 根据属性定义和属性值设置对象的值
	 * @param object
	 * @param field 属性定义
	 * @param value 属性值
	 * @throws Exception 
	 */
	public static void setProperty(Object object, Field field, Object value) throws Exception{
		String key = field.getName();
		//如果属性值是null则设置返回对象属性值为null
		if(value == null){				
			writeProperty(object, key, null);
			return;
		}
		//获得属性值的对象
		Class<?> valueClass = value.getClass();
		//如果属性的对象是string并且值是空或者null则设置返回对象属性值为null
		if(String.class.isAssignableFrom(valueClass)){
			String valueString = (String) value;
			if(valueString.isEmpty()
				|| valueString.equalsIgnoreCase("null")){
				writeProperty(object, key, null);
				return;
			}
		}
		//获得属性定义的对象类型
		Class<?> typeClass = field.getType();
		//如果不是数组或者不是list
		if(!typeClass.isArray()
			&& !List.class.isAssignableFrom(typeClass)){
			//如果值是以{开头的string，按json格式设置到object中
			if(String.class.isAssignableFrom(valueClass)
				&& ((String) value).startsWith("{")){				
				writeProperty(object, key, JsonUtil.json2Object((String) value, Map.class));
				return;
			}
			//对于一般的string类型 直接set
			writeProperty(object, key, value);
			return;
		}
		//如果值是以[开头的string，存储形式是将是list或者array
		if(String.class.isAssignableFrom(valueClass)
			&& ((String) value).startsWith("[")){
			//如果定义也是list则直接按jsonarray形式设置到object中
			if(List.class.isAssignableFrom(typeClass)){
				writeProperty(object, key, JsonUtil.json2Object((String) value, List.class));
				return;
			}
			//如果定义不是list而是数组类型,需要对value转换后存储
			writeProperty(object, key, convertArray(JsonUtil.json2Object((String) value, List.class), field.getType()));
			return;
		}
		//一般的string类型直接set
		writeProperty(object, key, value);		
	}
	
	/**
	 * 对于object根据key设置属性值
	 * @param object 需要设置属性的对象
	 * @param key 属性名
	 * @param value 设置值
	 * @throws Exception
	 */
	public static void setProperty(Object object, String key, Object value) throws Exception{
		//如果值是null
		if(value == null){		
			writeProperty(object, key, null);
			return;
		}
		Class<?> valueClass = value.getClass();
		//如果是string但是值是null
		if(String.class.isAssignableFrom(valueClass)){
			String valueString = (String) value;
			if(valueString.isEmpty()
				|| valueString.equalsIgnoreCase("null")){
				writeProperty(object, key, null);
				return;
			}
		}
		//一般的string直接设置
		writeProperty(object, key, value);
	}
	
	/**
	 * 根据key获得对象的属性值
	 * @param object
	 * @param key 属性名
	 * @return
	 * @throws Exception
	 */
	public static Object getProperty(Object object, String key) throws Exception{
		String getter = new StringBuffer()
			.append("get")
			.append(StringUtils.capitalize(key))
			.toString();
		Map<String, Method> methods = getAllMethods(object.getClass());
		//如果对象中有getXXX方法符合key名字
		if(methods.containsKey(getter)){
			//invoke()方法通过发射机制调用object的方法
			return methods.get(getter).invoke(object);
		}
		//直接通过反射机制在object中获得key名字的值
		return readProperty(object, key);
	}
	
	/**
	 * 根据calzz定义的具体数组类型对list进行转换后存储,对于二维以上的数组该方法会递归调用convertArray()函数
	 * @param list 需要储存的数组
	 * @param clazz 需要转换的对象定义
	 * @return Object 转换后的数组
	 * @throws Exception
	 */
	private static Object convertArray(List<?> list, Class<?> clazz) throws Exception{
		//如果转换值为null则直接返回
		if(list == null){
			return null;
		}
		/**
		 * 获得数组的具体类型
		 * 对于int[] clazz.getName()：[I  clazz.getComponentType().getName():int
		 * 对于String[] clazz.getName()：[Ljava.lang.String  clazz.getComponentType().getName():java.lang.String
		 * 对于String[][] clazz.getName()：[[Ljava.lang.String  clazz.getComponentType().getName():[Ljava.lang.String
		 * 所以对于二维以上的数组需要递归调用convertArray()函数
		 */
		Class<?> elementClass = clazz.getComponentType();
		//根据类的定义和list大小生成array数组的子类实例
		Object object = Array.newInstance(elementClass, list.size());
		//如果list长度是空
		if(list.isEmpty()){
			return object;
		}
		int index = 0;
		//如果是二维以上的数组则递归调用
		if(elementClass.isArray()){
			//对于每一维递归调用设置数组，value是list的某一个值，value是一个数组，elemtClass是该数组的定义
			for(Object value : list){
				Array.set(object, index++, convertArray((List<?>) value, elementClass));
			}
			return object;
		}
		Class<?> valueClass;
		//如果就是1维的数组则根据定义直接设置
		for(Object value : list){
			//如果该值是null
			if(value == null){
				Array.set(object, index++, null);
				continue;
			}
			valueClass = value.getClass();
			//如果是string但是值是null
			if(String.class.isAssignableFrom(valueClass)){
				String valueString = (String) value;
				if(valueString.isEmpty()
					|| valueString.equalsIgnoreCase("null")){
					Array.set(object, index++, null);
					continue;
				}
			}
			//正常的value直接设置
			Array.set(object, index++, ConvertUtils.convert(value, elementClass));
		}
		//返回设置好的object对象
		return object;
	}
	
	public static void writeProperty(Object object, String key, Object value) throws Exception{
		BeanUtils.setProperty(object, key, value);
	}
	
	public static Object readProperty(Object object, String key) throws Exception{
		return BeanUtils.getProperty(object, key);
	}
	
	public static void clearAll(){
		METHODS.clear();
		METHODS = null;
		FIELDS.clear();
		FIELDS = null;
		ENTITY_FIELDS.clear();
		ENTITY_FIELDS = null;
	}
}