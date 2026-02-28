package org.profiling.autoconfigure;


import org.profiling.enums.LogType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * External configuration for profiling starter behavior.
 */
@ConfigurationProperties(prefix = "profiling")
public class ProfilingProperties {
    private boolean enabled = true;
    private LogType logType = LogType.SIMPLE;
    private ProfilingMode mode = ProfilingMode.AOP;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public ProfilingMode getMode() {
        return mode;
    }

    public void setMode(ProfilingMode mode) {
        this.mode = mode;
    }
}
