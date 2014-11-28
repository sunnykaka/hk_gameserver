package com.origingame.item.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;

/**
 * User: Liub
 * Date: 2014/11/27
 */
public class ItemSpecResolver {

    private static final Logger log = LoggerFactory.getLogger(ItemSpecResolver.class);

    private static ItemSpecResolver INSTANCE = new ItemSpecResolver();
    private ItemSpecResolver() {}
    public static ItemSpecResolver getInstance() {
        return INSTANCE;
    }

    public void init(String filePath) throws JAXBException {

//        JAXBContext jaxbContext = JAXBContext.newInstance(ServerPersistence.class);
//        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//        log.info("读取item定义文件[{}]", filePath);
//        ServerPersistence serverPersistence = (ServerPersistence) jaxbUnmarshaller.unmarshal(Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath));
        System.out.println("找到了item文件:" + (Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath) != null));


    }




}
