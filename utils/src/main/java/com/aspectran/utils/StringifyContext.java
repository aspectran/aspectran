package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * <p>Created: 2024. 11. 30.</p>
 */
public class StringifyContext implements Cloneable {

    private Boolean pretty;

    private int indentSize = -1;

    private boolean indentTab;

    private String dateFormat;

    private String dateTimeFormat;

    private Boolean nullWritable;

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

    public boolean hasDateFormat() {
        return (dateFormat != null);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public boolean hasDateTimeFormat() {
        return (dateTimeFormat != null);
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
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

    @Override
    public StringifyContext clone() {
        try {
            return (StringifyContext)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
