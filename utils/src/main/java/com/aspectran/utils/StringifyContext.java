package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.Nullable;

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

    private int indentSize = -1;

    private boolean indentTab;

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
        return (indentSize > -1);
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

    @Nullable
    public String getIndentString() {
        if (indentTab) {
            return "\t";
        } else if (indentSize == 1) {
            return " ";
        } else if (indentSize > 0) {
            return StringUtils.repeat(' ', indentSize);
        } else {
            return null;
        }
    }

    public boolean hasDateTimeFormat() {
        return (dateTimeFormat != null);
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
        this.dateTimeFormatter = null;
    }

    public boolean hasDateFormat() {
        return (dateFormat != null);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        this.dateFormatter = null;
    }

    public boolean hasTimeFormat() {
        return (timeFormat != null);
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
        this.timeFormatter = null;
    }

    public boolean hasLocale() {
        return (locale != null);
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

    public boolean hasNullWritable() {
        return (nullWritable != null);
    }

    public boolean isNullWritable() {
        return BooleanUtils.toBoolean(nullWritable);
    }

    public void setNullWritable(Boolean nullWritable) {
        this.nullWritable = nullWritable;
    }

    public String toString(LocalDateTime localDateTime) {
        Assert.notNull(localDateTime, "localDateTime must not be null");
        DateTimeFormatter dataTimeFormatter = getDateTimeFormatter();
        if (dataTimeFormatter != null) {
            return localDateTime.format(dataTimeFormatter);
        } else {
            return localDateTime.toString();
        }
    }

    public String toString(LocalDate localDate) {
        Assert.notNull(localDate, "localDate must not be null");
        DateTimeFormatter dateFormatter = getDateFormatter();
        if (dateFormatter != null) {
            return localDate.format(dateFormatter);
        } else {
            return localDate.toString();
        }
    }

    public String toString(LocalTime localTime) {
        Assert.notNull(localTime, "localTime must not be null");
        DateTimeFormatter timeFormatter = getTimeFormatter();
        if (timeFormatter != null) {
            return localTime.format(timeFormatter);
        } else {
            return localTime.toString();
        }
    }

    public String toString(Date date) {
        Assert.notNull(date, "date must not be null");
        if (dateTimeFormat != null) {
            LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            return toString(ldt);
        } else {
            return date.toString();
        }
    }

    private DateTimeFormatter getDateTimeFormatter() {
        if (dateTimeFormat != null && dateTimeFormatter == null) {
            if (locale != null) {
                dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat, locale);
            } else {
                dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
            }
        }
        return dateTimeFormatter;
    }

    private DateTimeFormatter getDateFormatter() {
        if (dateFormat != null && dateFormatter == null) {
            if (locale != null) {
                dateFormatter = DateTimeFormatter.ofPattern(dateFormat, locale);
            } else {
                dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
            }
        }
        return dateFormatter;
    }

    private DateTimeFormatter getTimeFormatter() {
        if (timeFormat != null && timeFormatter == null) {
            if (locale != null) {
                timeFormatter = DateTimeFormatter.ofPattern(timeFormat, locale);
            } else {
                timeFormatter = DateTimeFormatter.ofPattern(timeFormat);
            }
        }
        return timeFormatter;
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
        tsb.append("dateTimeFormat", dateTimeFormat);
        tsb.append("dateFormat", dateFormat);
        tsb.append("timeFormat", timeFormat);
        tsb.append("locale", locale);
        tsb.append("nullWritable", nullWritable);
        return tsb.toString();
    }

}
