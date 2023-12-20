package com.aspectran.web.support.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 12/20/23</p>
 */
class JavaScriptUtilsTest {

    @Test
    public void escape() {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        sb.append('\'');
        sb.append('\\');
        sb.append('/');
        sb.append('\t');
        sb.append('\n');
        sb.append('\r');
        sb.append('\f');
        sb.append('\b');
        sb.append('\013');
        assertEquals(JavaScriptUtils.javaScriptEscape(sb.toString()), "\\\"\\'\\\\\\/\\t\\n\\n\\f\\b\\v");
    }

    // SPR-9983

    @Test
    public void escapePsLsLineTerminators() {
        StringBuilder sb = new StringBuilder();
        sb.append('\u2028');
        sb.append('\u2029');
        String result = JavaScriptUtils.javaScriptEscape(sb.toString());
        assertEquals(result, "\\u2028\\u2029");
    }

    // SPR-9983

    @Test
    public void escapeLessThanGreaterThanSigns() {
        assertEquals(JavaScriptUtils.javaScriptEscape("<>"), "\\u003C\\u003E");
    }

}
