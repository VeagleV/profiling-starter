package org.profiling.autoconfigure;

/***
 * Selects the profiling instrumentation mechanism.
 */
public enum ProfilingMode {
    /***
     * Primary mode that uses Spring AOP infrastructure beans.
     */
    AOP,
    /***
     * Legacy fallback mode that keeps the historical enhancer-based bean post processor.
     */
    LEGACY
}
