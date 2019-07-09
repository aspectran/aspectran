package com.aspectran.core.util.apon;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * <p>Created: 2019-07-08</p>
 */
class XmlToAponTest {

    @Test
    void testConvert1() throws IOException {
        String xml = "<container id=\"12\">\n" +
                "  <item1>\n" +
                "    <container id=\"34\">\n" +
                "      <item id=\"56\">a\na\na</item>\n" +
                "      <item id=\"78\">bbb</item>\n" +
                "    </container>\n" +
                "    <container>\n" +
                "      <item>aaa</item>\n" +
                "      <item>bbb</item>\n" +
                "      <item>ccc</item>\n" +
                "    </container>\n" +
                "  </item1>\n" +
                "  <item2>\n" +
                "    xyz\n" +
                "  </item2>\n" +
                "  <item3 id=\"90\">\n" +
                "    xyz\n" +
                "  </item3>\n" +
                "  <item4>\n" +
                "    <item5 id=\"91\">\n" +
                "      xyz\n" +
                "    </item5>\n" +
                "  </item4>\n" +
                "</container>";

        Parameters ps = XmlToApon.from(xml);

        System.out.println(ps);
    }

}
