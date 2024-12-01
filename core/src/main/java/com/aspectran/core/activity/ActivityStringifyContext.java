/*
 * Copyright (c) 2008-2024 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.activity;

import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

/**
 * <p>Created: 2019-07-06</p>
 */
public class ActivityStringifyContext extends StringifyContext {

    private static final Logger logger = LoggerFactory.getLogger(ActivityStringifyContext.class);

    static final int MAX_INDENT_SIZE = 8;

    public static final String FORMAT_PRETTY = "format.pretty";

    public static final String FORMAT_INDENT_STYLE = "format.indentStyle";

    private static final String FORMAT_INDENT_STYLE_TAB = "tab";

    public static final String FORMAT_INDENT_SIZE = "format.indentSize";

    public static final String FORMAT_NULL_WRITABLE = "format.nullWritable";

    public static final String FORMAT_DATETIME_FORMAT = "format.dateTimeFormat";

    public static final String FORMAT_DATE_FORMAT = "format.dateFormat";

    public static final String FORMAT_TIME_FORMAT = "format.timeFormat";

    ActivityStringifyContext(@NonNull Activity activity) {
        String indentStyle = activity.getSetting(FORMAT_INDENT_STYLE);
        String dateTimeFormat = activity.getSetting(FORMAT_DATETIME_FORMAT);
        String dateFormat = activity.getSetting(FORMAT_DATE_FORMAT);
        String timeFormat = activity.getSetting(FORMAT_TIME_FORMAT);

        Object pretty = activity.getSetting(FORMAT_PRETTY);
        Boolean prettyToUse = null;
        if (pretty instanceof Boolean prettyInBool) {
            prettyToUse = prettyInBool;
        } else if (pretty != null) {
            prettyToUse = BooleanUtils.toBooleanObject(pretty.toString());
        }

        Object indentSize = activity.getSetting(FORMAT_INDENT_SIZE);
        int indentSizeToUse = 0;
        if (indentSize instanceof Number number) {
            indentSizeToUse = number.intValue();
        } else if (indentSize != null) {
            indentSizeToUse = parseIndentSize(indentSize.toString());
        }

        Object nullWritable = activity.getSetting(FORMAT_NULL_WRITABLE);
        Boolean nullWritableToUse = null;
        if (nullWritable instanceof Boolean nullWritableInBool) {
            nullWritableToUse = nullWritableInBool;
        } else if (nullWritable != null) {
            nullWritableToUse = BooleanUtils.toBooleanObject(nullWritable.toString());
        }

        if (FORMAT_INDENT_STYLE_TAB.equalsIgnoreCase(indentStyle)) {
            setIndentTab(true);
            setIndentSize(1);
        } else if (indentSizeToUse > 0) {
            setIndentSize(indentSizeToUse);
        }
        if (prettyToUse != null) {
            setPretty(prettyToUse);
        } else if (getIndentSize() > 0) {
            setPretty(true);
        }
        if (nullWritableToUse != null) {
            setNullWritable(nullWritableToUse);
        }
        if (StringUtils.hasLength(dateTimeFormat)) {
            setDateTimeFormat(dateTimeFormat);
        }
        if (StringUtils.hasLength(dateFormat)) {
            setDateFormat(dateFormat);
        }
        if (StringUtils.hasLength(timeFormat)) {
            setTimeFormat(timeFormat);
        }
        if (activity.getRequestAdapter() != null) {
            setLocale(activity.getRequestAdapter().getLocale());
        }
    }

    private static int parseIndentSize(String indentSize) {
        try {
            int size = Integer.parseInt(indentSize);
            if (size > MAX_INDENT_SIZE) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Indent size should be less than " + MAX_INDENT_SIZE);
                }
                size = MAX_INDENT_SIZE;
            }
            return size;
        } catch (NumberFormatException e) {
            // ignore
        }
        return 0;
    }

}
