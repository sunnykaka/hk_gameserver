package com.origingame.server.protocol;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 * Unit test for simple App.
 */
public class GameProtocolTest {


    @BeforeTest
    public static void init() {
    }

    @AfterTest
    public static void destroy() {
    }

    @Test
    public void test() throws Exception{

        byte[] data = "你好啊".getBytes(Charset.forName("UTF-8"));
        ByteBuf buffer = createSuccessfulByteBuf(data);
        byte[] before = buffer.array();
        GameProtocol protocol = GameProtocol.decode(buffer);
        ByteBuf after = Unpooled.buffer();
        protocol.encode(after);
        assertThat(after.array(), is(before));
        System.out.println(DatatypeConverter.printHexBinary(before));
        System.out.println(DatatypeConverter.printHexBinary(after.array()));
    }

    private ByteBuf createSuccessfulByteBuf(byte[] data) {

        ByteBuf buffer = Unpooled.buffer();
        //length
        buffer.writeInt(data.length);
        //session id
        buffer.writeInt(111);
        //id
        buffer.writeInt(222);
        //phase
        buffer.writeByte(0x1F);
        //type
        buffer.writeByte(0x0F);
        //status
        buffer.writeByte(0);
        buffer.writeBytes(data);
        return buffer;
    }

}
