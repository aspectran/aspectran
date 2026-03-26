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
import com.aspectran.jpa.common.model.Vet;
import com.aspectran.test.ActivityTester;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@AspectranTest(
    rules = "com/aspectran/jpa/test/jpa-test-context.xml",
    profiles = {"h2", "hibernate"},
    basePackages = {
            "com.aspectran.jpa.common",
            "com.aspectran.jpa.test"
    }
)
class JpaRoutingTest {

    @Test
    void testReadOnlySession(@NonNull ActivityTester tester) throws ActivityPerformException {
        // 1. Initial count
        Integer initialCount = tester.perform(activity -> {
            JpaTestDao intelligentDao = activity.getBean("intelligentDao");
            return intelligentDao.getVetList().size();
        });

        // 2. Attempt a write operation in a Read-Only session
        tester.perform(activity -> {
            JpaTestDao readOnlyDao = activity.getBean("readOnlyDao");

            Vet vet = new Vet();
            vet.setFirstName("Should Not");
            vet.setLastName("Persist");

            try {
                // Calling any method on readOnlyDao triggers the single Read-Only aspect.
                readOnlyDao.insertVet(vet);
                throw new RuntimeException("Should not be able to insert a vet in a Read-Only session");
            } catch (Exception e) {
                System.out.println("Caught exception in Read-Only session: " + e.getMessage());
            }
            return null;
        });

        // 3. Verify that the vet count has not increased
        tester.perform(activity -> {
            JpaTestDao intelligentDao = activity.getBean("intelligentDao");
            assertEquals(initialCount, intelligentDao.getVetList().size(),
                    "Vet count should not have increased after a Read-Only session");
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
            // It reuses the open writable session (Intelligent Routing).
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
            JpaTestDao singleDao = activity.getBean("singleDao");
            return singleDao.getVetList().size();
        });

        // 2. Attempt to insert both in one transaction (should fail and rollback)
        try {
            tester.perform(activity -> {
                JpaTestDao singleDao = activity.getBean("singleDao");

                Vet goodVet = new Vet();
                goodVet.setFirstName("Good");
                goodVet.setLastName("Vet");

                Vet badVet = new Vet(); // lastName is null -> triggers validation/DB exception

                // This should throw an exception that bubbles up to CoreActivity
                singleDao.insertVets(goodVet, badVet);
                return null;
            });
            fail("Should have thrown an ActivityPerformException");
        } catch (ActivityPerformException e) {
            // Expected rollback
        }

        // 3. Verify rollback
        tester.perform(activity -> {
            JpaTestDao singleDao = activity.getBean("singleDao");
            assertEquals(initialCount, singleDao.getVetList().size());
            return null;
        });
    }

}
