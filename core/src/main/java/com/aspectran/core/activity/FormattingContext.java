package com.aspectran.core.activity;

import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * <p>Created: 2019-07-06</p>
 */
public class FormattingContext {

    private static final Log log = LogFactory.getLog(FormattingContext.class);

    private static final int MAX_INDENT_SIZE = 8;

    private static final String FORMAT_INDENT_STYLE = "format.indentStyle";

    private static final String FORMAT_INDENT_SIZE = "format.indentSize";

    private static final String FORMAT_DATE_FORMAT = "format.dateFormat";

    private static final String FORMAT_DATETIME_FORMAT = "format.dateTimeFormat";

    private static final String FORMAT_NULL_WRITABLE = "format.nullWritable";

    private String indentString;

    private String dateFormat;

    private String dateTimeFormat;

    private Boolean nullWritable;

    private FormattingContext() {
    }

    public String getIndentString() {
        return indentString;
    }

    private void setIndentString(String indentString) {
        this.indentString = indentString;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    private void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    private void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public Boolean getNullWritable() {
        return nullWritable;
    }

    private void setNullWritable(Boolean nullWritable) {
        this.nullWritable = nullWritable;
    }

    public static FormattingContext parse(Activity activity) {
        String indentStyle = activity.getSetting(FORMAT_INDENT_STYLE);
        String indentSize = activity.getSetting(FORMAT_INDENT_SIZE);
        String dateFormat = activity.getSetting(FORMAT_DATE_FORMAT);
        String dateTimeFormat = activity.getSetting(FORMAT_DATETIME_FORMAT);
        Boolean nullWritable = BooleanUtils.toNullableBooleanObject(activity.getSetting(FORMAT_NULL_WRITABLE));

        String indentString = parseIndentString(indentStyle, indentSize);

        FormattingContext formattingContext = new FormattingContext();
        if (indentString != null) {
            formattingContext.setIndentString(indentString);
        }
        if (!StringUtils.isEmpty(dateFormat)) {
            formattingContext.setDateFormat(dateFormat);
        }
        if (!StringUtils.isEmpty(dateTimeFormat)) {
            formattingContext.setDateTimeFormat(dateTimeFormat);
        }
        if (nullWritable != null) {
            formattingContext.setNullWritable(nullWritable);
        }

        return formattingContext;
    }

    private static String parseIndentString(String indentStyle, String indentSize) {
        try {
            char ch = ("tab".equalsIgnoreCase(indentStyle) ? '\t' : ' ');
            int repeat = Integer.parseInt(indentSize);
            if (repeat > MAX_INDENT_SIZE) {
                if (log.isDebugEnabled()) {
                    log.debug("Indent size should be less than " + MAX_INDENT_SIZE);
                }
                repeat = MAX_INDENT_SIZE;
            }
            if (repeat > 0) {
                return StringUtils.repeat(ch, repeat);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return null;
    }

}
