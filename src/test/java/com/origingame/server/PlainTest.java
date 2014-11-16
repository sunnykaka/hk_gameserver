package com.origingame.server;


import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * Unit test for simple App.
 */
@Test
public class PlainTest {

    @BeforeTest
    public void init() {
        System.out.println("before");
    }

    @Test(enabled = false)
    public void testApp() {
        print1(13);
    }

    @Test
    public void test2() {
        long l = 2147483649L;
        int i = (int)l;
        System.out.println(i);
        long l2 = i & 0xFFFFFFFFL;
        System.out.println(l2);
    }

    @Test
    public void test3() {
        System.out.println("native order: " + ByteOrder.nativeOrder());

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        System.out.println("byteBuffer order: " + byteBuffer.order());
        byteBuffer.putShort((short)1);
        System.out.println("change order: " + ByteOrder.LITTLE_ENDIAN);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putShort((short) 1);
        byte[] array = byteBuffer.array();
        System.out.println(array.length);
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + ", ");
        }

    }

    private void print1(int num) {
        byte[] array = new byte[32];
        for (int i = 31; i >= 0; i--) {
            array[31 - i] = (byte)((num >>> i) & 0x00000001);
        }
        String s = "[";
        for (int i = 0; i < array.length; i++) {
            s += array[i];
            if(i != array.length - 1) {
                s += ", ";
            }
        }
        s += "]";
        System.out.println(s);
//        System.out.println(Integer.toBinaryString(num));
    }

    private void print2 (int num) {
        byte[] array = new byte[32];
        for (int i = 31; i >= 0; i--) {
            array[i] = (byte)(num & 1);
            num = num >> 1;
        }
        String s = "[";
        for (int i = 0; i < array.length; i++) {
            s += array[i];
            if(i != array.length - 1) {
                s += ", ";
            }
        }
        s += "]";
        System.out.println(s);
    }

    @Test
    public void test() throws InvocationTargetException, IllegalAccessException {
        MyBean myBean = new MyBean();
        myBean.setName("你好");
        myBean.setAge(18);

        Map map = (Map) new ConvertUtilsBean().convert(myBean, Map.class);
        MyBean myBean2 = new MyBean();
        BeanUtils.populate(myBean2, map);

        System.out.println(myBean2.getName());
        System.out.println(myBean2.getAge());

    }

    static class MyBean {
        private String name;

        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
