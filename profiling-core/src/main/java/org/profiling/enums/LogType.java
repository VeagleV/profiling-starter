package org.profiling.enums;


/***
 * Enum for Logging type
 */
public enum LogType {
    /***
     * simple log format. uses no truncation or table-view. just basic view
     */
    SIMPLE,

    /***
     * prettier log format. utilized table formatting, multi-line formatting and so on. Looks great in th log but takes up a lot of space/memory
     */
    PRETTIER
}
