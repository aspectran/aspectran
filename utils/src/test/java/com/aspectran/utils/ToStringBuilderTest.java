package com.aspectran.utils;

import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToStringBuilderTest {

    @Test
    void testMap() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        assertEquals("{key1=value1, key2=value2}", ToStringBuilder.toString(map));
        assertEquals("Map {key1=value1, key2=value2}", ToStringBuilder.toString("Map", map));
    }

    @Test
    void testCollection() {
        List<String> list = new ArrayList<>();
        list.add("value1");
        list.add("value2");

        assertEquals("[value1, value2]", ToStringBuilder.toString(list));
        assertEquals("List [value1, value2]", ToStringBuilder.toString("List", list));
    }

    @Test
    void testParameters() {
        Parameters parameters = new VariableParameters();
        parameters.putValue("key1", "value1");
        parameters.putValue("key2", "value2");

        assertEquals("{key1=value1, key2=value2}", ToStringBuilder.toString(parameters));
        assertEquals("Parameters {key1=value1, key2=value2}", ToStringBuilder.toString("Parameters", parameters));
    }

    @Test
    void testToStringBuilder() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        String result = ToStringBuilder.toString(map);

        assertEquals("{key1=value1, key2=value2}", result);
    }

    @Test
    void testAppend() {
        ToStringBuilder tsb = new ToStringBuilder("Named");
        tsb.append("key1", "value1");
        tsb.append("key2", "value2");

        assertEquals("Named {key1=value1, key2=value2}", tsb.toString());
    }

    @Test
    void testAppend2() {
        ToStringBuilder tsb = new ToStringBuilder("Named");
        tsb.append("key1", "value1");
        tsb.append("key2", "value2");
        assertEquals("Named {key1=value1, key2=value2}", tsb.toString());
        tsb.append("key3", "value3");
        assertEquals("Named {key1=value1, key2=value2, key3=value3}", tsb.toString());
    }

    @Test
    void testCircularReference() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", map);

        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> ToStringBuilder.toString(map),
            "Expected doThing() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Serialization Failure"));
    }

    @Test
    void testDateFormat() throws ParseException {
        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        SimpleDateFormat formatter = new SimpleDateFormat(stringifyContext.getDateTimeFormat(), Locale.ENGLISH);
        Date date1 = formatter.parse("2024-12-01 10:39:24");

        Map<String, Object> map = new HashMap<>();
        map.put("key1", date1);

        String result = ToStringBuilder.toString("Variable", map, stringifyContext);
        assertEquals("Variable {key1=2024-12-01 10:39:24}", result);
    }

    @Test
    void testVariable() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        Parameters parameters = new VariableParameters();
        parameters.putValue("key1", "value1");
        parameters.putValue("key2", "value2");
        map.put("parameters", parameters);

        String result = ToStringBuilder.toString("Variable", map);
        assertEquals("Variable {key1=value1, key2=value2, parameters={key1=value1, key2=value2}}", result);
    }

}
