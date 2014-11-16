package com.origingame.server.action;

import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.server.action.annotation.Action;
import com.origingame.server.action.annotation.MessageType;
import com.origingame.server.context.GameContext;
import com.origingame.server.exception.GameBusinessException;
import com.origingame.server.exception.GameException;
import com.origingame.server.protocol.RequestWrapper;
import com.origingame.server.util.WalkPackageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Liub
 * Date: 2014/11/14
 */
public class ActionResolver {

    private static final Logger log = LoggerFactory.getLogger(ActionResolver.class);

    private static ActionResolver INSTANCE = new ActionResolver();

    private ActionResolver() {}

    public static ActionResolver getInstance() {
        return INSTANCE;
    }

    private Map<String, Method> actionMethodMap = new HashMap<>();
    private Map<String, String> messageTypeActionRelationMap = new HashMap<>();
    private Map<String, Object> actionObjectMap = new HashMap<>();



    public void init() {

        try {
            List<Class> actionClassList = WalkPackageUtil.findTypes("/com/origingame/business",
                    new WalkPackageUtil.CandidateFinder() {
                        @Override
                        public Class findCandidate(MetadataReader metadataReader) throws ClassNotFoundException {
                            Class c = Class.forName(metadataReader.getClassMetadata().getClassName());
                            if (c.getAnnotation(Action.class) != null) {
                                return c;
                            }
                            return null;
                        }
                    }
            );

            for(Class actionClass : actionClassList) {
                Method[] methods = actionClass.getMethods();
                for(Method method : methods) {
                    if(!method.isAnnotationPresent(MessageType.class)) {
                        continue;
                    }
                    String[] messageTypes = method.getAnnotation(MessageType.class).value();
                    if(messageTypes == null || messageTypes.length == 0) {
                        throw new GameException(String.format("Action[%s],method[%s]对应的messageTypes内容为空",
                                actionClass.getName(), method.getName()));
                    }
                    for(String messageType : messageTypes) {
                        if(actionMethodMap.put(messageType, method) != null) {
                            throw new GameException(String.format("Action[%s],method[%s]对应的messageType[%s]重复定义",
                                    actionClass.getName(), method.getName(), messageType));
                        }
                        messageTypeActionRelationMap.put(messageType, actionClass.getName());
                    }
                }
                actionObjectMap.put(actionClass.getName(), actionClass.newInstance());
            }


        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("初始化失败", e);
            throw new GameException(e);
        }
    }

    public Message executeAction0(GameContext ctx, Message message)
            throws InvocationTargetException, IllegalAccessException {

        RequestWrapper request = ctx.getRequest();
        String messageType = request.getRequestMsg().getMessageType();
        Method actionMethod = actionMethodMap.get(messageType);
        if(actionMethod == null) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.NO_ACTION_FOR_MESSAGE_TYPE);
        }
        String actionClassName = messageTypeActionRelationMap.get(messageType);
        Object actionObject = actionObjectMap.get(actionClassName);

        if(log.isDebugEnabled()) {
            log.debug("执行action方法,actionClassName[{}], methodName[{}], message",
                    actionClassName, actionMethod.getName(), message);
        }

        //执行方法
        Message result = (Message)actionMethod.invoke(actionObject, ctx, message);

        return result;


    }


}
