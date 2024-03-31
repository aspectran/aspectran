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
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

/**
 * <p>Created: 2019-07-06</p>
 */
public class FormattingContext {

    private static final Logger logger = LoggerFactory.getLogger(FormattingContext.class);

    private static final int MAX_INDENT_SIZE = 8;

    private static final String FORMAT_INDENT_TAB = "format.indentTab";

    private static final String FORMAT_INDENT_SIZE = "format.indentSize";

    private static final String FORMAT_DATE_FORMAT = "format.dateFormat";

    private static final String FORMAT_DATETIME_FORMAT = "format.dateTimeFormat";

    private static final String FORMAT_NULL_WRITABLE = "format.nullWritable";

    private boolean pretty;

    private int indentSize;

    private boolean indentTab;

    private String dateFormat;

    private String dateTimeFormat;

    private Boolean nullWritable;

    public FormattingContext() {
    }

    public boolean isPretty() {
        return pretty;
    }

    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }

    public int getIndentSize() {
        return indentSize;
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    public void setIndentTab(boolean indentTab) {
        this.indentTab = indentTab;
    }

    public String makeIndentString() {
        if (pretty) {
            if (indentTab) {
                return "\t";
            } else if (indentSize > 0) {
                return StringUtils.repeat(' ', indentSize);
            } else {
                return StringUtils.EMPTY;
            }
        }
        return null;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public Boolean getNullWritable() {
        return nullWritable;
    }

    public void setNullWritable(Boolean nullWritable) {
        this.nullWritable = nullWritable;
    }

    @NonNull
    public static FormattingContext parse(@NonNull Activity activity) {
        String indentStyle = activity.getSetting(FORMAT_INDENT_TAB);
        String indentSize = activity.getSetting(FORMAT_INDENT_SIZE);
        String dateFormat = activity.getSetting(FORMAT_DATE_FORMAT);
        String dateTimeFormat = activity.getSetting(FORMAT_DATETIME_FORMAT);
        Boolean nullWritable = BooleanUtils.toNullableBooleanObject(activity.getSetting(FORMAT_NULL_WRITABLE));

        FormattingContext formattingContext = new FormattingContext();
        if ("tab".equalsIgnoreCase(indentStyle)) {
            formattingContext.setPretty(true);
            formattingContext.setIndentTab(true);
        } else {
            int size = parseIndentSize(indentSize);
            if (size > 0) {
                formattingContext.setPretty(true);
                formattingContext.setIndentSize(size);
            }
        }
        if (StringUtils.hasLength(dateFormat)) {
            formattingContext.setDateFormat(dateFormat);
        }
        if (StringUtils.hasLength(dateTimeFormat)) {
            formattingContext.setDateTimeFormat(dateTimeFormat);
        }
        if (nullWritable != null) {
            formattingContext.setNullWritable(nullWritable);
        }
        return formattingContext;
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
