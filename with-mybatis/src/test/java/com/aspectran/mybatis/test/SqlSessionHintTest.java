/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.mybatis.test;

import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.mybatis.test.dao.TestDao;
import com.aspectran.mybatis.test.model.Member;
import com.aspectran.mybatis.test.service.TestHintService;
import com.aspectran.test.ActivityTester;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AspectranTest(
        rules = {
                "com/aspectran/mybatis/test/test-datasource.xml",
                "com/aspectran/mybatis/test/test-routing.xml"
        },
        basePackages = {
                "com.aspectran.mybatis.test.service"
        }
)
class SqlSessionHintTest {

    @Test
    void testReadOnlyHint(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            TestHintService hintService = activity.getBean(TestHintService.class);

            int initialCount = hintService.getMembers().size();

            // Try to insert in a read-only hint method (should fail silently or not persist)
            Member member = new Member();
            member.setName("ReadOnly Hint User");
            member.setEmail("readonly@example.com");

            // We need a way to verify if it's actually read-only.
            // In our implementation, we set the connection to read-only.
            // H2 might throw an exception or just ignore it depending on the operation.
            try {
                hintService.getMembers(); // activates read-only hint
                // If we call insert here, it should be in the same read-only session
                hintService.addMember(member);
            } catch (Exception e) {
                System.out.println("Caught expected exception in read-only hint: " + e.getMessage());
            }

            assertEquals(initialCount, hintService.getMembers().size());
            return null;
        });
    }

}
