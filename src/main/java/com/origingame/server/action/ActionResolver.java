package com.origingame.server.action;

import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.server.action.annotation.Action;
import com.origingame.server.action.annotation.CheckPlayer;
import com.origingame.server.action.annotation.MessageType;
import com.origingame.server.context.GameContext;
import com.origingame.exception.GameBusinessException;
import com.origingame.exception.GameException;
import com.origingame.server.lock.PlayerDbLock;
import com.origingame.server.protocol.ServerRequestWrapper;
import com.origingame.server.util.WalkPackageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private AtomicBoolean initialized = new AtomicBoolean(false);

    /** key:messageType, value:actionMethod */
    protected Map<String, Method> actionMethodMap = new HashMap<>();
    /** key:messageType, value:actionClassName */
    protected Map<String, String> messageTypeActionRelationMap = new HashMap<>();
    /** key:actionClassName, value:ActionInstance */
    protected Map<String, Object> actionObjectMap = new HashMap<>();
    /** readonly messageType set */
//    protected Set<String> lockFreeActionMethodSet = new HashSet<>();
//    /** readonly messageType set */
//    protected Set<String> noCheckPlayerActionMethodSet = new HashSet<>();
    protected Map<String, CheckPlayer> checkPlayerAnnotationMap = new HashMap<>();

    public void init(String basePackage) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(!initialized.compareAndSet(false, true)) return;

        log.info("搜寻{}目录下的Action类", basePackage);
        List<Class> actionClassList = WalkPackageUtil.findTypes(basePackage,
                new WalkPackageUtil.CandidateFinder() {
                    @Override
                    public Class findCandidate(MetadataReader metadataReader) throws ClassNotFoundException {
                        Class c = Class.forName(metadataReader.getClassMetadata().getClassName());
                        if (c.getAnnotation(Action.class) != null) {
                            log.info("搜寻到Action类: {}", c.getName());
                            return c;
                        }
                        return null;
                    }
                }
        );

        log.info("搜寻得到{}目录下的Action类数量: {}", basePackage, actionClassList.size());
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
//                boolean lockFree = method.isAnnotationPresent(LockFree.class);
//                boolean checkPlayer = method.isAnnotationPresent(CheckPlayer.class);
                CheckPlayer checkPlayer = method.getAnnotation(CheckPlayer.class);
                for(String messageType : messageTypes) {
                    if(actionMethodMap.put(messageType, method) != null) {
                        throw new GameException(String.format("Action[%s],method[%s]对应的messageType[%s]重复定义",
                                actionClass.getName(), method.getName(), messageType));
                    }
                    messageTypeActionRelationMap.put(messageType, actionClass.getName());
//                    if(lockFree) {
//                        lockFreeActionMethodSet.add(messageType);
//                    }
                    if(checkPlayer != null) {
                        checkPlayerAnnotationMap.put(messageType, checkPlayer);
                    }
                    log.info("解析得到messageType[{}]对应Action类[{}],checkPlayer[{}]", messageType, actionClass.getName(), checkPlayerToString(checkPlayer));
                }
            }
            actionObjectMap.put(actionClass.getName(), actionClass.newInstance());
        }


    }

    private String checkPlayerToString(CheckPlayer checkPlayer) {
        String result = "";
        if(checkPlayer != null) {
            result = "check=true, lock=" + checkPlayer.lock();
        }
        return result;
    }

    public Message executeAction0(GameContext ctx, Message message)
            throws InvocationTargetException, IllegalAccessException {

        ServerRequestWrapper request = ctx.getRequest();
        String messageType = request.getMessageType();
        Method actionMethod = actionMethodMap.get(messageType);
        if (actionMethod == null) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.NO_ACTION_FOR_MESSAGE_TYPE);
        }
        String actionClassName = messageTypeActionRelationMap.get(messageType);
        Object actionObject = actionObjectMap.get(actionClassName);

        if (log.isDebugEnabled()) {
            log.debug("执行action方法,actionClassName[{}], methodName[{}], message",
                    actionClassName, actionMethod.getName(), message);
        }

        int playerId = request.getPlayerId();
        CheckPlayer checkPlayer = checkPlayerAnnotationMap.get(messageType);
//        boolean needLock = lockFreeActionMethodSet.contains(messageType);
//        boolean noCheckPlayer = lockFreeActionMethodSet.contains(messageType);
        boolean needLock = checkPlayer != null && checkPlayer.lock();
        if(checkPlayer != null && playerId <= 0) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.PLAYER_ID_INVALID);
        }

        PlayerDbLock lock = null;

        try {
            if (needLock) {
                //对playerId加互斥锁
                lock = PlayerDbLock.newLock(ctx.getDbMediator().selectPlayerDb(playerId).getJedis(), "player", String.valueOf(playerId));
                lock.lock();
            }
            //检验session合法性
            ctx.checkSession(checkPlayer);

            //执行方法
            Message result = (Message) actionMethod.invoke(actionObject, ctx, message);

            //保存player数据
            ctx.savePlayerAfterRequest();
            return result;
        } catch (InvocationTargetException e) {
            if(GameBusinessException.class.isAssignableFrom(e.getTargetException().getClass())) {
                throw (GameBusinessException)e.getTargetException();
            } else {
                throw e;
            }
        } finally {
            if(lock != null) {
                lock.release();
            }
        }
    }


}
