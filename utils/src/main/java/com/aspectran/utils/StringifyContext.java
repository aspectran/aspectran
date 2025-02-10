/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * <p>Created: 2024. 11. 30.</p>
 */
public class StringifyContext implements Cloneable {

    private Boolean pretty;

    private Integer indentSize;

    private Boolean indentTab;

    private Boolean nullWritable;

    private String dateTimeFormat;

    private String dateFormat;

    private String timeFormat;

    private Locale locale;

    private DateTimeFormatter dateTimeFormatter;

    private DateTimeFormatter dateFormatter;

    private DateTimeFormatter timeFormatter;

    public StringifyContext() {
    }

    public boolean hasPretty() {
        return (pretty != null);
    }

    public boolean isPretty() {
        return BooleanUtils.toBoolean(pretty);
    }

    public void setPretty(Boolean pretty) {
        this.pretty = pretty;
    }

    public boolean hasIndentSize() {
        return (indentSize != null);
    }

    public int getIndentSize() {
        return (indentSize != null ? indentSize : 0);
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    public boolean isIndentTab() {
        return BooleanUtils.toBoolean(indentTab);
    }

    public void setIndentTab(boolean indentTab) {
        this.indentTab = indentTab;
    }

    @Nullable
    public String getIndentString() {
        if (indentTab != null && indentTab) {
            return "\t";
        } else if (getIndentSize() == 1) {
            return " ";
        } else if (getIndentSize() > 0) {
            return StringUtils.repeat(' ', indentSize);
        } else {
            return null;
        }
    }

    public boolean hasNullWritable() {
        return (nullWritable != null);
    }

    public boolean isNullWritable() {
        return BooleanUtils.toBoolean(nullWritable);
    }

    public void setNullWritable(Boolean nullWritable) {
        this.nullWritable = nullWritable;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
        this.dateTimeFormatter = null;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        this.dateFormatter = null;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
        this.timeFormatter = null;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        this.dateTimeFormatter = null;
        this.dateFormatter = null;
        this.timeFormatter = null;
    }

    public String toString(LocalDateTime localDateTime) {
        return toString(localDateTime, null);
    }

    public String toString(LocalDateTime localDateTime, String format) {
        Assert.notNull(localDateTime, "localDateTime must not be null");
        DateTimeFormatter dateTimeFormatter = touchDateTimeFormatter(format);
        if (dateTimeFormatter != null) {
            return localDateTime.format(dateTimeFormatter);
        } else {
            return localDateTime.toString();
        }
    }

    public String toString(LocalDate localDate) {
        return toString(localDate, null);
    }

    public String toString(LocalDate localDate, String format) {
        Assert.notNull(localDate, "localDate must not be null");
        DateTimeFormatter dateTimeFormatter = touchDateFormatter(format);
        if (dateTimeFormatter != null) {
            return localDate.format(dateTimeFormatter);
        } else {
            return localDate.toString();
        }
    }

    public String toString(LocalTime localTime) {
        return toString(localTime, null);
    }

    public String toString(LocalTime localTime, String format) {
        Assert.notNull(localTime, "localTime must not be null");
        DateTimeFormatter dateTimeFormatter = touchTimeFormatter(format);
        if (dateTimeFormatter != null) {
            return localTime.format(dateTimeFormatter);
        } else {
            return localTime.toString();
        }
    }

    public String toString(Date date) {
        return toString(date, null);
    }

    public String toString(Date date, String format) {
        Assert.notNull(date, "date must not be null");
        DateTimeFormatter dateTimeFormatter = touchDateTimeFormatter(format);
        if (dateTimeFormatter != null) {
            LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            return ldt.format(dateTimeFormatter);
        } else {
            return date.toString();
        }
    }

    public LocalDateTime toLocalDateTime(String dateTime) {
        return toLocalDateTime(dateTime, null);
    }

    public LocalDateTime toLocalDateTime(String dateTime, String format) {
        Assert.notNull(dateTime, "dateTime must not be null");
        DateTimeFormatter dateTimeFormatter = touchDateTimeFormatter(format);
        return LocalDateTime.parse(dateTime, dateTimeFormatter);
    }

    public LocalDate toLocalDate(String date) {
        return toLocalDate(date, null);
    }

    public LocalDate toLocalDate(String date, String format) {
        Assert.notNull(date, "date must not be null");
        DateTimeFormatter dateTimeFormatter = touchDateFormatter(format);
        return LocalDate.parse(date, dateTimeFormatter);
    }

    public LocalTime toLocalTime(String time) {
        return toLocalTime(time, null);
    }

    public LocalTime toLocalTime(String time, String format) {
        Assert.notNull(time, "time must not be null");
        DateTimeFormatter dateTimeFormatter = touchTimeFormatter(format);
        return LocalTime.parse(time, dateTimeFormatter);
    }

    public Date toDate(String date) throws ParseException {
        return toDate(date, null);
    }

    public Date toDate(String date, String format) throws ParseException {
        Assert.notNull(date, "date must not be null");
        if (format != null) {
            return createSimpleDateFormat(format, locale).parse(date);
        } else {
            return createSimpleDateFormat(dateTimeFormat, locale).parse(date);
        }
    }

    private DateTimeFormatter touchDateTimeFormatter(String format) {
        if (format != null) {
            return createDateTimeFormatter(format, locale);
        } else if (dateTimeFormat != null && dateTimeFormatter == null) {
            dateTimeFormatter = createDateTimeFormatter(dateTimeFormat, locale);
        }
        return dateTimeFormatter;
    }

    private DateTimeFormatter touchDateFormatter(String format) {
        if (format != null) {
            return createDateTimeFormatter(format, locale);
        } else if (dateFormat != null && dateFormatter == null) {
            dateFormatter = createDateTimeFormatter(dateFormat, locale);
        }
        return dateFormatter;
    }

    private DateTimeFormatter touchTimeFormatter(String format) {
        if (format != null) {
            return createDateTimeFormatter(format, locale);
        } else if (timeFormat != null && timeFormatter == null) {
            timeFormatter = createDateTimeFormatter(timeFormat, locale);
        }
        return timeFormatter;
    }

    @NonNull
    private DateTimeFormatter createDateTimeFormatter(String format, Locale locale) {
        if (locale != null) {
            return DateTimeFormatter.ofPattern(format, locale);
        } else {
            return DateTimeFormatter.ofPattern(format);
        }
    }

    @NonNull
    private SimpleDateFormat createSimpleDateFormat(String format, Locale locale) {
        Assert.notNull(format, "format must not be null");
        if (locale != null) {
            return new SimpleDateFormat(format, locale);
        } else {
            return new SimpleDateFormat(format);
        }
    }

    public void merge(StringifyContext from) {
        if (pretty == null && from.pretty != null) {
            pretty = from.pretty;
        }
        if (indentSize == null && from.indentSize != null) {
            indentSize = from.indentSize;
        }
        if (indentTab == null && from.indentTab != null) {
            indentTab = from.indentTab;
        }
        if (nullWritable == null && from.nullWritable != null) {
            nullWritable = from.nullWritable;
        }
        if (dateTimeFormat == null && from.dateTimeFormat != null) {
            dateTimeFormat = from.dateTimeFormat;
        }
        if (dateFormat == null && from.dateFormat != null) {
            dateFormat = from.dateFormat;
        }
        if (timeFormat == null && from.timeFormat != null) {
            timeFormat = from.timeFormat;
        }
        if (locale == null && from.locale != null) {
            locale = from.locale;
        }
        if (dateTimeFormatter == null && from.dateTimeFormatter != null) {
            dateTimeFormatter = from.dateTimeFormatter;
        }
        if (dateFormatter == null && from.dateFormatter != null) {
            dateFormatter = from.dateFormatter;
        }
        if (timeFormatter == null && from.timeFormatter != null) {
            timeFormatter = from.timeFormatter;
        }
    }

    @Override
    public StringifyContext clone() {
        try {
            return (StringifyContext)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("pretty", pretty);
        if (indentSize > -1) {
            tsb.append("indentSize", indentSize);
        }
        tsb.append("indentTab", indentTab);
        tsb.append("nullWritable", nullWritable);
        tsb.append("dateTimeFormat", dateTimeFormat);
        tsb.append("dateFormat", dateFormat);
        tsb.append("timeFormat", timeFormat);
        tsb.append("locale", locale);
        return tsb.toString();
    }

}
