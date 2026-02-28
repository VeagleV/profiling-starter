package org.profiling;


import org.profiling.enums.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Core profiling interceptor logic used by both legacy enhancer proxies and
 * infrastructure-driven Spring AOP adapter.
 */
public class ProfilingMethodInterceptor implements MethodInterceptor {
    private final Class<?> targetClass;
    private final Object target;
    private final LogType defaultLogType;
    private static final Logger logger = LoggerFactory.getLogger("ProfilingLogger");

    public ProfilingMethodInterceptor(Class<?> targetClass, Object target, LogType defaultLogType) {
        this.targetClass = targetClass;
        this.target = target;
        this.defaultLogType = defaultLogType;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

        Method originalMethod = findOriginalMethod(method);

        Profiling methodAnnotation = originalMethod.getAnnotation(Profiling.class);
        Profiling classAnnotation = targetClass.getAnnotation(Profiling.class);

        Profiling annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
        if (annotation == null) {
            return invokeMethod(method, target, args);
        }
        if (!(annotation.logCallerInfo() ||
                annotation.logParams() ||
                annotation.logResult() ||
                annotation.logTime()))  {
            logger.info("Profiling method intercepted with message: {}", annotation.message());
            return invokeMethod(method, target, args);
        }


        Object result = null;
        Throwable exception = null;
        long startTime = System.nanoTime();

        try {
            result = invokeMethod(method, target, args);
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long executionTime = System.nanoTime() - startTime;
            logProfilingInfo(originalMethod, args, result, executionTime, exception, annotation);
        }
    }

