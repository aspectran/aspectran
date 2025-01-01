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
package com.aspectran.embed.sample.anno;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.activity.response.transform.CustomTransformer;
import com.aspectran.core.component.bean.annotation.AttrItem;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Format;
import com.aspectran.core.component.bean.annotation.Forward;
import com.aspectran.core.component.bean.annotation.ParamItem;
import com.aspectran.core.component.bean.annotation.Qualifier;
import com.aspectran.core.component.bean.annotation.Request;
import com.aspectran.embed.sample.custom.TestCustomTransformer;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.Parameters;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Component
public class AnnotatedActivity {

    @Request("/action-1")
    @ParamItem(
            name = "param1",
            value = "Apple"
    )
    @ParamItem(
            name = "param2",
            value = "Tomato"
    )
    public void action1(Translet translet, String param1, String param2) {
        assertNotNull(translet);
        assertEquals("Apple", param1);
        assertEquals("Tomato", param2);
    }

    @Request("/action-2")
    @ParamItem(
            name = "param1",
            value = "1234"
    )
    @ParamItem(
            name = "param2",
            value = "5678"
    )
    @ParamItem(
            name = "param3",
            value = "88888888888888888888-forTest"
    )
    @ParamItem(
            name = "param4",
            value = "99999999999999999999-forTest"
    )
    public void action2(Integer param1, int param2, int param3, Integer param4) {
        assertEquals(1234, param1);
        assertEquals(5678, param2);
        assertEquals(0, param3);
        assertNull(param4);
    }

    @Request("/action-3")
        @ParamItem(
                name = "param1",
                value = "1234"
        )
    public void action3(String param1, String param2) {
        assertNotNull(param1);
        assertNull(param2);
    }

    @Request("/action-4")
    @ParamItem(
            name = "date1",
            value = "2019-02-15"
    )
    @ParamItem(
            name = "date2",
            value = "2019-02-15T01:30:50.123"
    )
    public void action4(@Format("yyyy-MM-dd") Date date1,
                        @Format("yyyy-MM-dd'T'HH:mm:ss.SSS") Date date2,
                        @Qualifier("date2") @Format("yyyy-MM-dd'T'HH:mm:ss.SSS") LocalDateTime date3) {

        String dt1 = new SimpleDateFormat("yyyy-MM-dd").format(date1);
        assertEquals("2019-02-15", dt1);

        String dt2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(date2);
        assertEquals("2019-02-15T01:30:50.123", dt2);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String dt3 = formatter.format(date3);
        assertEquals("2019-02-15T01:30:50.123", dt3);
    }

    @Request("/action-5")
    @ParamItem(
            name = "param1",
            value = "1234"
    )
    @ParamItem(
            name = "param2",
            value = "5678"
    )
    @ParamItem(
            name = "param3",
            value = "33333333"
    )
    @ParamItem(
            name = "param4",
            value = "44444444"
    )
    @ParamItem(
            name = "param5",
            value = "55555555"
    )
    public void action5(long param1, float param2, double param3, BigInteger param4, BigDecimal param5) {
        assertEquals(Long.valueOf(1234), param1);
        assertEquals(Float.valueOf(5678), param2);
        assertEquals(Double.valueOf(33333333.0), param3);
        assertEquals(BigInteger.valueOf(44444444), param4);
        //noinspection UnpredictableBigDecimalConstructorCall
        assertEquals(new BigDecimal(55555555.0), param5);
    }

    @Request("/action-6")
    public void action6(String[] param1) {
        assertArrayEquals(new String[] { "v1", "v2", "v3" }, param1);
    }

    @Request("/action-7")
    @ParamItem(
            name = "param1",
            value = "Strawberry"
    )
    @ParamItem(
            name = "param0",
            value = "Grape"
    )
    public void action7(Character param1, char param2, char[] param3) {
        assertEquals('S', param1);
        assertEquals(Character.MIN_VALUE, param2);
        assertArrayEquals(new char[] { 'A', 'B', 'C' }, param3);
    }

