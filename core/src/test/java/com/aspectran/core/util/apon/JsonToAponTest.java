package com.aspectran.core.util.apon;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * <p>Created: 2019-06-29</p>
 */
class JsonToAponTest {

    @Test
    void testConvertJsonToApon() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        sb.append("{\n");
        sb.append("  \"param1\": 111,\n");
        sb.append("  \"param2\": 222\n");
        sb.append("}\n");
        sb.append(",\n");
        sb.append("{\n");
        sb.append("  \"param3\": 333,\n");
        sb.append("  \"param4\": 444\n");
        sb.append("}\n, null");
        sb.append("]\n");

        Parameters ps = JsonToApon.from(sb.toString(), new ArrayParameters());
        System.out.println(ps);
    }

    @Test
    void testConvertJsonToApon2() throws IOException {
        String json = "{\n" + "    \"glossary\": {\n" + "        \"title\": \"example glossary\",\n" + "\t\t\"GlossDiv\": {\n" + "            \"title\": \"S\",\n" + "\t\t\t\"GlossList\": {\n" + "                \"GlossEntry\": {\n" + "                    \"ID\": \"SGML\",\n" + "\t\t\t\t\t\"SortAs\": \"SGML\",\n" + "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" + "\t\t\t\t\t\"Acronym\": \"SGML\",\n" + "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" + "\t\t\t\t\t\"GlossDef\": {\n" + "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" + "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" + "                    },\n" + "\t\t\t\t\t\"GlossSee\": \"markup\"\n" + "                }\n" + "            }\n" + "        }\n" + "    }\n" + "}";

        Parameters ps = JsonToApon.from(json);
        System.out.println(ps);
    }

}