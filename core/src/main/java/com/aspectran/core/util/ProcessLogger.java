package com.aspectran.core.util;

/**
 * <p>Created: 2019/11/17</p>
 */
public interface ProcessLogger {

    void debug(String message);

    void info(String message);

    void warn(String message);

    void error(String message);

    void error(String message, Throwable t);

}
