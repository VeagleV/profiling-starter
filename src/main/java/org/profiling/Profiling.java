package org.profiling;


import org.profiling.enums.LogType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Profiling {
    String  message() default "";
    boolean logResult() default true;
    boolean logParams() default true;
    boolean logTime() default true;
    boolean logCallerInfo() default true;
}
