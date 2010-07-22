/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.slf4j.Marker;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.spi.LocationAwareLogger;

@SuppressWarnings("serial")
public final class WinkLoggerAdapter extends MarkerIgnoringBase implements
        LocationAwareLogger {

    /*
     * We create a new Logger instance for each call because we want the level controlled by WinkLogHandler.
     * It's clunky, but the level is controllable by individual unittest methods, and we need to accommodate that.
     */
    
    String loggerName;
    WinkLogHandler winkLogHandler;
    
    WinkLoggerAdapter(java.util.logging.Logger logger) {
        winkLogHandler = new WinkLogHandler();
        loggerName = logger.getName();
    }

    public boolean isTraceEnabled() {
        return WinkLogHandler.getLogLevel().equals(Level.FINEST);
    }

    public void trace(String message) {
        log(Level.FINEST, message, null);
    }

    public void trace(String message, Object arg) {
        log(Level.FINEST, message, null);
    }

    public void trace(String message, Object arg1, Object arg2) {
        log(Level.FINEST, message, null);
    }

    public void trace(String message, Object[] argArray) {
        log(Level.FINEST, message, null);
    }

    public void trace(String msg, Throwable t) {
        log(Level.FINEST, msg, t);
    }

    public boolean isDebugEnabled() {
        return WinkLogHandler.getLogLevel().equals(Level.FINE);
    }

    public void debug(String msg) {
        log(Level.FINE, msg, null);
    }

    public void debug(String message, Object arg) {
        log(Level.FINE, message, null);
    }

    public void debug(String message, Object arg1, Object arg2) {
        log(Level.FINE, message, null);
    }

    public void debug(String message, Object[] argArray) {
        log(Level.FINE, message, null);
    }

    public void debug(String msg, Throwable t) {
        log(Level.FINE, msg, t);
    }

    public boolean isInfoEnabled() {
        return WinkLogHandler.getLogLevel().equals(Level.INFO);
    }

    public void info(String msg) {
        log(Level.INFO, msg, null);
    }

    public void info(String message, Object arg) {
        log(Level.INFO, message, null);
    }

    public void info(String message, Object arg1, Object arg2) {
        log(Level.INFO, message, null);
    }

    public void info(String message, Object[] argArray) {
        log(Level.INFO, message, null);
    }

    public void info(String msg, Throwable t) {
        log(Level.INFO, msg, t);
    }

    public boolean isWarnEnabled() {
        return WinkLogHandler.getLogLevel().equals(Level.WARNING);
    }

    public void warn(String msg) {
        log(Level.WARNING, msg, null);
    }

    public void warn(String message, Object arg) {
        log(Level.WARNING, message, null);
    }

    public void warn(String message, Object arg1, Object arg2) {
        log(Level.WARNING, message, null);
    }

    public void warn(String message, Object[] argArray) {
        log(Level.WARNING, message, null);
    }

    public void warn(String msg, Throwable t) {
        log(Level.WARNING, msg, t);
    }

    public boolean isErrorEnabled() {
        return WinkLogHandler.getLogLevel().equals(Level.SEVERE);
    }

    public void error(String msg) {
        log(Level.SEVERE, msg, null);
    }

    public void error(String message, Object arg) {
        log(Level.SEVERE, message, null);
    }

    public void error(String message, Object arg1, Object arg2) {
        log(Level.SEVERE, message, null);
    }

    public void error(String message, Object[] argArray) {
        log(Level.SEVERE, message, null);
    }

    public void error(String msg, Throwable t) {
        log(Level.SEVERE, msg, t);
    }

    private void log(Level level, String msg, Throwable t) {
        LogRecord record = new LogRecord(level, msg);
        record.setThrown(t);
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(loggerName);
        logger.setLevel(WinkLogHandler.getLogLevel());
        // all of the WinkLogHander internals are static, so despite having a new instance
        // of Logger and WinkLogHandler, we'll get the expected log records
        logger.addHandler(winkLogHandler);
        record.setLoggerName(logger.getName());
        logger.log(record);
        logger.removeHandler(winkLogHandler);
    }

    public void log(Marker marker, String caller, int level, String message,
            Throwable t) {
        log(Level.FINEST, message, t);
    }
}
