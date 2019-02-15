/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Format;
import com.aspectran.core.component.bean.annotation.Parameter;
import com.aspectran.core.component.bean.annotation.Qualifier;
import com.aspectran.core.component.bean.annotation.Request;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Component
public class AnnotatedAction {

    @Request(translet = "/action-1",
            parameters = {
                    @Parameter(
                            name = "param1",
                            value = "Apple"
                    ),
                    @Parameter(
                            name = "param2",
                            value = "Tomato"
                    )
            }
    )
    public void action1(Translet translet, String param1, String param2) {
        assertNotNull(translet);
        assertEquals("Apple", param1);
        assertEquals("Tomato", param2);
    }

    @Request(translet = "/action-2",
            parameters = {
                    @Parameter(
                            name = "param1",
                            value = "1234"
                    ),
                    @Parameter(
                            name = "param2",
                            value = "5678"
                    ),
                    @Parameter(
                            name = "param3",
                            value = "88888888888888888888"
                    ),
                    @Parameter(
                            name = "param4",
                            value = "99999999999999999999"
                    )
            }
    )
    public void action2(Integer param1, int param2, int param3, Integer param4) {
        assertEquals(1234, param1);
        assertEquals(5678, param2);
        assertEquals(0, param3);
        assertNull(param4);
    }

    @Request(translet = "/action-3",
            parameters = {
                    @Parameter(
                            name = "param1",
                            value = "1234"
                    )
            }
    )
    public void action3(String param1, String param2) {
        assertNotNull(param1);
        assertNull(param2);
    }

    @Request(translet = "/action-4",
            parameters = {
                    @Parameter(
                            name = "date1",
                            value = "2019-02-15"
                    ),
                    @Parameter(
                            name = "date2",
                            value = "2019-02-15T01:30:50.123"
                    )
            }
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

}
