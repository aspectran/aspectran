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
    void testDynamicRouting(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            TestDao testDao = activity.getBean("testDao");

            // 1. Insert a member (Triggers testTxAspect - Writable)
            Member member = new Member();
            member.setName("John Doe");
            member.setEmail("john@example.com");
            int result = testDao.insertMember(member);
            assertEquals(1, result);
            assertNotNull(member.getId());

            /*
            SqlSessionAdvice txAdvice = activity.getBeforeAdviceResult("testTxAspect");
            if (txAdvice != null) {
                txAdvice.commit();
            }
            */

            // 2. Get the member (Triggers testReadOnlyTxAspect - ReadOnly)
            // Note: In a real Master-Slave setup, this might return null if
            // the data hasn't synced or if using separate sessions without commit.
            Member foundMember = testDao.getMember(member.getId());

            // Depending on the DB configuration (H2 mem), if they are different sessions,
            // the record might not be visible here because the insert session isn't committed yet.
            // This test helps confirm the "Strict Separation" behavior.
            System.out.println("Found member in Read-Only session: " + (foundMember != null));
            assertNull(foundMember);

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

    /**
     * Tests the SERIALIZABLE isolation level.
     * This level is the highest isolation level and is essential for applications
     * where maximum data consistency is required, such as financial transaction
     * systems. It prevents all common concurrency problems, including dirty reads,
     * non-repeatable reads, and phantom reads, by ensuring that transactions
     * behave as if they were executed sequentially.
     * @param tester the activity tester
     * @throws ActivityPerformException if the activity performance fails
     */
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
            TestDao testDao = activity.getBean("testDao");
            return testDao.getMemberList().size();
        });

        // 2. Attempt to insert both in one transaction (should fail and rollback)
        try {
            tester.perform(activity -> {
                TestDao testDao = activity.getBean("testDao");

                Member goodMember = new Member();
                goodMember.setName("Good Member");
                goodMember.setEmail("good@example.com");

                Member badMember = new Member(); // name is null -> triggers DB exception

                // This should throw an exception that bubbles up to CoreActivity
                testDao.insertMembers(goodMember, badMember);
                return null;
            });
            fail("Should have thrown an ActivityPerformException");
        } catch (ActivityPerformException e) {
            // Expected rollback
        }

        // 3. Verify rollback: even the goodMember should not exist in the DB
        tester.perform(activity -> {
            TestDao testDao = activity.getBean("testDao");
            assertEquals(initialCount, testDao.getMemberList().size());
            return null;
        });
    }

    /**
     * Verifies that write operations (INSERT, UPDATE, DELETE) are blocked
     * when using a session explicitly marked as Read-Only.
     * This is crucial for protecting Replica/Slave databases from accidental writes.
     * @param tester the activity tester
     * @throws ActivityPerformException if the activity performance fails
     */
    @Test
    void testReadOnlySessionViolation(@NonNull ActivityTester tester) throws ActivityPerformException {
        // 1. Initial count
        Integer initialCount = tester.perform(activity -> {
            TestDao testDao = activity.getBean("testDao");
            return testDao.getMemberList().size();
        });

        // 2. Attempt a write operation in a Read-Only session
        tester.perform(activity -> {
            SqlSessionAgent testSqlSession = activity.getBean("testSqlSession");

            Member member = new Member();
            member.setName("Should Not Persist");
            member.setEmail("fail@example.com");

            try {
                // Calling a select* method on SqlSessionAgent triggers the Read-Only aspect.
                testSqlSession.selectOne("com.aspectran.mybatis.TestMapper.insertMember", member);
            } catch (Exception e) {
                // Some drivers might throw an exception here, which is also fine.
                System.out.println("Caught exception in Read-Only session: " + e.getMessage());
            }
            return null;
        });

        // 3. Verify that the data was not persisted after the previous session closed
        tester.perform(activity -> {
            TestDao testDao = activity.getBean("testDao");
            assertEquals(initialCount, testDao.getMemberList().size(),
                    "Member count should not have increased after a Read-Only session");
            return null;
        });
    }

}
