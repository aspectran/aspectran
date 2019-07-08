package com.aspectran.core.util.apon;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * <p>Created: 2019-07-08</p>
 */
class XmlToAponTest {

    @Test
    void testConvert1() throws IOException {
        String xml = "<container>\n" +
                "\t<item>\n" +
                "\t\t<container>\n" +
                "\t\t\t<item>a\na\na</item>\n" +
                "\t\t\t<item>bbb</item>\n" +
                "\t\t</container>\n" +
                "\t\t<container>\n" +
                "\t\t\t<item>aaa</item>\n" +
                "\t\t\t<item>bbb</item>\n" +
                "\t\t</container>\n" +
                "\t</item>\n" +
                "\t<item2>\n" +
                "\t\txyz\n" +
                "\t</item2>\n" +
                "</container>";

        Parameters ps = XmlToApon.from(xml);

        System.out.println(ps);
    }


}