    @Request("/action-8")
    public void action8(
            @Qualifier("param1") Byte[] param11, @Qualifier("param1") byte[] param12,
            @Qualifier("param1") Short[] param21, @Qualifier("param1") short[] param22,
            @Qualifier("param1") Integer[] param31, @Qualifier("param1") int[] param32,
            @Qualifier("param1") Long[] param41, @Qualifier("param1") long[] param42,
            @Qualifier("param1") Float[] param51, @Qualifier("param1") float[] param52,
            @Qualifier("param1") Double[] param61, @Qualifier("param1") double[] param62,
            @Qualifier("param1") BigInteger[] param71,
            @Qualifier("param1") BigDecimal[] param81
    ) {
        assertArrayEquals(new Byte[] { 1, 2, 3 }, param11);
        assertArrayEquals(new byte[] { 1, 2, 3 }, param12);
        assertArrayEquals(new Short[] { 1, 2, 3 }, param21);
        assertArrayEquals(new short[] { 1, 2, 3 }, param22);
        assertArrayEquals(new Integer[] { 1, 2, 3 }, param31);
        assertArrayEquals(new int[] { 1, 2, 3 }, param32);
        assertArrayEquals(new Long[] { 1L, 2L, 3L }, param41);
        assertArrayEquals(new long[] { 1L, 2L, 3L }, param42);
        assertArrayEquals(new Float[] { 1F, 2F, 3F }, param51);
        assertArrayEquals(new float[] { 1F, 2F, 3F }, param52);
        assertArrayEquals(new Double[] { 1D, 2D, 3D }, param61);
        assertArrayEquals(new double[] { 1D, 2D, 3D }, param62);
        assertArrayEquals(new BigInteger[] { new BigInteger("1"), new BigInteger("2"), new BigInteger("3") }, param71);
        assertArrayEquals(new BigDecimal[] { new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3") }, param81);
    }

    @Request("/action-9")
    public void action9(@NonNull TestModel model) {
        assertEquals("Apple", model.getString());
        assertArrayEquals(new String[] { "Orange", "Grape", "Melon" }, model.getStrings());

        assertEquals('A', model.getCharacter());
        assertArrayEquals(new Character[] { 'A', 'B', 'C' }, model.getCharacters());
        assertEquals('A', model.getPcharacter());
        assertArrayEquals(new char[] { 'A', 'B', 'C' }, model.getPcharacters());

        assertEquals(Byte.valueOf("1"), model.getAbyte());
        assertArrayEquals(new Byte[] { 1, 2, 3 }, model.getBytes());
        assertEquals((byte)1, model.getPbyte());
        assertArrayEquals(new byte[] { 1, 2, 3 }, model.getPbytes());

        assertEquals(Short.valueOf("1"), model.getAshort());
        assertArrayEquals(new Short[] { 1, 2, 3 }, model.getShorts());
        assertEquals((short)1, model.getPshort());
        assertArrayEquals(new short[] { 1, 2, 3 }, model.getPshorts());

        assertEquals(Integer.valueOf("1"), model.getInteger());
        assertArrayEquals(new Integer[] { 1, 2, 3 }, model.getIntegers());
        assertEquals(1, model.getPinteger());
        assertArrayEquals(new int[] { 1, 2, 3 }, model.getPintegers());

        assertEquals(Long.valueOf("1"), model.getAlong());
        assertArrayEquals(new Long[] { 1L, 2L, 3L }, model.getLongs());
        assertEquals(1L, model.getPlong());
        assertArrayEquals(new long[] { 1L, 2L, 3L }, model.getPlongs());

        assertEquals(Float.valueOf("1"), model.getAfloat());
        assertArrayEquals(new Float[] { 1F, 2F, 3F }, model.getFloats());
        assertEquals(1F, model.getPfloat());
        assertArrayEquals(new float[] { 1F, 2F, 3F }, model.getPfloats());

        assertEquals(Double.valueOf("1"), model.getAdouble());
        assertArrayEquals(new Double[] { 1D, 2D, 3D }, model.getDoubles());
        assertEquals(1D, model.getPdouble());
        assertArrayEquals(new double[] { 1D, 2D, 3D }, model.getPdoubles());

        assertEquals(new BigInteger("1"), model.getBigInteger());
        assertArrayEquals(new BigInteger[] { new BigInteger("1"), new BigInteger("2"), new BigInteger("3") }, model.getBigIntegers());
        assertEquals(new BigDecimal("1"), model.getBigDecimal());
        assertArrayEquals(new BigDecimal[] { new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3") }, model.getBigDecimals());

        assertEquals("2019-02-17", new SimpleDateFormat("yyyy-MM-dd").format(model.getDate()));
    }

    @Request("/action-10")
    public void action10(@NonNull ParameterMap params) {
        assertArrayEquals(new String[] {"1"}, params.getParameterValues("one"));
        assertArrayEquals(new String[] {"2"}, params.getParameterValues("two"));
        assertArrayEquals(new String[] {"3"}, params.getParameterValues("three"));
        assertArrayEquals(new String[] {"1", "2", "3", "4"}, params.getParameterValues("four"));
    }

    @Request("/action-11")
    public void action11(@NonNull List<String> list) {
        assertArrayEquals(new String[] {"1", "2", "3"}, list.toArray(new String[0]));
    }

    @Request("/action-12")
    public void action12(@NonNull Parameters parameters) {
        assertEquals(1234, parameters.getInt("p1"));
        assertEquals(5678, parameters.getInt("p2"));
    }

    @Request("/action-13")
    public CustomTransformer action13() {
        return new TestCustomTransformer();
    }

    @Request("/forward")
    @Forward(
            translet = "/forwarded",
            attributes = {
                    @AttrItem(
                            name = "attr1",
                            value = "Strawberry"
                    ),
                    @AttrItem(
                            name = "attr2",
                            value = "Grape"
                    )
            }
    )
    public void forward() {
    }

    @Request("/forwarded")
    public void forwarded(@NonNull Translet translet) {
        String attr1 = translet.getAttribute("attr1");
        String attr2 = translet.getAttribute("attr2");
        assertEquals("Strawberry", attr1);
        assertEquals("Grape", attr2);
    }

}
