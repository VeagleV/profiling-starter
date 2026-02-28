package org.profiling.autoconfigure;


import org.profiling.enums.LogType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/***
 * External configuration for profiling starter behavior.
 */
@ConfigurationProperties(prefix = "profiling")
public class ProfilingProperties {
    private boolean enabled = true;
    private LogType logType = LogType.SIMPLE;
    private ProfilingMode mode = ProfilingMode.AOP;

    /***
     * Indicates whether profiling auto-configuration is active.
     *
     * @return {@code true} when profiling is enabled globally.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /***
     * Enables or disables profiling auto-configuration globally.
     *
     * @param enabled global profiling switch.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /***
     * Returns log output format used by profiling interceptors.
     *
     * @return configured log output type.
     */
    public LogType getLogType() {
        return logType;
    }

    /***
     * Sets log output format used by profiling interceptors.
     *
     * @param logType log output type.
     */
    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    /***
     * Returns selected instrumentation mode.
     *
     * @return profiling mode that defines wiring strategy.
     */
    public ProfilingMode getMode() {
        return mode;
    }

    /***
     * Sets instrumentation mode.
     *
     * @param mode profiling mode that selects AOP or legacy wiring.
     */
    public void setMode(ProfilingMode mode) {
        this.mode = mode;
    }
}
