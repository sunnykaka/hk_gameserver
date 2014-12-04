package com.origingame.server.action.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckPlayer {
    //是不是需要锁
    boolean lock() default false;

}