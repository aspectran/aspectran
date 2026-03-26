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
package com.aspectran.jpa.test;

import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.jpa.common.entity.Vet;
import com.aspectran.test.ActivityTester;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@AspectranTest(
    rules = "com/aspectran/jpa/test/jpa-test-context.xml",
    profiles = {"h2", "hibernate"},
    basePackages = {
            "com.aspectran.jpa.test",
            "com.aspectran.jpa.eclipselink",
            "com.aspectran.jpa.hibernate"
    }
)
class JpaRoutingTest {

    @Test
    void testStrictRouting(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            JpaTestDao strictDao = activity.getBean("strictDao");

            // 1. Insert a vet (Triggers strictTxAspect - Writable)
            Vet vet = new Vet();
            vet.setFirstName("Strict");
            vet.setLastName("User");
            strictDao.insertVet(vet);
            assertNotNull(vet.getId());

            // 2. Get the vet (Triggers strictReadOnlyTxAspect - ReadOnly)
            // With strict routing (reuseWritable=false), it opens a separate read-only session.
            // Since the insert session isn't committed yet, the record is not visible here.
            Vet foundVet = strictDao.getVet(vet.getId());
            System.out.println("Found vet in Strict Read-Only session: " + (foundVet != null));
            assertNull(foundVet, "Record should not be visible in a separate uncommitted session");

            return null;
        });
    }

    @Test
    void testIntelligentRouting(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            JpaTestDao intelligentDao = activity.getBean("intelligentDao");

            // 1. Insert a vet (Triggers intelligentTxAspect - Writable)
            Vet vet = new Vet();
            vet.setFirstName("Intelligent");
            vet.setLastName("User");
            intelligentDao.insertVet(vet);
            assertNotNull(vet.getId());

            // 2. Get the vet (Triggers intelligentReadOnlyTxAspect - ReadOnly)
            // With intelligent routing (reuseWritable=true), it reuses the open writable session.
            // Therefore, the uncommitted record should be visible.
            Vet foundVet = intelligentDao.getVet(vet.getId());
            System.out.println("Found vet in Intelligent session (reused): " + (foundVet != null));
            assertNotNull(foundVet, "Record should be visible as it reuses the same transaction session");
            assertEquals("Intelligent", foundVet.getFirstName());

            return null;
        });
    }

    @Test
    void testSingleTransaction(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            JpaTestDao singleDao = activity.getBean("singleDao");

            Vet vet = new Vet();
            vet.setFirstName("Jane");
            vet.setLastName("Doe");
            singleDao.insertVet(vet);
            assertNotNull(vet.getId());

            Vet foundVet = singleDao.getVet(vet.getId());
            assertNotNull(foundVet);
            assertEquals("Jane", foundVet.getFirstName());

            return null;
        });
    }

    @Test
    void testRollbackOnException(@NonNull ActivityTester tester) throws ActivityPerformException {
        // 1. Initial count
        Integer initialCount = tester.perform(activity -> {
            JpaTestDao strictDao = activity.getBean("strictDao");
            return strictDao.getVetList().size();
        });

        // 2. Attempt to insert both in one transaction (should fail and rollback)
        try {
            tester.perform(activity -> {
                JpaTestDao strictDao = activity.getBean("strictDao");

                Vet goodVet = new Vet();
                goodVet.setFirstName("Good");
                goodVet.setLastName("Vet");

                Vet badVet = new Vet(); // lastName is null -> triggers validation/DB exception

                // This should throw an exception that bubbles up to CoreActivity
                strictDao.insertVets(goodVet, badVet);
                return null;
            });
            fail("Should have thrown an ActivityPerformException");
        } catch (ActivityPerformException e) {
            // Expected rollback
        }

        // 3. Verify rollback
        tester.perform(activity -> {
            JpaTestDao strictDao = activity.getBean("strictDao");
            assertEquals(initialCount, strictDao.getVetList().size());
            return null;
        });
    }

}
