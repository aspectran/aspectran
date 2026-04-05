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
import com.aspectran.test.ActivityTester;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@AspectranTest(
        rules = {
                "com/aspectran/mybatis/test/test-datasource.xml",
                "com/aspectran/mybatis/test/test-routing.xml"
        }
)
class SqlSessionRoutingTest {

    @Test
    void testIntelligentRouting(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            TestDao intelligentDao = activity.getBean("intelligentDao");

            // 1. Insert a member (Triggers intelligentTxAspect - Writable)
            Member member = new Member();
            member.setName("Intelligent User");
            member.setEmail("intel@example.com");
            int result = intelligentDao.insertMember(member);
            assertEquals(1, result);
            assertNotNull(member.getId());

            // 2. Get the member (Triggers intelligentReadOnlyTxAspect - ReadOnly)
            // It reuses the open writable session (Intelligent Routing).
            // Therefore, the uncommitted record should be visible.
            Member foundMember = intelligentDao.getMember(member.getId());
            System.out.println("Found member in Intelligent session (reused): " + (foundMember != null));
            assertNotNull(foundMember, "Record should be visible as it reuses the same transaction session");
            assertEquals("Intelligent User", foundMember.getName());

            return null;
        });
    }

}
