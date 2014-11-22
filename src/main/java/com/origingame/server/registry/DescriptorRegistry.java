package com.origingame.server.registry;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.origingame.exception.GameException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * protobuf生成类注册器
 * 所有需要进行传输的protobuf生成类都需要调用该类的register方法进行注册.
 * 进行注册之后系统才能知道protobuf生成类的proto全限定名,不然反序列化对象的时候会报错
 *
 * User: liubin
 * Date: 14-2-27
 */
public class DescriptorRegistry {

    private static final DescriptorRegistry instance = new DescriptorRegistry();
    private DescriptorRegistry() {
    }
    public static final DescriptorRegistry getInstance() {
        return instance;
    }

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private Map<String, Class<? extends Message>> pbNameToClassMap = new HashMap<>();
    private Map<String, Method> pbNameToParseMethodMap = new HashMap<>();

    public void init() {
        if(initialized.compareAndSet(false, true)) {
//            instance.register(HandShakeProtos.class);
        }
    }

    public Class<? extends Message> getMessageClass(String fullName) {
        return pbNameToClassMap.get(fullName);
    }

    public Method getMessageParseMethod(String fullName) {
        return pbNameToParseMethodMap.get(fullName);
    }

    /**
     * 注册google protobuf生成的类,用来根据pb类的包名+类名找到对应的java类
     * @param clazz
     */
    public void register(Class<?> clazz) {
        Object getDescriptorResult;
        try {
            getDescriptorResult = clazz.getMethod("getDescriptor").invoke(null);
        } catch (NoSuchMethodException e) {
            throw new GameException(String.format("class[%s]没有getDescriptor这个类方法,传进来的是protobuf生成类?", clazz.getName()), e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new GameException(e);
        }
        if(getDescriptorResult instanceof Descriptors.FileDescriptor) {
            //必须要是FileDescriptor类型
            Descriptors.FileDescriptor fileDescriptor = (Descriptors.FileDescriptor)getDescriptorResult;
            for(Descriptors.Descriptor descriptor : fileDescriptor.getMessageTypes()) {
                //遍历文件中的所有类,进行注册
                String className = fileDescriptor.getOptions().getJavaPackage() + "." + fileDescriptor.getOptions().getJavaOuterClassname()
                        + "$" + descriptor.getName();   //得到java类名
                Class<? extends Message> messageClass;
                try {
                    //存储protobuf类全限定名和java类名的映射关系
                    messageClass = (Class<? extends Message>) Class.forName(className);
                    pbNameToClassMap.put(descriptor.getFullName(), messageClass);
                } catch (ClassNotFoundException e) {
                    throw new GameException(String.format("根据messageClassName找不到对应的class,register class[%s], messageClassName[%s]",
                            clazz.getName(), className));
                }
                try {
                    //存储protobuf类全限定名和parseFrom方法的映射关系,方便进行反序列化
                    pbNameToParseMethodMap.put(descriptor.getFullName(), messageClass.getMethod("parseFrom", byte[].class));
                } catch (NoSuchMethodException e) {
                    throw new GameException(String.format("messageClass没有parseFrom方法,register class[%s], messageClassName[%s]",
                            clazz.getName(), className));
                }
            }
        } else {
            throw new GameException(String.format("传进来的protobuf生成类的Descriptor必须是FileDescriptor类型,class[%s], getDescriptorResultClass[%s]",
                    clazz.getName(), getDescriptorResult.getClass().getName()));
        }
    }

}