    private Method findOriginalMethod(Method method) {
        try {
            return targetClass.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            try {
                Method m = targetClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e1) {
                return method;
            }
        }
    }
    private  Object invokeMethod(Method method, Object target, Object[] args) {
        try{
            Object result = method.invoke(target, args);
            return result;
        } catch (InvocationTargetException e){
            Throwable exp = e.getTargetException();
            logger.warn("Invocation of method {} failed", method.getName(), exp);

        } catch (Throwable t){
            logger.warn("Error while invoking profiling method", t);
        }
        return null;
    }
    private void logProfilingInfo(Method originalMethod, Object[] args, Object result, long executionTime, Throwable exception, Profiling annotation) {
        try {
            String callerInfo = getCallerInfo();
            String methodName = originalMethod.getDeclaringClass().getName() + "." + originalMethod.getName();
            String execTime = formatTime(executionTime);
            String message = annotation.message();

//        boolean logResult() default true; +
//        boolean logParams() default true; +
//        boolean logTime() default true; +
//        boolean logCallerInfo() default true; +
//        LogType type() default LogType.SIMPLE;

            StringBuilder sb = new StringBuilder("\n");

            if (defaultLogType == LogType.SIMPLE){
                sb.append("\n+------------------+");
                sb.append("\n| Profiling info:  |");
                sb.append("\n+------------------+");
                sb.append("\n| " + message);
                sb.append("\n| Method: " + methodName);

                if (annotation.logCallerInfo()) {
                    sb.append("\n| CallerInfo: " + callerInfo);
                }

                if (annotation.logParams()) {
                    Class<?>[] paramTypes = originalMethod.getParameterTypes();
                    sb.append("\n| Params: ");
                    for (int i = 0; i < paramTypes.length; i++) {
                        sb.append("\n| [").append(i).append("] ")
                                .append(paramTypes[i].getName()).append(" = ")
                                .append(prettyToString(args[i]));
                    }
                }

                if (annotation.logResult()) {
                    sb.append("\n| Result: ").append(prettyToString(result)).append("\n");
                }

                if (annotation.logTime()){
                    sb.append("\n| Time: ").append(formatTime(executionTime)).append("\n");
                }

                logger.info(sb.toString());
            } else if (defaultLogType == LogType.PRETTIER) {
                int maxWidth = 80;

                maxWidth = Math.max(maxWidth, methodName.length() + 10);
                maxWidth = Math.max(maxWidth, callerInfo.length() + 15);

                if (annotation.logParams() && args != null) {
                    Class<?>[] paramTypes = originalMethod.getParameterTypes();
                    for (int i = 0; i < args.length; i++) {
                        String paramLine = paramTypes[i].getSimpleName() + " = " + prettyToString(args[i]);
                        maxWidth = Math.max(maxWidth, paramLine.length() + 2);
                    }
                }

                if (annotation.logResult()) {
                    String resultStr = prettyToString(result);
                    maxWidth = Math.max(maxWidth, resultStr.length() + 15);
                }

                // Ограничиваем максимальную ширину
                maxWidth = Math.min(maxWidth, 120);
                // + - + + + | + +
                sb.append("+").append(repeatChar('-', maxWidth)).append("+\n");

                // Заголовок
                sb.append("|").append(centerText(" PROFILING INFO ", maxWidth)).append("|\n");
                sb.append("+").append(repeatChar('-', maxWidth)).append("+\n");

                // Метод
                appendFormattedLine(sb, "Method", methodName, maxWidth);

                // Откуда вызван
                appendFormattedLine(sb, "Called from", callerInfo, maxWidth);

                // Параметры
                if (annotation.logParams() && args != null && args.length > 0) {
                    sb.append("+").append(repeatChar('-', maxWidth)).append("+\n");
                    sb.append("|").append(boldText(" Parameters:", maxWidth)).append("|\n");

                    Class<?>[] paramTypes = originalMethod.getParameterTypes();
                    String[] paramNames = getParameterNames(originalMethod);

                    for (int i = 0; i < args.length; i++) {
                        String paramName = paramNames != null && i < paramNames.length
                                ? paramNames[i]
                                : "arg" + i;
                        String paramType = paramTypes[i].getSimpleName();
                        String paramValue = prettyToString(args[i]);

                        // Форматируем параметр с отступом
                        String paramLine = String.format("  [%d] %s %s = %s",
                                i, paramType, paramName, paramValue);

                        appendMultilineText(sb, paramLine, maxWidth, "|   ");
                    }
                }

                // Время выполнения
                if (annotation.logTime()) {
                    sb.append("+").append(repeatChar('-', maxWidth)).append("+\n");
                    appendFormattedLine(sb, "Execution Time",
                            execTime, maxWidth);
                }

                // Результат или исключение
                sb.append("+").append(repeatChar('-', maxWidth)).append("+\n");

                if (exception != null) {
                    appendFormattedLine(sb, "Status", " EXCEPTION", maxWidth);
                    appendFormattedLine(sb, "Exception LogType", exception.getClass().getSimpleName(), maxWidth);
                    String exceptionMsg = exception.getMessage();
                    if (exceptionMsg != null) {
                        appendMultilineText(sb, "Message: " + exceptionMsg, maxWidth, "|   ");
                    }
                } else if (annotation.logResult()) {
                    appendFormattedLine(sb, "Status", " SUCCESS", maxWidth);
                    String resultStr = prettyToString(result);

                    if (resultStr.length() > maxWidth - 20) {
                        appendMultilineText(sb, "Result: " + resultStr, maxWidth, "|   ");
                    } else {
                        appendFormattedLine(sb, "Result", resultStr, maxWidth);
                    }
                }

                // Нижняя граница
                sb.append("+").append(repeatChar('-', maxWidth)).append("+");

                logger.info(sb.toString());
            }
        } catch (Exception e) {
            logger.error("Error logging profiling info", e);
        }
    }

