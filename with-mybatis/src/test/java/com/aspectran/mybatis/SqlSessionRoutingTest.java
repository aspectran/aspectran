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
package com.aspectran.mybatis;

import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.test.ActivityTester;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@AspectranTest(
    rules = "com/aspectran/mybatis/test-context.xml"
)
class SqlSessionRoutingTest {

    @Test
    void testStrictRouting(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            TestDao strictDao = activity.getBean("strictDao");

            // 1. Insert a member (Triggers strictTxAspect - Writable)
            Member member = new Member();
            member.setName("Strict User");
            member.setEmail("strict@example.com");
            int result = strictDao.insertMember(member);
            assertEquals(1, result);
            assertNotNull(member.getId());

            // 2. Get the member (Triggers strictReadOnlyTxAspect - ReadOnly)
            // With strict routing (reuseWritable=false), it opens a separate read-only session.
            // Since the insert session isn't committed yet, the record is not visible here.
            Member foundMember = strictDao.getMember(member.getId());
            System.out.println("Found member in Strict Read-Only session: " + (foundMember != null));
            assertNull(foundMember, "Record should not be visible in a separate uncommitted session");

            return null;
        });
    }

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
            // With intelligent routing (reuseWritable=true), it reuses the open writable session.
            // Therefore, the uncommitted record should be visible.
            Member foundMember = intelligentDao.getMember(member.getId());
            System.out.println("Found member in Intelligent session (reused): " + (foundMember != null));
            assertNotNull(foundMember, "Record should be visible as it reuses the same transaction session");
            assertEquals("Intelligent User", foundMember.getName());

            return null;
        });
    }

    @Test
    void testSingleTransaction(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            TestDao singleDao = activity.getBean("singleDao");

            Member member = new Member();
            member.setName("Jane Doe");
            member.setEmail("jane@example.com");
            int result = singleDao.insertMember(member);
            assertEquals(1, result);

            Member foundMember = singleDao.getMember(member.getId());
            assertNotNull(foundMember);
            assertEquals("Jane Doe", foundMember.getName());

            return null;
        });
    }

    @Test
    void testSerializableTransaction(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            TestDao serializableDao = activity.getBean("serializableDao");

            Member member = new Member();
            member.setName("Serializable User");
            member.setEmail("serial@example.com");
            int result = serializableDao.insertMember(member);
            assertEquals(1, result);

            Member foundMember = serializableDao.getMember(member.getId());
            assertNotNull(foundMember);
            assertEquals("Serializable User", foundMember.getName());

            return null;
        });
    }

    @Test
    void testRollbackOnException(@NonNull ActivityTester tester) throws ActivityPerformException {
        // 1. Initial count
        Integer initialCount = tester.perform(activity -> {
            TestDao strictDao = activity.getBean("strictDao");
            return strictDao.getMemberList().size();
        });

        // 2. Attempt to insert both in one transaction (should fail and rollback)
        try {
            tester.perform(activity -> {
                TestDao strictDao = activity.getBean("strictDao");

                Member goodMember = new Member();
                goodMember.setName("Good Member");
                goodMember.setEmail("good@example.com");

                Member badMember = new Member(); // name is null -> triggers DB exception

                // This should throw an exception that bubbles up to CoreActivity
                strictDao.insertMembers(goodMember, badMember);
                return null;
            });
            fail("Should have thrown an ActivityPerformException");
        } catch (ActivityPerformException e) {
            // Expected rollback
        }

        // 3. Verify rollback
        tester.perform(activity -> {
            TestDao strictDao = activity.getBean("strictDao");
            assertEquals(initialCount, strictDao.getMemberList().size());
            return null;
        });
    }

    @Test
    void testReadOnlySessionViolation(@NonNull ActivityTester tester) throws ActivityPerformException {
        // 1. Initial count
        Integer initialCount = tester.perform(activity -> {
            TestDao strictDao = activity.getBean("strictDao");
            return strictDao.getMemberList().size();
        });

        // 2. Attempt a write operation in a Read-Only session
        tester.perform(activity -> {
            SqlSessionAgent strictRoutingSqlSession = activity.getBean("strictRoutingSqlSession");

            Member member = new Member();
            member.setName("Should Not Persist");
            member.setEmail("fail@example.com");

            try {
                // Calling a select* method on SqlSessionAgent triggers the Read-Only aspect.
                strictRoutingSqlSession.selectOne("com.aspectran.mybatis.TestMapper.insertMember", member);
            } catch (Exception e) {
                System.out.println("Caught exception in Read-Only session: " + e.getMessage());
            }
            return null;
        });

        // 3. Verify
        tester.perform(activity -> {
            TestDao strictDao = activity.getBean("strictDao");
            assertEquals(initialCount, strictDao.getMemberList().size(),
                    "Member count should not have increased after a Read-Only session");
            return null;
        });
    }

}
