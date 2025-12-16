/*
 * Copyright (c) 2008-present The Aspectran Project
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
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StringifyContext tailored for an {@link Activity}. It reads formatting
 * preferences (indentation, pretty-print, null handling, date/time formats,
 * and locale) from the current activity's settings and request adapter and
 * configures itself accordingly.
 *
 * <p>Typical settings keys:</p>
 * <ul>
 *   <li>{@code format.prettyPrint} - enable pretty printing</li>
 *   <li>{@code format.indentStyle} - {@code tab} or spaces</li>
 *   <li>{@code format.indentSize} - number of spaces when using space indent</li>
 *   <li>{@code format.nullWritable} - whether to render nulls</li>
 *   <li>{@code format.dateTimeFormat}, {@code format.dateFormat}, {@code format.timeFormat}</li>
 * </ul>
 *
 * <p>Created: 2019-07-06</p>
 */
public class ActivityStringifyContext extends StringifyContext {

    private static final Logger logger = LoggerFactory.getLogger(ActivityStringifyContext.class);

    /** Maximum allowed indent size when using spaces. */
    static final int MAX_INDENT_SIZE = 8;

    /** Setting key to enable pretty printing. */
    public static final String FORMAT_PRETTY_PRINT = "format.prettyPrint";

    /** Setting key to choose indent style: "tab" or spaces. */
    public static final String FORMAT_INDENT_STYLE = "format.indentStyle";

    private static final String FORMAT_INDENT_STYLE_TAB = "tab";

    /** Setting key for number of spaces used when indenting. */
    public static final String FORMAT_INDENT_SIZE = "format.indentSize";

    /** Setting key to control whether null values are rendered. */
    public static final String FORMAT_NULL_WRITABLE = "format.nullWritable";

    /** Setting key for LocalDateTime output format. */
    public static final String FORMAT_DATETIME_FORMAT = "format.dateTimeFormat";

    /** Setting key for LocalDate output format. */
    public static final String FORMAT_DATE_FORMAT = "format.dateFormat";

    /** Setting key for LocalTime output format. */
    public static final String FORMAT_TIME_FORMAT = "format.timeFormat";

    /**
     * Create an Activity-aware stringify context configured from the given activity.
     * @param activity the current activity providing settings and locale (never {@code null})
     */
    ActivityStringifyContext(@NonNull Activity activity) {
        String indentStyle = activity.getSetting(FORMAT_INDENT_STYLE);
        String dateTimeFormat = activity.getSetting(FORMAT_DATETIME_FORMAT);
        String dateFormat = activity.getSetting(FORMAT_DATE_FORMAT);
        String timeFormat = activity.getSetting(FORMAT_TIME_FORMAT);

        Object prettyPrint = activity.getSetting(FORMAT_PRETTY_PRINT);
        Boolean prettyPrintToUse = null;
        if (prettyPrint instanceof Boolean prettyPrintInBool) {
            prettyPrintToUse = prettyPrintInBool;
        } else if (prettyPrint != null) {
            prettyPrintToUse = BooleanUtils.toBooleanObject(prettyPrint.toString());
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
        if (prettyPrintToUse != null) {
            setPrettyPrint(prettyPrintToUse);
        } else if (getIndentSize() > 0) {
            setPrettyPrint(true);
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

    /**
     * Parse an indent size string into an int, capping the result at {@link #MAX_INDENT_SIZE}.
     * @param indentSize the indent size string to parse
     * @return a non-negative indent size not exceeding {@link #MAX_INDENT_SIZE}
     */
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