    private String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for ( StackTraceElement stackTraceElement : stackTrace ) {
            String className = stackTraceElement.getClassName();

            if (!className.startsWith("org.profiling")
                    && !className.contains("$$EnhancerByCGLIB$$")
                    && !className.contains("$$FastClassByCGLIB$$")
                    && !className.startsWith("org.springframework.cglib")
                    && !stackTraceElement.getMethodName().equals("getStackTrace")
                    && !stackTraceElement.getMethodName().equals("intercept")) {

                return className + "." + stackTraceElement.getMethodName()
                        + "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")";
            }
        }
        return "Unknown Caller";
    }

    private String prettyToString(Object object) {
        if (object == null) {
            return "null";
        }

        try {
            //коллекции
            if (object instanceof java.util.Collection<?> coll) {
                if (coll.isEmpty()) {
                    return "[] (empty)";
                }
                return String.format("[%d items] %s", coll.size(),
                        truncate(object.toString(), 150));
            }

            // Map
            if (object instanceof java.util.Map<?, ?> map) {
                if (map.isEmpty()) {
                    return "{} (empty)";
                }
                return String.format("{%d entries} %s", map.size(),
                        truncate(object.toString(), 150));
            }

            // массивы
            if (object.getClass().isArray()) {
                int length = java.lang.reflect.Array.getLength(object);
                if (length == 0) {
                    return "[] (empty array)";
                }
                return String.format("[%d items] %s", length,
                        truncate(java.util.Arrays.deepToString(new Object[]{object}), 150));
            }

            // Обычные объекты
            String str = object.toString();
            return truncate(str, 200);

        } catch (Exception e) {
            return object.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(object));
        }
    }

    private String formatTime(long nano){
        if (nano < 1_000) return nano + " ns";
        if (nano < 1_000_000) return String.format("%.2f μs", nano / 1_000.0);
        if (nano < 1_000_000_000) return String.format("%.2f ms", nano / 1_000_000.0);
        return String.format("%.2f s", nano / 1_000_000_000.0);
    }

    private String truncate(String str, int maxLength) {
        if ( str == null) return "null";
        if ( str.length() <= maxLength) return str;
        return str.substring(0, maxLength-3) + "...";
    }


    private void appendFormattedLine(StringBuilder sb, String label, String value, int maxWidth) {
        String labelPart = " " + label + ": ";
        int valueMaxWidth = maxWidth - labelPart.length() - 1;

        if (value.length() > valueMaxWidth) {
            value = value.substring(0, valueMaxWidth - 3) + "...";
        }

        sb.append("|").append(labelPart).append(value);

        // Дополняем пробелами до нужной ширины
        int padding = maxWidth - labelPart.length() - value.length();
        sb.append(repeatChar(' ', padding)).append("|\n");
    }

    /**
     * Форматирует многострочный текст
     */
    private void appendMultilineText(StringBuilder sb, String text, int maxWidth, String prefix) {
        int contentWidth = maxWidth - prefix.length() - 1;
        //int contentWidth = maxWidth - 4;

        if (text.length() <= contentWidth) {
            sb.append(prefix).append(text);
            int padding = maxWidth - prefix.length() - text.length();
            sb.append(repeatChar(' ', padding)).append(" |\n");
            return;
        }

        // Разбиваем на строки
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (line.length() + word.length() + 1 <= contentWidth) {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            } else {
                // Выводим текущую строку
                sb.append(prefix).append(line);
                int padding = maxWidth - prefix.length() - line.length();
                sb.append(repeatChar(' ', padding)).append(" |\n");

                // Начинаем новую строку
                line = new StringBuilder(word);
            }
        }

        // Выводим последнюю строку
        if (line.length() > 0) {
            sb.append(prefix).append(line);
            int padding = maxWidth - prefix.length() - line.length();
            sb.append(repeatChar(' ', padding)).append(" |\n");
        }
    }

    /**
     * Центрирует текст
     */
    private String centerText(String text, int width) {
        int padding = width - text.length();
        int leftPadding = padding / 2;
        int rightPadding = padding - leftPadding;

        return repeatChar(' ', leftPadding) + text + repeatChar(' ', rightPadding);
    }

    /**
     * Делает текст "жирным" (добавляет пробелы)
     */
    private String boldText(String text, int width) {
        int padding = width - text.length();
        return text + repeatChar(' ', padding);
    }

    /**
     * Повторяет символ N раз
     */
    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }



    /**
     * Пытается получить имена параметров через Reflection (Java 8+)
     */
    private String[] getParameterNames(Method method) {
        try {
            // Java 8+ поддержка Parameter.getName()
            java.lang.reflect.Parameter[] parameters = method.getParameters();
            String[] names = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                names[i] = parameters[i].getName();
            }
            return names;
        } catch (Exception e) {
            // Fallback для старых версий Java
            return null;
        }
    }

}
