package org.profiling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/***
 * Annotation for profiling(logging time, args, result, stacktrace) methods.
 * Can be placed on class to profile all of its methods.
 * Can be placed on methods to overwrite properties of profiling if needed.
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Profiling {
    /***
     * Message to be displayed in the logs
     * @return String value of the message
     */
    String  message() default "";

    /***
     * Determine whether Result of the method will show up in the logs or not
     * @return boolean value
     */
    boolean logResult() default true;

    /***
     * Determine whether Params(this includes parameters Types, as well as the values) of the method will show up in the logs or not
     * @return boolean value
     */
    boolean logParams() default true;

    /***
     * Determine whether Time completion of the method will show up in the logs or not
     * @return boolean value
     */
    boolean logTime() default true;

    /***
     * Determine whether Information about the caller of the method(class, function, row of the call) will show up in the logs or not
     * @return boolean value
     */
    boolean logCallerInfo() default true;
}
