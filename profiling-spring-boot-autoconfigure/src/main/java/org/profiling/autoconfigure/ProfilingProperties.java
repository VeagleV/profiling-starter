package org.profiling.autoconfigure;


import org.profiling.enums.LogType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "profiling")
public class ProfilingProperties {
    private boolean enabled = true;
    private LogType logType = LogType.SIMPLE;

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
}
