package com.origingame.server.action;

import com.origingame.server.action.annotation.Action;
import com.origingame.server.action.annotation.MessageType;
import com.origingame.server.exception.GameException;
import com.origingame.server.util.WalkPackageUtil;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.classreading.MetadataReader;

import java.lang.reflect.Method;
import java.util.List;

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
                    if(method.isAnnotationPresent(MessageType.class)) {
                        String[] messageTypes = method.getAnnotation(MessageType.class).value();

                    }
                }
            }


        } catch (Exception e) {
            log.error("初始化失败", e);
            throw new GameException(e);
        }
    }



}
